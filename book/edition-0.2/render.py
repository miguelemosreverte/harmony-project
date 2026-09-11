"""Readable source rendering; exact source quotations remain a separate audit view."""
from html import escape
from html.parser import HTMLParser
from pathlib import Path
import re

try:
    from markdown_it import MarkdownIt
except ImportError as error:
    raise SystemExit("Run scripts/build-design to install the pinned book renderer.") from error


def markdown(text, source=False):
    parser = MarkdownIt("commonmark", {"html": False, "typographer": False}).enable("table")
    tokens = parser.parse(text)
    for token in tokens:
        if source and token.type == "heading_open":
            token.tag = "h3" if int(token.tag[1]) <= 3 else "h4"
        if source and token.type == "heading_close":
            token.tag = "h3" if int(token.tag[1]) <= 3 else "h4"
        for child in token.children or []:
            if child.type == "image" and source:
                child.type = "html_inline"
                child.content = '<span class="missing-asset">Original diagram attachment was not included in the download.</span>'
            if child.type == "link_open" and source:
                href = child.attrGet("href") or ""
                if href.startswith("assets/"):
                    child.attrSet("href", "../sources/proposal.html?view=source")
    rendered = parser.renderer.render(tokens, parser.options, {})
    rendered = rendered.replace('<pre><code class="language-plantuml">', '<details class="diagram-code"><summary>Read the original diagram program</summary><pre><code class="language-plantuml">')
    rendered = re.sub(r'(<details class="diagram-code">.*?</pre>)', r'\1</details>', rendered, flags=re.S)
    return rendered


def diagram(asset, title):
    return f'<figure class="source-diagram"><div class="diagram-scroll" tabindex="0" aria-label="Scrollable {escape(title)}"><img src="../assets/{asset}.svg" alt="{escape(title)}"></div><figcaption>{escape(title)} · <a href="../assets/{asset}.svg">Open full diagram ↗</a></figcaption></figure>'


class Fragment(HTMLParser):
    """Retain document semantics without importing the original page's CSS globally."""
    tags = {"p", "div", "span", "h1", "h2", "h3", "h4", "table", "thead", "tbody", "tr", "td", "th", "ul", "ol", "li", "strong", "em", "code", "a", "br"}

    def __init__(self):
        super().__init__(convert_charrefs=True)
        self.skip = 0
        self.stack = []
        self.output = []

    def handle_starttag(self, tag, attrs):
        if tag in {"head", "style", "script", "svg"}:
            self.skip += 1
            return
        if self.skip or tag not in self.tags:
            return
        mapped = "h3" if tag.startswith("h") else tag
        href = dict(attrs).get("href", "")
        attributes = f' href="{escape(href, quote=True)}"' if tag == "a" and not href.lower().startswith("javascript:") else ""
        self.output.append(f"<{mapped}{attributes}>")
        if tag != "br":
            self.stack.append((tag, mapped))

    def handle_endtag(self, tag):
        if tag in {"head", "style", "script", "svg"}:
            self.skip = max(0, self.skip - 1)
            return
        if self.skip:
            return
        if any(original == tag for original, _ in self.stack):
            while self.stack:
                original, mapped = self.stack.pop()
                self.output.append(f"</{mapped}>")
                if original == tag:
                    break

    def handle_data(self, data):
        if not self.skip:
            self.output.append(escape(data))

    def rendered(self):
        return "".join(self.output) + "".join(f"</{mapped}>" for _, mapped in reversed(self.stack))


def html_fragment(text):
    parser = Fragment()
    parser.feed(text)
    return parser.rendered()


def diagram_assets(source):
    """Extract the two supplied SVGs with their own styles, in isolated image files."""
    style = re.search(r"<style>(.*?)</style>", source, re.S).group(1)
    assets = {}
    for name, svg in zip(["component-map", "contract-model"], re.findall(r"<svg\b.*?</svg>", source, re.S)):
        svg = svg.replace("<svg ", '<svg xmlns="http://www.w3.org/2000/svg" ', 1) if "xmlns=" not in svg.split(">", 1)[0] else svg
        # HTML permits decorative comments that XML SVG rejects. Comments carry no diagram content.
        svg = re.sub(r"<!--.*?-->", "", svg, flags=re.S)
        end = svg.index(">") + 1
        assets[f"assets/{name}.svg"] = svg[:end] + f'<style>{style}</style><rect width="100%" height="100%" fill="#111823"/>' + svg[end:]
    if len(assets) != 2:
        raise ValueError("Expected both original architecture diagrams")
    return assets
