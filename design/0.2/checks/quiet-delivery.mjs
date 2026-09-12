import fs from 'node:fs/promises';
import assert from 'node:assert/strict';
import {createHash} from 'node:crypto';
import {browser} from './cdp.mjs';
const b=await browser(process.argv[2]),base=process.argv[3],product=process.argv[4],checks=[];
const check=(name,value)=>{assert(value,name);checks.push(name);};
try{
 await b.viewport(1280,1100);await b.navigate(new URL('source/design/0.2/author.html?passage=architecture-88',base).href);await b.until("!!document.querySelector('#companion-frame .workflow-node')");check('Separate book pairs original labels with a redrawn infographic',await b.evaluate("!!document.getElementById('passage-architecture-88').querySelector('.original-diagram-labels')&&!document.querySelector('iframe')"));const files=await b.evaluate('HarmoniaAtlas.files');for(const [name,metadata] of Object.entries(files)){const response=await fetch(new URL('source/'+name,base));assert.equal(response.status,200,name);assert.equal(createHash('sha256').update(Buffer.from(await response.arrayBuffer())).digest('hex'),metadata.sha256,name);}check('Every exported source matches its source-catalog fingerprint',true);
 for(const task of ['financing','composer','packages']){await b.navigate(new URL('source/design/0.2/sandbox.html?task='+task,base).href);await b.until('!!window.HarmoniaView');check(task+' entry links to separate product',await b.evaluate(`document.querySelector('#sandbox-enter').href===${JSON.stringify(new URL('?view='+task,product).href)}`));}
 check('Product has no book route',(await fetch(new URL('book/',product))).status===404);
 await b.cdp('Emulation.setScriptExecutionDisabled',{value:true});await b.navigate(new URL('source/design/0.2/chapters/01-product.html',base).href);check('The written chapter remains readable without JavaScript',await b.evaluate("document.body.textContent.includes('Alice needs financing')"));await b.cdp('Emulation.setScriptExecutionDisabled',{value:false});
 check('No browser errors',b.errors.length===0);await fs.writeFile('docs/0.2/quiet-delivery.json',JSON.stringify({book:base,product,sourceFiles:Object.keys(files).length,checks,errors:b.errors},null,2)+'\n');console.log(JSON.stringify({checks:checks.length,sourceFiles:Object.keys(files).length}));
}finally{await b.cdp('Emulation.setScriptExecutionDisabled',{value:false});b.close();}
