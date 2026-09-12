"""Export actual source with authored annotations; never infer a program from its text."""
from .chapter_diagrams import DIAGRAMS
from hashlib import sha256
from html import escape
from pathlib import Path
import json
import re
from pygments import lex
from pygments.lexers import get_lexer_by_name
from pygments.token import Comment, Keyword, String, Number, Name, Operator

SCOPES = {'product': 'Production', 'harness': 'Verification harness', 'book': 'Book', 'examples': 'Golden stories', 'scripts': 'Build and run', 'project': 'Build configuration'}
SUFFIXES = {'.html', '.scala', '.daml', '.md', '.json', '.yaml', '.yml', '.sbt', '.py', '.js', '.mjs', '.css', '.sh', '.properties'}
EXCLUDED = {'target', '.daml', '.git', '.artifacts', '__pycache__', 'recordings', 'history'}
LANGUAGES = {'.html':'html', '.scala':'scala', '.sbt':'scala', '.daml':'daml', '.md':'markdown', '.yaml':'yaml', '.yml':'yaml', '.json':'json', '.py':'python', '.js':'javascript', '.mjs':'javascript', '.css':'css', '.sh':'bash'}


def source_paths(root):
    candidates = [root/'build.sbt', root/'README.md']
    for scope in SCOPES:
        candidates.extend((root/scope).rglob('*'))
    for path in sorted(set(candidates)):
        relative = path.relative_to(root)
        if path.is_symlink() or not path.is_file() or any(part in EXCLUDED for part in relative.parts):
            continue
        if path.suffix not in SUFFIXES and not (relative.parts[0]=='scripts' and not path.suffix):
            continue
        if path.stat().st_size > 400_000:
            raise ValueError(f'Source file exceeds the explicit catalog size limit: {relative}')
        yield path


def annotations(text, path):
    result = {}
    language=LANGUAGES.get(Path(path).suffix,'text')
    lexer=get_lexer_by_name('haskell' if language=='daml' else language,stripnl=False,ensurenl=False)
    # A lexer may split one comment into many tokens. Rejoin its original lines,
    # blanking executable text so annotation examples in strings cannot become metadata.
    comments=''.join(value if token in Comment else re.sub(r'[^\n]', ' ', value) for token,value in lex(text,lexer))
    active=None
    for line,part in enumerate(comments.splitlines(),1):
        content=re.sub(r'^\s*(?:/\*\*|\*|--|#)\s?', '', part).strip()
        if not content or content in {'/','*/'}:
            active=None
            continue
        match=re.match(r'^@(?:book|module)\.(\w+)(?:\s+(.*))?$',content)
        if match:
            key,content=match.groups()
            if key not in {'slice','role','summary'}:raise ValueError(f'Unknown book annotation {key}: {path}')
            if key in result:raise ValueError(f'Duplicate book annotation {key}: {path}')
            result[key]=content or ''
            result.setdefault('line',line)
            active=key
        elif active:
            result[active]=(result[active]+' '+content).strip()
    if result and not all(result.get(key) for key in ['slice','role','summary']):
        raise ValueError(f'Incomplete book annotation: {path}')
    return result


def colored_lines(text, language):
    lexer = get_lexer_by_name('haskell' if language=='daml' else language, stripnl=False, ensurenl=False)
    lines = ['']
    for token, value in lex(text, lexer):
        if language=='daml' and token in Name and value in {'template','choice','controller','signatory','observer','ensure','interface','instance','viewtype','nonconsuming','exercise','create','fetch','archive','with'}:token=Keyword
        kind = next((css for family,css in [(Comment,'comment'),(Keyword,'keyword'),(String,'string'),(Number,'number'),(Name.Class,'type'),(Name.Function,'function'),(Operator,'operator')] if token in family),'plain')
        for index,part in enumerate(value.split('\n')):
            if index: lines.append('')
            if part: lines[-1] += f'<span class="token-{kind}">{escape(part)}</span>'
    if text.endswith('\n'): lines.pop()
    return lines


