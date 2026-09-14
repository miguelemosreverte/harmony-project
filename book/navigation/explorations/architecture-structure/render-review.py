"""Render a versioned review from its Markdown, template and image."""
import argparse
from hashlib import sha256
from pathlib import Path

from markdown_it import MarkdownIt


parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("version", type=int, help="Architecture review version")
version = parser.parse_args().version
folder = Path(__file__).resolve().parent


def fingerprint(path: str) -> str:
    return sha256((folder / path).read_bytes()).hexdigest()[:12]


values = {
    "markdown": MarkdownIt("commonmark", {"html": False}).render(
        (folder / f"read-v{version}.md").read_text()
    ),
    "css_hash": fingerprint("review.css"),
    "image_hash": fingerprint(f"{version:02}-structure.png"),
    "script_hash": fingerprint("../../sheet.js"),
    "review_hash": fingerprint("review.js"),
}
page = (folder / f"v{version}.template.html").read_text()
for key, value in values.items():
    page = page.replace("{{" + key + "}}", value)
(folder / f"v{version}.html").write_text(page)
