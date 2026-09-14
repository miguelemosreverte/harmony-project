// Sixteen deliberately composed drawings. Shared shapes provide a consistent visual vocabulary.
const group = (x, y, content, scale = 1) => `<g transform="translate(${x} ${y}) scale(${scale})">${content}</g>`;
const text = (x, y, value, css = '') => `<text x="${x}" y="${y}" class="${css}">${value}</text>`;
const line = (x, y, to, yy = y, css = '') => `<path d="M${x} ${y}H${to}" class="${css}"/><path d="M${to-8} ${yy-6}l8 6-8 6"/>`;
const tick = (x, y, scale = 1) => group(x, y, '<path d="m0 8 8 8L26 0" class="accent-mark"/>', scale);
const lock = (x, y, scale = 1) => group(x, y, '<path d="M9 20V12a11 11 0 0 1 22 0v8"/><rect x="3" y="20" width="34" height="32" rx="5" class="blue"/><circle cx="20" cy="32" r="3" class="ink"/><path d="M20 35v8"/>', scale);
const paper = (x, y, scale = 1, approved = false) => group(x, y,
  '<path d="M0 0h59l21 21v83H0Z" class="white"/><path d="M59 0v21h21"/><path d="M15 37h49M15 51h40M15 65h27" class="light-stroke"/>' +
  (approved ? '<circle cx="67" cy="88" r="19" class="orange"/>' + group(55,80,'<path d="m0 7 7 7L22 0" class="white-stroke"/>') : ''), scale);
const person = (x, y, name = '', woman = false, scale = 1) => group(x, y,
  (woman ? '<path d="M7 43Q-3-7 36-7T64 47l-8 20H12Z" class="ink"/>' : '') +
  '<path d="M6 102V78Q7 56 34 56T64 78v24" class="blue"/><path d="m22 59 12 18 12-18"/>' +
  '<path d="M11 23Q11 1 34 1T57 23v10Q56 55 34 55T11 33Z" class="white"/>' +
  (woman ? '<path d="M10 23Q36 24 41 5Q57 9 57 30V10Q35-10 13 6Z" class="ink"/>' : '<path d="M9 23Q26 21 32 7Q44 20 59 17V5Q40-10 18 0Q5 7 9 23Z" class="ink"/>') +
  '<circle cx="25" cy="29" r="1.7" class="ink"/><circle cx="44" cy="29" r="1.7" class="ink"/><path d="M29 43q6 4 12-1" class="fine"/>' +
  (name ? text(34,128,name,'center') : ''), scale);
const bank = (x, y, scale = 1, label = 'Bank') => group(x, y,
  '<path d="M0 41 75 0l75 41v12H0Z" class="blue"/><path d="M13 42h124"/><path d="M10 53h130v14H10Z" class="white"/>' +
  [23,66,109].map(x=>`<path d="M${x} 68h18v79h-18Z" class="blue"/>`).join('') +
  '<path d="M8 148h135v14H8Z" class="white"/>' + (label ? text(75,185,label,'center') : ''),scale);
const house = (x,y,scale=1) => group(x,y,
  '<path d="M0 71 77 5l78 66" class="white"/><path d="M15 64v102h125V64L77 13Z" class="blue"/><path d="M64 166v-55h29v55" class="white"/>' +
  '<path d="M33 79h20v22H33ZM106 79h20v22h-20Z" class="white"/>' + text(77,188,'Property','center'),scale);
const dots = (x,y,scale=1) => group(x,y,'<path d="M0 0h78"/><circle r="12" class="blue"/><circle cx="39" r="12" class="blue"/><circle cx="78" r="12" class="white"/>',scale);
const laptop = (x,y,scale=1,closed=false) => group(x,y,closed
  ? '<path d="m0 64 90-2 25 15H-12Z" class="blue"/><path d="M-12 78h127v6H-12Z" class="white"/>'
  : '<rect width="105" height="71" rx="6" class="blue"/><path d="M8 8h89v55H8Z" class="white"/><path d="m0 73-12 11h129l-12-11Z" class="blue"/>' + dots(23,36,.75),scale);
