#!/usr/bin/env python3
"""Build the source-cited design edition. No product code or network is involved."""
from dataclasses import dataclass
from hashlib import sha256
from html import escape
from html.parser import HTMLParser
from pathlib import Path
import json
import re

ROOT = Path(__file__).resolve().parents[2]
EDITION = Path("book/edition-0.2")
SITE = Path("design/0.2")


@dataclass(frozen=True)
class Unit:
    id: str
    line: int
    text: str

    @property
    def words(self):
        return len(re.findall(r"\w+(?:[’'-]\w+)*", self.text))


@dataclass(frozen=True)
class Passage:
    source: str
    start: int
    end: int
    chapter: str
    title: str


class VisibleText(HTMLParser):
    """Browser-decoded body text, including SVG labels; never CSS or comments."""
    def __init__(self):
        super().__init__(convert_charrefs=True)
        self.body = False
        self.hidden = 0
        self.units = []

    def handle_starttag(self, tag, attrs):
        if tag == "body":
            self.body = True
        if tag in {"style", "script", "template"}:
            self.hidden += 1

    def handle_endtag(self, tag):
        if tag in {"style", "script", "template"}:
            self.hidden -= 1
        if tag == "body":
            self.body = False

    def handle_data(self, data):
        if self.body and not self.hidden and data.strip():
            self.units.append(Unit(f"architecture-T{len(self.units)+1:03}", self.getpos()[0], data.strip()))


def source_units(name, text):
    if name == "proposal":
        return [Unit(f"proposal-L{line}", line, value)
                for line, value in enumerate(text.splitlines(), 1) if value.strip()]
    parser = VisibleText()
    parser.feed(text)
    return parser.units


def read_passages(text):
    passages = []
    for line in text.splitlines():
        cells = [cell.strip() for cell in line.strip().strip("|").split("|")]
        if len(cells) != 4 or cells[0] not in {"proposal", "architecture"}:
            continue
        start, end = map(int, cells[1].split("-"))
        if start < 1 or end < start:
            raise ValueError(f"Invalid source range: {line}")
        passages.append(Passage(cells[0], start, end, cells[2], cells[3]))
    if not passages:
        raise ValueError("Empty coverage map")
    return passages


def inline(text):
    """The chapter narrative uses only text and explicit Markdown links."""
    value = escape(text)
    return re.sub(r"\[([^\]]+)\]\(([^)]+)\)", r'<a href="\2">\1</a>', value)


def narrative(text):
    blocks = []
    for block in text.strip().split("\n\n"):
        if block.startswith("# "):
            continue
        if block.startswith("## "):
            blocks.append(f"<h2>{inline(block[3:])}</h2>")
        else:
            blocks.append(f"<p>{inline(block)}</p>")
    return "\n".join(blocks)


def shell(title, body, active="", prefix="", body_class=""):
    groups = [("UNDERSTAND", [("01-product", "The product"), ("02-roles-and-trust", "Roles & trust")]),
              ("EXPLORE", [("03-financing-to-offer", "Financing to offer"), ("04-four-party-transfer", "Four-party transfer"),
                           ("05-bring-an-application", "Bring an application"), ("06-compose-a-workflow", "Compose a workflow")]),
              ("VERIFY", [("07-evidence-and-boundaries", "Evidence & boundaries"), ("08-release-and-adoption", "Release & adoption")])]
    nav = [f'<a class="nav-link {"active" if active == "overview" else ""}" href="{prefix}book-overview.html">Overview</a>']
    for label, chapters in groups:
        nav.append(f'<p class="nav-label">{label}</p>')
        for slug, name in chapters:
            href = f"{prefix}book-chapter.html" if slug.startswith("03") else f"{prefix}chapters/{slug}.html"
            nav.append(f'<a class="nav-link {"active" if active == slug else ""}" href="{href}"><span>{slug[:2]}</span>{escape(name)}</a>')
    return f'''<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>{escape(title)} · Harmonia</title><link rel="stylesheet" href="{prefix}style.css"><script src="{prefix}book.js" defer></script></head>
<body class="{body_class}"><a class="skip-link" href="#main">Skip to content</a>
<aside class="rail"><a class="brand" href="{prefix}book-overview.html"><span class="mark" aria-hidden="true"></span><span class="brand-name">Harmonia</span><span class="brand-caption">THE FIELD GUIDE</span></a>
<button class="mobile-menu" aria-expanded="false" aria-controls="chapter-nav">Chapters</button><nav id="chapter-nav" aria-label="Book chapters">{"".join(nav)}</nav>
<div class="rail-footer"><a href="{prefix}coverage.html">Original documents ↗</a></div></aside>
<header class="topbar"><span>The book &nbsp; / &nbsp; {escape(title)}</span><span class="edition">0.2 · DESIGN PREVIEW</span></header>
{body}</body></html>\n'''


