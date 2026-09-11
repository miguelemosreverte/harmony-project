#!/usr/bin/env python3
"""Check exact rendered quotations, deterministic output, links, and failure cases."""
from collections import Counter
from html import escape
from html.parser import HTMLParser
from pathlib import Path
from tempfile import TemporaryDirectory
from urllib.parse import unquote, urlsplit
import json
import shutil
import unittest
import xml.etree.ElementTree as ET
from unittest.mock import patch
from render import markdown, html_fragment
from atlas.catalog import annotations, colored_lines, build_catalog

import build


class Page(HTMLParser):
    def __init__(self):
        super().__init__(convert_charrefs=True)
        self.quotes = []
        self.active = None
        self.parts = []
        self.links = []
        self.ids = []

    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if "id" in attrs:
            self.ids.append(attrs["id"])
        if "href" in attrs:
            self.links.append(attrs["href"])
        if "src" in attrs:
            self.links.append(attrs["src"])
        if "data-unit" in attrs:
            if self.active is not None:
                raise AssertionError("Nested quote units")
            self.active = attrs["data-unit"]
            self.parts = []

    def handle_data(self, data):
        if self.active is not None:
            self.parts.append(data)

    def handle_endtag(self, tag):
        if tag == "span" and self.active is not None:
            self.quotes.append((self.active, "".join(self.parts)))
            self.active = None


def verify_rendered(outputs):
    corpus = build.load_corpus(build.ROOT)
    expected = {unit.id: unit.text for _, _, units in corpus.values() for unit in units}
    observed = []
    for name, text in outputs.items():
        if name.startswith("chapters/"):
            page = Page()
            page.feed(text)
            observed.extend(page.quotes)
    counts = Counter(key for key, _ in observed)
    if set(counts) != set(expected) or any(value != 1 for value in counts.values()):
        raise AssertionError("Missing, duplicate, or unknown quotation units")
    for key, text in observed:
        if text != expected[key]:
            raise AssertionError(f"Altered quotation: {key}")
    report = json.loads(outputs["coverage.json"])
    for document in report["documents"]:
        units = corpus[document["id"]][2]
        if document["quoted_units"] != len(units) or document["quoted_words"] != sum(u.words for u in units):
            raise AssertionError("Coverage report differs from actual quotations")
    return len(observed)


def verify_links():
    pages = {}
    for path in (build.ROOT / build.SITE).rglob("*.html"):
        page = Page()
        page.feed(path.read_text())
        if len(page.ids) != len(set(page.ids)):
            raise AssertionError(f"Duplicate HTML ids: {path}")
        pages[path.resolve()] = page
    checked = 0
    for path, page in pages.items():
        for link in page.links:
            parsed = urlsplit(link)
            if parsed.scheme or parsed.netloc:
                continue
            target = (path.parent / unquote(parsed.path)).resolve() if parsed.path else path
            if not target.is_file():
                raise AssertionError(f"Broken local link in {path.name}: {link}")
            if parsed.fragment and target in pages and unquote(parsed.fragment) not in pages[target].ids:
                # The workspace uses these documented view names as client routes.
                if not (target.name == "application.html" and parsed.fragment in {"overview", "applications", "history"}):
                    raise AssertionError(f"Missing anchor in {path.name}: {link}")
            checked += 1
    return checked