const book = (x,y,scale=1) => group(x,y,'<path d="M9 0h94v128H9Q0 128 0 119V10Q0 0 9 0Z" class="blue"/><path d="M12 0v110h91M12 110q-15 0-11 14"/><path d="M69 0v42l10-8 10 8V0" class="orange"/>' + dots(24,76,.77),scale);
const folder = (x,y,label,scale=1) => group(x,y,'<path d="M0 17V0h39l14 17h72v73H0Z" class="blue"/><path d="M0 27h125"/>' + text(62,59,label,'center mono'),scale);
const boundary = (x,y,w,h,label) => `<rect x="${x}" y="${y}" width="${w}" height="${h}" rx="14" class="zone"/>${text(x+16,y+26,label,'small')}`;
const stamp = (x,y,scale=1) => group(x,y,
  '<path d="m0 4 61 5 6 24-24 9-7-12-9 10-9-7 9-15H0Z" class="white"/><path d="m62 7 37-7 6 32-37 4Z" class="blue"/>' +
  '<path d="M34 33q-8 3-7 12l5 18H19v12h57V63H58l4-18q0-12-13-12Z" class="orange"/>',scale);
const svg = (content, label) => `<svg viewBox="0 0 460 260" role="img" aria-label="${label}" xmlns="http://www.w3.org/2000/svg"><g>${content}</g></svg>`;