def load_corpus(root):
    pins = json.loads((root / EDITION / "sources.json").read_text())
    corpus = {}
    for name, pin in pins.items():
        data = (root / pin["path"]).read_bytes()
        if sha256(data).hexdigest() != pin["sha256"]:
            raise ValueError(f"Original source changed: {name}; review before updating the pin")
        text = data.decode("utf-8")
        units = source_units(name, text)
        if not units:
            raise ValueError(f"Empty source: {name}")
        corpus[name] = (pin, text, units)
    return corpus


def validate_assignments(corpus, passages, chapters):
    assignments = {}
    for passage in passages:
        if passage.chapter not in chapters:
            raise ValueError(f"Missing chapter: {passage.chapter}")
        if passage.end > len(corpus[passage.source][1].splitlines()):
            raise ValueError(f"Range exceeds source: {passage.title}")
    for name, (_, _, units) in corpus.items():
        source_passages = [p for p in passages if p.source == name]
        for before, after in zip(sorted(source_passages, key=lambda p: p.start), sorted(source_passages, key=lambda p: p.start)[1:]):
            if before.end >= after.start:
                raise ValueError(f"Overlapping source ranges: {before.title}, {after.title}")
        for unit in units:
            owners = [p for p in source_passages if p.start <= unit.line <= p.end]
            if len(owners) != 1:
                raise ValueError(f"Expected one destination for {unit.id}; found {len(owners)}")
            assignments[unit.id] = owners[0]
    return assignments


def quote_passage(passage, units):
    selected = [u for u in units if passage.start <= u.line <= passage.end]
    quote = "\n".join(f'<span class="source-unit" data-unit="{u.id}">{escape(u.text)}</span>' for u in selected)
    label = "Original proposal" if passage.source == "proposal" else "Original architecture"
    return f'''<details class="source-section" id="{passage.source}-L{passage.start}"><summary>{escape(passage.title)}</summary>
<a class="citation" href="../sources/{passage.source}.html#L{passage.start}">{label} · lines {passage.start}–{passage.end} ↗</a>
<blockquote class="source-quotes">{quote}</blockquote></details>'''


def claim_rows(root):
    rows = []
    for line in (root / "docs/0.2/product-contract.md").read_text().splitlines():
        if re.match(r"\| C\d\d \|", line):
            rows.append([cell.strip() for cell in line.strip("|").split("|")])
    return rows


