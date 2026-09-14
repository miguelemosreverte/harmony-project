"""Render the architecture chapter directly from its Markdown source."""
from pathlib import Path

from markdown_it import MarkdownIt


folder = Path(__file__).resolve().parent
content = MarkdownIt("commonmark", {"html": False}).render(
    (folder / "architecture.md").read_text()
)
template = (folder / "architecture.template.html").read_text()
(folder / "architecture.html").write_text(template.replace("{{content}}", content))
