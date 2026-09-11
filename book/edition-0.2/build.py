#!/usr/bin/env python3
"""Build the source-cited design edition. No product code or network is involved."""
from dataclasses import dataclass
from hashlib import sha256
from html import escape
from html.parser import HTMLParser
from pathlib import Path
import json
import re

import pages
import workspace
from render import diagram_assets

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


def claim_rows(root):
    rows = []
    for line in (root / "docs/0.2/product-contract.md").read_text().splitlines():
        if re.match(r"\| C\d\d \|", line):
            rows.append([cell.strip() for cell in line.strip("|").split("|")])
    return rows


def load_recordings(root):
    directory = root / EDITION / "recordings"
    manifest = json.loads((directory / "manifest.json").read_text())
    recordings = {}
    for key, pin in manifest["stories"].items():
        data = (directory / pin["file"]).read_bytes()
        if sha256(data).hexdigest() != pin["sha256"]:
            raise ValueError(f"Recording changed: {key}")
        story = json.loads(data)
        for kind in ["input", "expected"]:
            digest = sha256((root / "examples/stories" / key / f"{kind}.md").read_bytes()).hexdigest()
            if digest != pin[f"{kind}_sha256"] or digest != story["provenance"][f"{kind}_sha256"]:
                raise ValueError(f"Recording no longer matches committed {kind}: {key}")
        if story["provenance"]["revision"] != pin["revision"] or story["expected"] != story["actual"] or not story["provenance"]["matched"]:
            raise ValueError(f"Recording does not support its stated result: {key}")
        units = story["presentation"]["units"]
        if [u["actual"] for u in units] != story["actual"]["actions"] or [u["expected"] for u in units] != story["expected"]["actions"]:
            raise ValueError(f"Playback differs from the recorded golden comparison: {key}")
        if len(units) != len(story["input"]["actions"]):
            raise ValueError(f"Playback does not account for every supplied action: {key}")
        recordings[key] = story
    return recordings


def build_outputs(root=ROOT):
    corpus = load_corpus(root)
    passages = read_passages((root / EDITION / "coverage-map.md").read_text())
    chapters = {path.stem: path.read_text() for path in sorted((root / EDITION / "chapters").glob("*.md"))}
    assignments = validate_assignments(corpus, passages, chapters)
    recordings = load_recordings(root)
    outputs = diagram_assets(corpus["architecture"][1])
    report = {"schema": 1, "scope": "Quotation inclusion, not product implementation or adoption", "documents": [], "chapters": []}
    for name, (pin, text, units) in corpus.items():
        report["documents"].append({"id": name, **pin, "units": len(units), "quoted_units": len(units),
                                    "words": sum(u.words for u in units), "quoted_words": sum(u.words for u in units),
                                    "unit_kind": "nonblank Markdown source lines" if name == "proposal" else "visible HTML body text nodes"})
        outputs[f"sources/{name}.html"] = pages.source_page(name, text, pin)
    for slug, text in chapters.items():
        count = sum(p.chapter == slug for p in assignments.values())
        report["chapters"].append({"id": slug, "title": pages.CHAPTERS[slug], "quoted_units": count})
        stories = {key: value for key,value in recordings.items() if (slug.startswith("03") and key.startswith("purchase")) or (slug.startswith("04") and key.startswith("transfer"))}
        outputs[f"chapters/{slug}.html"] = pages.chapter(slug, text, passages, corpus, stories)
    outputs["recordings.js"] = "window.HarmoniaRecordings = " + pages.json_script(recordings) + ";\n"
    outputs["sandbox.html"] = pages.sandbox()
    outputs["coverage.html"] = pages.coverage(report, claim_rows(root))
    outputs["coverage.json"] = json.dumps(report, ensure_ascii=False, indent=2) + "\n"
    outputs["review.html"] = pages.review()
    outputs["book-overview.html"] = pages.welcome()
    outputs["application.html"] = workspace.application()
    outputs["application-builder.html"] = workspace.builder()
    outputs["book-chapter.html"] = '''<!doctype html><html lang="en"><head><meta charset="utf-8"><title>Alice buys a home · Harmonia</title><script src="chapter-redirect.js" defer></script></head><body><p>This chapter has one home: <a href="chapters/03-financing-to-offer.html">Alice buys a home</a>.</p></body></html>\n'''
    return {name: "\n".join(line.rstrip() for line in text.splitlines()) + "\n" for name, text in outputs.items()}


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
