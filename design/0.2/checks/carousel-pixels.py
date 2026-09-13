#!/usr/bin/env python3
"""Compare actual stage screenshots; movement and changed area are separate measurements."""
import json
from pathlib import Path
from PIL import Image, ImageChops

report = json.loads(Path('docs/0.2/stable-carousel.json').read_text())
previous = {}
pairs = []
for frame in report['frames']:
    image = Image.open('/tmp/harmonia-carousel-pixels/' + frame['file'] + '.png').convert('RGB')
    if frame['group'] in previous:
        before, old = previous[frame['group']]
        assert image.size == old.size, (before['id'], frame['id'], 'stage resized')
        differences = ImageChops.difference(old, image)
        pixels = sum(max(pixel) > 12 for pixel in differences.getdata())
        assert pixels >= 150, (before['id'], frame['id'], 'imperceptible change')
        pairs.append({'from': before['id'], 'to': frame['id'], 'width': image.width,
                      'changed_pixels': pixels, 'changed_percent': round(100*pixels/(image.width*image.height),3)})
    previous[frame['group']] = frame, image
result = {'scope':'Actual infographic pixels, including the compact state badge. Changes smaller than 13/255 per channel are ignored.',
          'max_landmark_drift_css_px':max(frame['drift'] for frame in report['frames']),
          'pairs':pairs}
Path('docs/0.2/carousel-pixels.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps({'comparisons':len(pairs),'max_landmark_drift_css_px':result['max_landmark_drift_css_px'],
                  'changed_percent_range':[min(p['changed_percent'] for p in pairs),max(p['changed_percent'] for p in pairs)]}))
