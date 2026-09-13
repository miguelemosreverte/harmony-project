"""Keep manifest dependencies, reviewed execution handoffs, and reading order distinct."""
from pathlib import PurePosixPath
from .conversations import RELATIONSHIPS
from hashlib import sha256
import json


def build(root, files, slices):
    manifests=json.loads((root/'book/edition-0.2/atlas/package-dependencies.json').read_text())
    packages={m['file']:m for m in manifests}
    for path,package in packages.items():
        if sha256((root/path).read_bytes()).hexdigest()!=package['sha256']:
            raise ValueError('Stale manifest projection: '+path)
    actual={p.relative_to(root).as_posix() for p in (root/'product/ledger').rglob('daml.yaml') if '.daml' not in p.parts}
    if set(packages)!=actual:raise ValueError('Manifest inventory changed; rebuild the package map')
    artifacts={(root/path).parent.joinpath('.daml/dist',m['name']+'-'+m['version']+'.dar').resolve():path for path,m in packages.items()}
    dependencies={path:[] for path in packages}
    external={}
    for path,package in packages.items():
        for imported in package['imports']:
            artifact=(root/path).parent.joinpath(imported).resolve()
            if artifact in artifacts:target=artifacts[artifact]
            else:
                target='external-'+sha256(imported.encode()).hexdigest()[:12]
                external[target]=dict(id=target,label=PurePosixPath(imported).name,detail='This manifest declares a generated or external DAR at '+imported+'. Its presence here records the declaration, not a successful build or authorization.',actor='Declared external DAR',file=path)
            dependencies[path].append(target)
    maps={}
    for item in slices:
        seeds={path for path in packages if any(n['file']==path or n['file'].startswith(str(PurePosixPath(path).parent)+'/') for n in item['nodes'])}
        if item['id']=='packages':seeds.add('product/ledger/bindings/daml.yaml')
        if seeds:
            selected=set(seeds)
            while True:
                expanded=selected|{d for path in selected for d in dependencies.get(path,[])}
                if expanded==selected:break
                selected=expanded
            nodes=[]
            for path in sorted(selected):
                if path in external:nodes.append(external[path])
                else:
                    m=packages[path]
                    nodes.append(dict(id=path,label=m['name'],actor='Daml package · '+m['version'],file=path,detail=f"{path} declares {len(m['imports'])} DAR imports. Standard dependencies: {', '.join(m['dependencies'])}."))
            maps[item['id']]=dict(id=item['id'],title='Declared package dependencies',relationship='An arrow points from an imported DAR to the package that declares it in data-dependencies. This graph is parsed from current daml.yaml files; it is not a runtime call graph.',nodes=nodes,edges=[[dependency,path] for path in sorted(selected) for dependency in dependencies.get(path,[])],status='Parsed manifest data; source fingerprints checked')
    runtime=json.loads((root/'book/edition-0.2/atlas/execution.json').read_text())
    for key,graph in runtime.items():
        if key not in {s['id'] for s in slices}:raise ValueError('Unknown execution map slice')
        ids={n['id'] for n in graph['nodes']}
        for node in graph['nodes']:
            for reference in [dict(file=node['file'],sha256=node['sha256']),*node.get('related',[])]:
                if reference['file'] not in files:raise ValueError('Missing execution source: '+reference['file'])
                if files[reference['file']]['sha256']!=reference['sha256']:raise ValueError('Execution map needs source review: '+reference['file'])
        if any(a not in ids or b not in ids for a,b in graph['edges']):raise ValueError('Unknown execution handoff endpoint')
    for graph in [*maps.values(),*runtime.values()]:
        ids={n['id'] for n in graph['nodes']};placed=set()
        while placed!=ids:
            ready={n for n in ids-placed if all(a in placed for a,b in graph['edges'] if b==n)}
            if not ready:raise ValueError('Cyclic relationship diagram')
            placed|=ready
    art = {'financing':'private-approval','process':'branch-approved','transfer':'transfer-approved',
           'composition':'composer-direct','packages':'package-builder','book':'evidence-review'}
    for key, graph in maps.items(): graph['conversation'] = dict(RELATIONSHIPS['dependencies'], illustration=art[key])
    for key, graph in runtime.items(): graph['conversation'] = dict(RELATIONSHIPS['execution'], illustration=art[key])
    return dict(dependencies=maps,execution=runtime)
