"""Package a captured replay without depending on a ledger or rebuilding assets."""
from pathlib import Path
import hashlib
import json
import sys
import zipfile

directory = Path(sys.argv[1] if len(sys.argv) > 1 else '.artifacts/canton-demo').resolve()
names = ['index.html', 'open.html', 'events.json', 'evidence.html']
readme = '''Harmonia recorded Canton demonstrations

1. Unzip this folder.
2. Open index.html in a browser. No installation or network is required.
3. Choose carousel stops, swipe, or use the arrow keys.

Export log saves the observations, visuals and your selected step as JSON.
Open open.html and choose that JSON to restore it, without the repository.
Evidence contains the original milestone mapping, inputs and expectations.

These are recorded local Canton runs, not new ledger submissions.
Run provenance and the exact renderer travel inside events.json.
The included marks identify Canton and Daml; they do not imply a partnership.
'''
payload = {name: (directory / name).read_bytes() for name in names}
payload['README.txt'] = readme.encode()
payload['SHA256SUMS'] = ''.join(
    f'{hashlib.sha256(data).hexdigest()}  {name}\n'
    for name, data in sorted(payload.items())
).encode()
archive = directory.parent / 'harmonia-canton-demo.zip'
with zipfile.ZipFile(archive, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=9) as output:
    for name, data in sorted(payload.items()):
        info = zipfile.ZipInfo('harmonia-canton-demo/' + name, (2026, 1, 1, 0, 0, 0))
        info.compress_type = zipfile.ZIP_DEFLATED
        info.external_attr = 0o100644 << 16
        output.writestr(info, data)
print(json.dumps({'archive': str(archive), 'bytes': archive.stat().st_size,
                  'sha256': hashlib.sha256(archive.read_bytes()).hexdigest()}))
