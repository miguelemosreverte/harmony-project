"""Render the reviewed Markdown as slides without duplicating its prose."""
from html import escape
from pathlib import Path
import hashlib
import re
import struct

from markdown_it import MarkdownIt

folder = Path(__file__).resolve().parent
root = folder.parent.parent
source = root / "book/navigation/explorations/architecture-structure/architecture.md"
markdown = MarkdownIt("commonmark", {"html": False})
tokens = markdown.parse(source.read_text())
starts = [i for i, token in enumerate(tokens) if token.type == "heading_open" and token.tag == "h2"]
ending = next(i for i, token in enumerate(tokens) if token.type == "hr")
slides = []


def render(items):
    return markdown.renderer.render(items, markdown.options, {})


for index, start in enumerate(starts):
    stop = starts[index + 1] if index + 1 < len(starts) else ending
    section = tokens[start:stop]
    title = section[1].content
    body = section[3:]
    figures = [i for i, token in enumerate(body) if token.type == "inline"
               and token.children and len(token.children) == 1
               and token.children[0].type == "image"]
    if len(figures) != 1:
        raise ValueError(f"Each passage needs one standalone image: {title}")
    image_index = figures[0]
    picture = body[image_index].children[0]
    image_path = (source.parent / picture.attrGet("src")).resolve()
    image_bytes = image_path.read_bytes()
    width, height = struct.unpack(">II", image_bytes[16:24])
    digest = hashlib.sha256(image_bytes).hexdigest()[:12]
    image_url = "../" + str(image_path.relative_to(root / "book")) + "?v=" + digest
    intro = render(body[:image_index - 1])
    after = render(body[image_index + 2:])
    slides.append(f'''<section class="architecture-slide" data-step="{index}" role="group"
      aria-roledescription="slide" aria-labelledby="slide-title-{index}">
      <h2 id="slide-title-{index}">{escape(title)}</h2>
      <div class="slide-intro">{intro}</div>
      <figure><img src="{escape(image_url)}" alt="{escape(picture.content)}" width="{width}" height="{height}" decoding="async" draggable="false"></figure>
      <div class="slide-explanation">{after}</div>
    </section>''')

template = (folder / "index.template.html").read_text()
result = template.replace("{{slides}}", "\n".join(slides))
result = result.replace("{{sources}}", render(tokens[ending + 1:]))


def asset(match):
    relative = match.group(1)
    digest = hashlib.sha256((folder / relative).read_bytes()).hexdigest()[:12]
    return relative + "?v=" + digest


result = re.sub(r"\{\{asset:([^}]+)\}\}", asset, result)
(folder / "index.html").write_text(result)
print(f"Rendered {len(slides)} architecture slides from {source.relative_to(root)}")
