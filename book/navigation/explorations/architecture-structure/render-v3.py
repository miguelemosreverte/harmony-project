"""Render the review's committed Markdown beside the generated image."""
from hashlib import sha256
from pathlib import Path

from markdown_it import MarkdownIt


folder = Path(__file__).resolve().parent
values = {
    "markdown": MarkdownIt("commonmark", {"html": False}).render(
        (folder / "read-v3.md").read_text()
    ),
    "css_hash": sha256((folder / "review.css").read_bytes()).hexdigest()[:12],
    "image_hash": sha256((folder / "03-structure.png").read_bytes()).hexdigest()[:12],
    "script_hash": sha256((folder / "../../sheet.js").read_bytes()).hexdigest()[:12],
    "review_hash": sha256((folder / "review.js").read_bytes()).hexdigest()[:12],
}
page = (folder / "v3.template.html").read_text()
for key, value in values.items():
    page = page.replace("{{" + key + "}}", value)
(folder / "v3.html").write_text(page)