const drawings = {
  owners: () => boundary(16,23,205,213,'') + boundary(239,23,205,213,'') + bank(62,39,.84) + house(285,37,.84) + lock(173,169,.8) + lock(392,169,.8),
  plan: () => person(32,54,'Owner',false,1.1) + person(348,54,'Partner',true,1.1) +
    '<path d="m158 119 157 0 19 82H139Z" class="white"/><path d="m105 126 50 12m195-12-35 12"/>' + dots(183,157,1.12) + '<path d="M232 62v17m-34-11 9 14m58-14-9 14" class="accent-mark cue"/>',
  authority: () => boundary(32,19,264,220,'Application owner') + paper(93,113,.86) + stamp(95,57,1.18) +
    '<path d="M423 128h-35m8-8-8 8 8 8" class="accent-mark"/>' +
    '<circle cx="348" cy="128" r="30" class="orange"/><path d="m336 116 24 24m0-24-24 24" class="white-stroke cue"/>' + text(349,196,'No bypass','center'),
  resume: () => laptop(17,93,1.13,true) + line(155,136,184) + book(198,55,1.06) + line(324,136,350) + laptop(362,91,.85) +
    '<path d="M40 104q13-39 53-22m-9-9 10 9-9 7"/>' + text(70,225,'Close','center') + text(252,225,'Retained','center') + text(402,225,'Resume','center'),
  approve: () => boundary(18,22,272,217,'Northbank') + bank(41,53,.55,'') + lock(83,163,.65) + paper(163,99,.83,true) + stamp(166,43,.79) +
    line(290,137,328,137,'travel') + paper(356,103,.72,true) + text(382,218,'Result','center') + '<path d="M95 170h36" class="light-stroke"/>',
  propose: () => person(29,49,'Alice',true,1.1) + paper(145,79,.67,true) + line(217,120,260,120,'travel') +
    '<path d="M293 54h69l24 24v116h-93Z" class="white"/><path d="M362 54v25h24"/>' + group(311,90,'<path d="m0 28 23-23 23 23M7 24v34h32V24" class="light-stroke"/>') + tick(341,168,.8) + text(340,226,'Proposal','center'),
  relay: () => person(37,65,'Ben',false,1.05) + person(353,65,'Sofia',true,1.05) +
    '<path d="M177 92h104v77H177Z" class="blue"/><path d="m177 92 52 40 52-40"/>' +
    line(113,132,158,132,'travel') + line(301,132,341,132,'travel') + text(229,218,'Proposal','center'),
  receive: () => boundary(18,29,145,201,'Northbank') + bank(43,70,.6,'') + lock(120,145,.73) +
    person(343,61,'Sofia',true,1.05) + paper(225,93,.81) + '<path d="M203 187h119l-10 26h-98Z" class="blue"/>' +
    tick(278,93,.8) + '<path d="M243 154v20m-7-7 7 7 7-7" class="accent-mark cue"/>',
  request: () => person(28,65,'Participant',true,1) + laptop(161,60,1.45) +
    '<path d="m222 122 0 39 12-9 9 16 10-6-9-14 15-2Z" class="orange cue"/>' + line(334,111,410,111,'travel') + text(232,218,'Browser','center'),
  submit: () => paper(23,85,.8) + line(104,130,153,130,'travel') + boundary(173,38,180,188,'Scala service') +
    text(263,102,'Approve','center mono') + '<path d="M263 118v27m-7-7 7 7 7-7"/>' + text(263,178,'Choice','center mono') + line(368,130,428,130,'travel'),
  authorize: () => boundary(31,23,397,216,'On-ledger · Daml') + person(62,67,'Bank',false,.91) +
    '<path d="M173 105h48m-8-7 8 7-8 7" class="travel"/>' +
    '<path d="M268 62 319 80v55q-5 37-51 56-46-19-51-56V80Z" class="blue"/>' + tick(247,105,1.7) + text(349,211,'Approve','center mono'),
  observe: () => book(29,55,1.04) + line(157,128,194,128,'travel') + boundary(211,76,90,91,'Scala') +
    line(317,128,348,128,'travel') + laptop(354,94,.85) + text(79,222,'Committed','center') + text(397,222,'Observed','center'),
  feature: () => folder(33,41,'product',1.05) + line(177,86,237,86,'light-stroke') +
    folder(267,40,'server',.93) + '<path d="M325 133v17m-7-7 7 7 7-7" class="light-stroke"/>' +
    folder(267,162,'financing',.93) + '<path d="M251 156h161v94H251Z" class="selection cue"/>',
  types: () => laptop(18,90,.85) + boundary(166,42,129,176,'Shared API') +
    text(231,121,'Action','center mono') + text(231,149,'Result','center mono') +
    line(111,107,149,107,'travel') + '<path d="M149 148h-38m8-6-8 6 8 6" class="travel"/>' +
    boundary(353,71,88,123,'Scala') + line(309,107,339,107,'travel') + '<path d="M339 148h-30m8-6-8 6 8 6" class="travel"/>' + text(61,214,'Browser','center'),
  choice: () => '<rect x="27" y="27" width="405" height="207" rx="13" class="zone"/><path d="M27 69h405"/>' +
    text(48,54,'Financing.daml','mono small') + text(48,104,'choice Approve','mono') +
    '<rect x="42" y="120" width="372" height="35" rx="5" class="blue cue"/>' + text(61,144,'controller bank','mono') +
    text(61,183,'create this with','mono') + text(80,211,'status = "approved"','mono small') + lock(369,167,.55),
  golden: () => paper(30,52,.86) + text(64,172,'input.md','center mono small') +
    line(119,101,158,101,'travel') + '<rect x="177" y="51" width="99" height="109" rx="8" class="zone"/>' + text(226,112,'Run','center') +
    line(292,101,330,101,'travel') + paper(353,53,.81,true) + text(385,172,'actual.md','center mono small') +
    '<rect x="154" y="188" width="145" height="40" rx="5" class="white"/><path d="M385 188v20h-68m8-7-8 7 8 7" class="light-stroke"/>' + text(226,213,'expected.md','center mono small')
};

export function drawing(name, label) {
  if (!drawings[name]) throw Error(`Unknown drawing: ${name}`);
  return svg(drawings[name](), label);
}
