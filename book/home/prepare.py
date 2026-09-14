"""Prepare the website's recording from its committed portable package."""
from pathlib import Path
import hashlib
import zipfile

root = Path(__file__).resolve().parents[2]
with zipfile.ZipFile(root / 'book/investor/canton-demo.zip') as archive:
    manifest = archive.read('harmonia-canton-demo/SHA256SUMS').decode().splitlines()
    expected = {name: digest for digest, name in (line.split('  ', 1) for line in manifest)}
    contents = archive.read('harmonia-canton-demo/events.json')
    if hashlib.sha256(contents).hexdigest() != expected['events.json']:
        raise ValueError('Demo package checksum mismatch: events.json')
    target = root / 'book/investor/recorded/events.json'
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(contents)
print('Prepared the recorded demos at ' + str(target))