def build_outputs(root=ROOT):
    corpus = load_corpus(root)
    passages = read_passages((root / EDITION / "coverage-map.md").read_text())
    chapters = {path.stem: path.read_text() for path in sorted((root / EDITION / "chapters").glob("*.md"))}
    assignments = validate_assignments(corpus, passages, chapters)
    outputs = {}
    report = {"schema": 1, "scope": "Quotation inclusion, not product implementation or adoption", "documents": [], "chapters": []}
    for name, (pin, text, units) in corpus.items():
        report["documents"].append({"id": name, **pin, "units": len(units), "quoted_units": len(units),
                                    "words": sum(u.words for u in units), "quoted_words": sum(u.words for u in units),
                                    "unit_kind": "nonblank Markdown source lines" if name == "proposal" else "visible HTML body text nodes"})
        lines = "\n".join(f'<span class="source-line" id="L{n}"><a href="#L{n}">{n}</a>{escape(line)}</span>' for n, line in enumerate(text.splitlines(), 1))
        content = f'<main id="main" class="content"><h1>Original {name}</h1><p class="note">Pinned SHA-256: {pin["sha256"]}</p><p><a href="../../../{pin["path"]}">Open the intact original file ↗</a></p><div class="source-view">{lines}</div></main>'
        outputs[f"sources/{name}.html"] = shell(f"Original {name}", content, prefix="../")
    for slug, text in chapters.items():
        title = text.splitlines()[0][2:]
        chapter_passages = [p for p in passages if p.chapter == slug]
        quotes = "\n".join(quote_passage(p, corpus[p.source][2]) for p in chapter_passages)
        count = sum(p.chapter == slug for p in assignments.values())
        report["chapters"].append({"id": slug, "title": title, "quoted_units": count})
        demo = '<p><a class="button primary" href="../book-chapter.html">Explore the interactive chapter →</a></p>' if slug.startswith("03") else ''
        if slug.startswith("06") or slug.startswith("05"):
            demo = '<p><a class="button primary" href="../application.html">Explore the application design →</a></p>'
        body = f'<main id="main" class="content article"><p class="eyebrow">{"APPENDIX" if slug[:2] in {"09", "10"} else "CHAPTER " + slug[:2]} · SOURCE-CITED DESIGN EDITION</p><h1>{escape(title)}</h1>{narrative(text)}{demo}<h2 id="quotations">The original words</h2><p class="note">Open a passage to read its full quotation. Source locations and fingerprints remain inspectable. Quoted proposal statements are not claims of completed implementation.</p>{quotes}<div class="appendices"><a href="../coverage.html">View document coverage and all chapter destinations →</a><a href="09-proposal-context.html">Appendix: proposal context</a><a href="10-context-and-references.html">Appendix: context and references</a></div></main>'
        outputs[f"chapters/{slug}.html"] = shell(title, body, active=slug, prefix="../")
    docs = report["documents"]
    rows = ''.join(f'<tr><td><a href="sources/{d["id"]}.html">{d["id"].title()}</a> / <span>{d["unit_kind"]}</span></td><td>{d["quoted_units"]} / {d["units"]}</td><td>{d["quoted_words"]:,} / {d["words"]:,}</td><td>100%</td></tr>' for d in docs)
    destinations = ''.join(f'<tr><td><a href="chapters/{c["id"]}.html">{c["id"][:2]} · {escape(c["title"])}</a></td><td>{c["quoted_units"]}</td></tr>' for c in report["chapters"])
    claims = claim_rows(root)
    claim_table = ''.join('<tr>' + ''.join(f'<td>{escape(cell)}</td>' for cell in row) + '</tr>' for row in claims)
    content = f'''<main id="main" class="content article"><p class="eyebrow">READ THE CLAIM. INSPECT THE SOURCE.</p><h1>Every passage has a place.</h1>
<p class="lead">Trace the original proposal and architecture into the book.<br>Keep the words, their interpretation, and the evidence distinguishable.</p>
<div class="kpis"><div class="kpi"><strong>100%</strong><span>Textual quotation coverage</span></div><div class="kpi"><strong>{sum(d["units"] for d in docs)}</strong><span>Unique source units quoted</span></div><div class="kpi"><strong>{len(claims)}</strong><span>Product claims tracked separately</span></div></div>
<p class="status-note">This score measures exact quotation inclusion in the chapter source panels. It does not measure implementation completeness, quality of explanation, or external adoption.</p>
<table class="coverage-table"><thead><tr><th>Original</th><th>Quoted units</th><th>Quoted source words</th><th>Coverage</th></tr></thead><tbody>{rows}</tbody></table>
<h2>What the denominator includes</h2><p>Every nonblank Markdown source line, including headings, tables, diagram code and links. Every visible HTML body text node, including SVG labels.</p>
<p class="note">Markup, CSS, scripts and comments are excluded. Missing diagram attachments remain missing. Both original files are available intact.</p>
<p><a href="coverage.json">Inspect the machine-readable report</a> · <a href="../../book/edition-0.2/coverage-map.md">Read the authored coverage map</a></p>
<h2>Chapter destinations</h2><table class="coverage-table"><thead><tr><th>Chapter or appendix</th><th>Unique quoted units</th></tr></thead><tbody>{destinations}</tbody></table>
<details class="source-section"><summary>Exact counting rules and exclusions</summary><p>HTML entities are decoded and surrounding text-node whitespace is trimmed. Document-head metadata and whitespace-only units are excluded. Words are Unicode word tokens counted once per source position. Four referenced BPMN/SVG files were absent from the download. Linked third-party pages are outside this two-document corpus. All source fingerprints are available in the report and source views.</p></details><h2>Product evidence stays separate</h2><p>These are assessments of the fourth-draft baseline. No runtime evidence is upgraded by this design build. Open questions remain visible.</p>
<table class="coverage-table"><thead><tr><th>ID</th><th>Claim</th><th>Original source</th><th>Baseline assessment</th><th>Next evidence</th></tr></thead><tbody>{claim_table}</tbody></table></main>'''
    outputs["coverage.html"] = shell("Source coverage", content, body_class="coverage-page")
    outputs["coverage.json"] = json.dumps(report, ensure_ascii=False, indent=2) + "\n"
    templates = root / EDITION / "templates"
    outputs["book-overview.html"] = shell("Overview", (templates / "overview.html").read_text(), active="overview")
    outputs["book-chapter.html"] = shell("03 / Financing to an offer", (templates / "chapter.html").read_text(), active="03-financing-to-offer", body_class="chapter-preview")
    return outputs


def main():
    outputs = build_outputs()
    for name, text in outputs.items():
        path = ROOT / SITE / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text)
    report = json.loads(outputs["coverage.json"])
    for doc in report["documents"]:
        print(f'{doc["id"]}: {doc["quoted_units"]}/{doc["units"]} units; {doc["quoted_words"]}/{doc["words"]} words')
    print(f'Built {len(outputs)} artifacts for {len(report["chapters"])} chapter destinations.')


if __name__ == "__main__":
    main()
