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
    result['laboratory.html'] = pages.shell('Recorded workflows', '<main id="main" class="quiet-content"><p class="eyebrow">Recorded execution · <span id="laboratory-position"></span></p><h1 id="laboratory-title"></h1><p id="laboratory-action" class="quiet-note"></p><nav class="quiet-paging"><button id="previous-step">← Previous</button><button id="next-step">Next →</button></nav><div id="laboratory-stage"></div><section id="laboratory-observation" class="reader-note"></section><p id="recording-provenance" class="citation"></p></main>',kind='laboratory',stories=recordings)
    return result