def build_catalog(root):
    slices = json.loads((root/'book/edition-0.2/atlas/slices.json').read_text())
    known = {s['id'] for s in slices}
    if len(known)!=len(slices): raise ValueError('Duplicate slice identity')
    context_path=root/'book/edition-0.2/atlas/packages.json'
    contexts=json.loads(context_path.read_text()) if context_path.exists() else []
    if len({c['id'] for c in contexts})!=len(contexts):raise ValueError('Duplicate package context')
    for context in contexts:
        if context['slice'] not in known or not context['summary']:raise ValueError('Invalid package context')
        for prefix in context['paths']:
            if not (root/prefix).exists():raise ValueError('Missing package context path: '+prefix)
    source = {}
    outputs = {}
    for path in source_paths(root):
        name = path.relative_to(root).as_posix()
        text = path.read_text()
        annotation = annotations(text,name)
        if annotation.get('slice')=='presentation':annotation['slice']='book'
        if annotation and annotation['slice'] not in known:
            raise ValueError(f'Unknown slice in {name}')
        identifier = sha256(name.encode()).hexdigest()[:16]
        language = LANGUAGES.get(path.suffix,'bash' if name.startswith('scripts/') else 'text')
        entry = dict(path=name,id=identifier,sha256=sha256(path.read_bytes()).hexdigest(),lines=len(text.splitlines()),language=language,owner=SCOPES.get(name.split('/')[0],'Repository'),annotation=annotation)
        matches=[(len(prefix),context) for context in contexts for prefix in context['paths'] if name==prefix or name.startswith(prefix+'/')]
        if matches:
            maximum=max(length for length,_ in matches)
            owners={context['id']:context for length,context in matches if length==maximum}
            if len(owners)!=1:raise ValueError('Ambiguous package context: '+name)
            entry['context']=next(iter(owners))
        elif contexts:raise ValueError('Missing package context: '+name)
        source[name]=entry
        payload = dict(text=text,lines=colored_lines(text,language))
        outputs[f'reader/files/{identifier}.js'] = 'window.HarmoniaSourceFiles = window.HarmoniaSourceFiles || {};\nwindow.HarmoniaSourceFiles['+json.dumps(name)+'] = '+json.dumps(payload,ensure_ascii=False).replace('<','\\u003c')+';\n'
    for item in slices:
        ids={n['id'] for n in item['nodes']}
        if not ids or len(ids)!=len(item['nodes']):raise ValueError('Empty or duplicate diagram node')
        for node in item['nodes']:
            if node['file'] not in source:raise ValueError('Missing diagram source: '+node['file'])
            entry=source[node['file']]
            if entry['annotation']:
                node['label']=entry['annotation']['role']
                node['detail']=entry['annotation']['summary']
            if entry['annotation'] and entry['annotation']['slice']!=item['id']:
                raise ValueError('Annotation and slice disagree: '+node['file'])
            if not node.get('label') or not node.get('detail'):raise ValueError('Missing diagram documentation: '+node['file'])
            entry.setdefault('slices',[]).append(item['id'])
        if any(a not in ids or b not in ids for a,b in item['edges']):raise ValueError('Unknown diagram endpoint')
        placed=set()
        while placed!=ids:
            ready={n for n in ids-placed if all(a in placed for a,b in item['edges'] if b==n)}
            if not ready:raise ValueError('Cycle in authored diagram')
            placed|=ready
        if not (root/item['evidence']).is_file():raise ValueError('Missing evidence: '+item['evidence'])
    tree_hash=sha256(''.join(f'{p}:{e["sha256"]}\n' for p,e in source.items()).encode()).hexdigest()
    return dict(chapterDiagrams=DIAGRAMS,files=source,contexts={c['id']:{**c,'files':[p for p,f in source.items() if f.get('context')==c['id']]} for c in contexts},slices={s['id']:s for s in slices},sha256=tree_hash,counts=dict(files=len(source),lines=sum(f['lines'] for f in source.values()),annotated=sum(bool(f['annotation']) for f in source.values()))), outputs
