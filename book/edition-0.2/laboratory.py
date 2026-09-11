"""Package all preserved executions beside current, safely rendered source chapters."""
from pathlib import Path
from markdown_it import MarkdownIt
import re
import pages


def chapters(root):
    result = []
    for path in sorted((root/'book').glob('[0-9][0-9]-*.md')):
        text = path.read_text()
        def include(match):
            target = (path.parent/match[1]).resolve()
            if not target.is_relative_to((root/'book').resolve()) or target.suffix not in {'.scala','.daml'}:
                raise ValueError('Invalid chapter code inclusion: '+match[1])
            code = target.read_text()
            if len(code.encode()) > 65536:raise ValueError('Oversized chapter inclusion')
            fence = '`' * max(3, max((len(m[0])+1 for m in re.finditer(r'`+',code)),default=3))
            return fence+target.suffix.removeprefix('.')+'\n'+code+'\n'+fence
        expanded = re.sub(r'\{\{code:\s*([^}]+?)\s*\}\}', include, text)
        parser = MarkdownIt('commonmark',{'html':False}).enable('table')
        tokens = parser.parse(expanded)
        for token in tokens:
            for child in token.children or []:
                if child.type == 'link_open':
                    href = child.attrGet('href') or ''
                    if ':' not in href and not href.startswith('#'):child.attrSet('href','source/book/'+href)
        result.append(dict(id=path.name,title=text.splitlines()[0].removeprefix('# '),html=parser.renderer.render(tokens,parser.options,{})))
    return result


def outputs(root, recordings):
    raw = {}
    result = {}
    directory = root/'book/edition-0.2/recordings/evidence'
    for story in recordings:
        for path in sorted((directory/story).iterdir()):
            name = 'evidence/'+story+'/'+path.name
            raw[name] = path.read_text()
            result[name] = raw[name]
    evidence = dict(stories=list(recordings.values()),chapters=chapters(root))
    result['laboratory-evidence.js'] = 'window.HarmoniaLaboratoryEvidence = '+pages.json_script(evidence)+';\nwindow.HarmoniaEvidenceFiles = '+pages.json_script(raw)+';\n'
    template = (root/'book/site/index.html').read_text()
    result['laboratory.html'] = template.replace('href="source/book/site/print.css"', 'href="../../book/site/print.css"').replace('src="source/design/0.2/reader/catalog.js"', 'src="reader/catalog.js"').replace('href="book.css"','href="../../book/site/book.css"').replace('href="source/product/','href="../../product/').replace('src="evidence.js"','src="laboratory-evidence.js"').replace('src="main.js"','src="../../book/browser/target/scala-3.3.6/harmonia-reader-fastopt/main.js"').replace('src="laboratory.js"','src="../../book/site/laboratory.js"').replace('data-guide="source/design/0.2/"','data-guide="./"').replace('<script defer src="laboratory-evidence.js">','<script defer src="run-recordings.js"></script><script defer src="laboratory-evidence.js">')
    return result