class CoverageChecks(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.outputs = build.build_outputs()
        cls.corpus = build.load_corpus(build.ROOT)
        cls.passages = build.read_passages((build.ROOT / build.EDITION / "coverage-map.md").read_text())
        cls.chapters = {p.stem for p in (build.ROOT / build.EDITION / "chapters").glob("*.md")}

    def test_every_original_unit_is_quoted_once_and_unchanged(self):
        self.assertEqual(verify_rendered(self.outputs), 736)

    def test_committed_preview_matches_fresh_build(self):
        for name, text in self.outputs.items():
            self.assertEqual((build.ROOT / build.SITE / name).read_text(), text, name)

    def test_links_and_source_anchors_resolve(self):
        self.assertGreater(verify_links(), 100)

    def test_omitted_passage_fails(self):
        with self.assertRaisesRegex(ValueError, "Expected one destination"):
            build.validate_assignments(self.corpus, self.passages[1:], self.chapters)

    def test_duplicate_passage_fails(self):
        with self.assertRaisesRegex(ValueError, "Overlapping"):
            build.validate_assignments(self.corpus, self.passages + [self.passages[0]], self.chapters)

    def test_missing_destination_fails(self):
        with self.assertRaisesRegex(ValueError, "Missing chapter"):
            build.validate_assignments(self.corpus, self.passages, self.chapters - {self.passages[0].chapter})

    def test_changed_original_fails(self):
        with TemporaryDirectory() as directory:
            root = Path(directory)
            (root / build.EDITION).mkdir(parents=True)
            shutil.copy(build.ROOT / build.EDITION / "sources.json", root / build.EDITION / "sources.json")
            for pin, _, _ in self.corpus.values():
                target = root / pin["path"]
                target.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy(build.ROOT / pin["path"], target)
            proposal = root / "docs/proposal/harmonia.md"
            proposal.write_text(proposal.read_text().replace("without bespoke", "with bespoke", 1))
            with self.assertRaisesRegex(ValueError, "Original source changed"):
                build.load_corpus(root)

    def test_altered_rendered_quote_fails(self):
        outputs = dict(self.outputs)
        name = "chapters/01-product.html"
        outputs[name] = outputs[name].replace('data-unit="proposal-L13">' + escape(next(u.text for u in self.corpus["proposal"][2] if u.id == "proposal-L13")), 'data-unit="proposal-L13">ALTERED', 1)
        with self.assertRaisesRegex(AssertionError, "Altered quotation"):
            verify_rendered(outputs)

    def test_removed_rendered_quote_fails(self):
        outputs = dict(self.outputs)
        name = "chapters/01-product.html"
        outputs[name] = outputs[name].replace('data-unit="proposal-L13"', 'data-missing="proposal-L13"', 1)
        with self.assertRaisesRegex(AssertionError, "Missing, duplicate"):
            verify_rendered(outputs)

    def test_duplicate_rendered_quote_fails(self):
        outputs = dict(self.outputs)
        outputs["chapters/01-product.html"] += '<span data-unit="proposal-L13">duplicated text</span>'
        with self.assertRaisesRegex(AssertionError, "Missing, duplicate"):
            verify_rendered(outputs)

    def test_report_cannot_inflate_word_coverage(self):
        outputs = dict(self.outputs)
        report = json.loads(outputs["coverage.json"])
        report["documents"][0]["quoted_words"] += 1
        outputs["coverage.json"] = json.dumps(report)
        with self.assertRaisesRegex(AssertionError, "Coverage report differs"):
            verify_rendered(outputs)

    def test_readable_markdown_has_semantic_structure(self):
        rendered = markdown("### Heading\n\n- First\n- Second\n\n| Action | Actor |\n| --- | --- |\n| Assess | Bank |\n\n```plantuml\nAlice -> Bank\n```", source=True)
        self.assertIn("<h3>Heading</h3>", rendered)
        self.assertIn("<ul>", rendered)
        self.assertIn("<table>", rendered)
        self.assertIn('class="diagram-code"', rendered)
        self.assertNotIn("### Heading", rendered)
        self.assertEqual(rendered.count("<details"), rendered.count("</details>"))

    def test_exported_diagrams_are_valid_svg_images(self):
        for name in ["assets/component-map.svg", "assets/contract-model.svg"]:
            svg = ET.fromstring(self.outputs[name])
            self.assertEqual(svg.tag, "{http://www.w3.org/2000/svg}svg")
            self.assertGreater(len(list(svg.iter("{http://www.w3.org/2000/svg}text"))), 20)

    def test_missing_attachments_are_explained(self):
        self.assertIn("not included", markdown("![Missing](assets/missing.svg)", source=True))
        self.assertNotIn("<img", markdown("![Missing](assets/missing.svg)", source=True))

    def test_html_styles_do_not_escape_into_the_book(self):
        rendered = html_fragment('<style>body{display:none}</style><h2>Readable</h2><p>A &amp; B</p><svg><text>In isolated diagram</text></svg>')
        self.assertEqual(rendered, "<h3>Readable</h3><p>A &amp; B</p>")

    def test_preserved_recordings_match_current_committed_goldens(self):
        self.assertEqual(len(build.load_recordings(build.ROOT)), 4)

    def test_changed_recording_fails_before_it_can_be_presented(self):
        original = Path.read_bytes
        def altered(path):
            data = original(path)
            return data + b" " if str(path).endswith("recordings/purchase-approved.json") else data
        with patch.object(Path, "read_bytes", altered):
            with self.assertRaisesRegex(ValueError, "Recording changed"):
                build.load_recordings(build.ROOT)

    def test_stale_recorded_expectation_fails(self):
        original = Path.read_bytes
        def altered(path):
            data = original(path)
            return data + b" " if str(path).endswith("stories/purchase-approved/expected.md") else data
        with patch.object(Path, "read_bytes", altered):
            with self.assertRaisesRegex(ValueError, "no longer matches committed expected"):
                build.load_recordings(build.ROOT)

    def test_html_corpus_rules_include_svg_and_entities(self):
        sample = '<html><head><title>Excluded</title><style>.x { color:red; }</style></head><body><h1>A &amp; B</h1><!-- hidden --><svg><text>Actor</text></svg><script>ignore()</script><p> next </p></body></html>'
        self.assertEqual([u.text for u in build.source_units("architecture", sample)], ["A & B", "Actor", "next"])
        self.assertEqual([u.text for u in build.source_units("proposal", "# Heading\n\n  - nested  \n```daml\nmodule A where\n```\n")], ["# Heading", "  - nested  ", "```daml", "module A where", "```"])


class SourceCatalogChecks(unittest.TestCase):
    def test_scalafmt_wrapping_preserves_annotations(self):
        inline="/** @book.slice financing\n  * @book.role Observe\n  * @book.summary Read the participant state.\n  */"
        wrapped="/** @book.slice\n  *   financing\n  * @book.role\n  *   Observe\n  * @book.summary\n  *   Read the participant\n  *   state.\n  */"
        self.assertEqual(annotations(inline,'View.scala'),annotations(wrapped,'View.scala'))
        self.assertEqual(annotations(wrapped,'View.scala')['summary'],'Read the participant state.')

    def test_annotation_examples_in_executable_strings_are_ignored(self):
        self.assertEqual(annotations('val example = "@book.slice imaginary"','View.scala'),{})
        self.assertEqual(annotations('example = "# @book.slice imaginary"','catalog.py'),{})

    def test_invalid_annotation_is_not_silently_accepted(self):
        for text in ['-- @book.slice process', '-- @book.silce process', '-- @book.slice process\n-- @book.slice process']:
            with self.assertRaises(ValueError):annotations(text,'Engine.daml')

    def test_coloring_preserves_exact_source_text(self):
        class Text(HTMLParser):
            def __init__(self):super().__init__(convert_charrefs=True);self.parts=[]
            def handle_data(self,data):self.parts.append(data)
        for language,text in [('scala','val x = "<script>&"\n\n// hello\n'),('daml','template Example\n  with who : Party\n'),('python','value = "< & >"')]:
            actual=[]
            for line in colored_lines(text,language):
                parsed=Text();parsed.feed(line);actual.append(''.join(parsed.parts))
            self.assertEqual(actual,text.splitlines())

    def test_invalid_slice_references_fail_before_export(self):
        with TemporaryDirectory() as directory:
            root=Path(directory);(root/'book/edition-0.2/atlas').mkdir(parents=True);(root/'product').mkdir()
            (root/'product/A.scala').write_text('object A')
            base=dict(id='a',nodes=[dict(id='a',file='product/A.scala',label='A',detail='A file')],edges=[],evidence='product/A.scala')
            manifest=root/'book/edition-0.2/atlas/slices.json'
            for change,message in [({'nodes':[]},'Empty'),({'nodes':[dict(id='a',file='missing.scala')]},'Missing'),({'edges':[['a','unknown']]},'Unknown'),({'edges':[['a','a']]},'Cycle'),({'evidence':'missing.md'},'Missing evidence')]:
                manifest.write_text(json.dumps([{**base,**change}]))
                with self.assertRaisesRegex(ValueError,message):build_catalog(root)
            manifest.write_text(json.dumps([base]))
            (root/'product/A.scala').write_text('/** @book.slice unknown\n * @book.role A\n * @book.summary Source.\n */')
            with self.assertRaisesRegex(ValueError,'Unknown slice'):build_catalog(root)


if __name__ == "__main__":
    unittest.main(verbosity=2)
