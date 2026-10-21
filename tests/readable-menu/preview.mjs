// Browser mock only: applies parsed FancyMenu GUI coordinates, not a game render.
import assert from 'node:assert/strict'
import {readFileSync} from 'node:fs'
const {chromium}=await import(process.env.PLAYWRIGHT_MODULE || 'playwright')
const root=new URL('./',import.meta.url)
const geometry=JSON.parse(readFileSync(new URL('geometry.json',root),'utf8'))
const browser=await chromium.launch({channel:'chrome',headless:true})
const escape=s=>s.replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('"','&quot;')
const newsTitle='Entegre Client 2.4.7: daha okunaklı ana menü ve Türkçe harita işaretleri'
const newsSummary='Tek paket: Prism’de Kurulum Ekle → İçe Aktar ile ZIP’i ekleyin. Ayrıntılar duyuru sayfasında.'
const eventTitle='Yeni ENTEGRE sistemini kutluyoruz: topluluk buluşması ve ortak keşif'
try {
 const page=await browser.newPage()
 for (const [physicalWidth,gui] of [[390,'320x240'],[1440,'640x360']]) {
  const [gw,gh]=gui.split('x').map(Number),scale=physicalWidth/gw,boxes=geometry[gui]
  await page.setViewportSize({width:physicalWidth,height:Math.ceil(gh*scale)+112})
  const panel=(id,html,classes='')=>{const b=boxes[id];return `<div id="${id}" class="box ${classes}" style="left:${b.x}px;top:${b.y}px;width:${b.w}px;height:${b.h}px">${html}</div>`}
  const button=(id,label)=>panel(id,`<button>${label}</button>`)
  const feed=(id,title,text)=>panel(id,`<div class="scroll" tabindex="0"><a href="#detail">${escape(title)}</a><br>${escape(text)}</div>`,'feed')
  const body=[
   ...['play','news','events'].map(name=>panel('community-'+name+'-panel','','panel '+name)),
   panel('community-logo','◆','logo'),panel('community-entegre','Entegre'),panel('community-vesselam','Vesselam','vesselam'),panel('community-quote','Hıyaaâr.'),
   button('mc_titlescreen_singleplayer_button','Singleplayer'),button('mc_titlescreen_multiplayer_button','Multiplayer'),button('mc_titlescreen_options_button','Options…'),button('modmenu_titlescreen_mods_button','Mods'),button('mc_titlescreen_quit_button','Quit Game'),
   panel('community-links','Pano · Harita'),panel('community-wallpapers','Galeri · Wallpaper'),
   panel('community-news-title','DUYURULAR','heading'),feed('community-news-feed',newsTitle,newsSummary),button('community-all-news','Tüm duyurular ›'),
   panel('community-events-title','ETKİNLİKLER','heading event'),feed('community-events-feed',eventTitle,'24.10.2026 — 21:00 TSİ'),button('community-all-events','Tüm etkinlikler ›'),
   panel('community-version','Entegre Client 2.4.7 | 26.3')
  ].join('')
  await page.setContent(`<!doctype html><meta charset="utf-8"><style>*{box-sizing:border-box}html{scroll-behavior:auto}body{margin:0;background:#15251e;color:#edf2e9;font:14px/1.5 system-ui}.notice{padding:12px;height:112px;overflow:hidden} .stage{position:relative;transform-origin:top left;background:linear-gradient(135deg,#283d32,#1c2d28);font:9px/11px monospace;letter-spacing:.6px}.box{position:absolute}.panel{background:#0c1a17ed;border-top:2px solid #c8acf0}.news{border-color:#bce095}.events{border-color:#e8c98f}.logo{color:#c8acf0;font-size:30px;line-height:32px}.vesselam{color:#c8acf0;font-size:12px}.heading{color:#bce095;padding-top:3px}.event{color:#e8c98f}.feed{padding:3px 5px}.scroll{height:100%;overflow:auto;overflow-wrap:anywhere;scrollbar-width:thin;scrollbar-color:#46594d transparent}.scroll a{color:#bce095;text-decoration:none}.scroll:focus-visible,button:focus-visible{outline:2px solid #e8c98f;outline-offset:0}button{width:100%;height:100%;border:1px solid #56735e;background:#162e25;color:#edf2e9;font:inherit;letter-spacing:inherit;cursor:pointer}</style><div class="notice">Tarayıcı maketi — gerçek oyun görüntüsü değildir.<br>${gui} GUI → ${physicalWidth}px önizleme. Dar GUI’de metin alanları kaydırılabilir.</div><div style="width:${physicalWidth}px;height:${gh*scale}px;overflow:hidden"><div class="stage" style="width:${gw}px;height:${gh}px;transform:scale(${scale})">${body}</div></div>`)
  assert(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),'No physical preview overflow')
  for(const id of ['community-news-feed','community-events-feed']){
   const scroll=page.locator('#'+id+' .scroll')
   await scroll.focus()
   assert.equal(await scroll.evaluate(el=>el===document.activeElement),true)
   await page.keyboard.press('End')
   if(gui==='640x360') assert(await scroll.evaluate(el=>el.scrollHeight<=el.clientHeight+1), `${id}: standard fixture fits wide GUI`)
  }
  await page.evaluate(async()=>{
   document.activeElement?.blur()
   document.querySelectorAll('.scroll').forEach(el=>el.scrollTop=0)
   window.scrollTo({top:0,left:0,behavior:'instant'})
   await new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)))
  })
  assert.equal(await page.locator('.notice').count(),1,'Preview has a single top banner')
  assert(await page.locator('#community-version').evaluate(el=>{const r=el.getBoundingClientRect();return r.top>=0&&r.bottom<=innerHeight}), 'Entire version footer is visible')
  await page.screenshot({path:new URL(`preview-${physicalWidth}.png`,root).pathname,fullPage:true})
  console.log(`PASS browser mock: ${gui} GUI at ${physicalWidth}px, bounded fields, keyboard scroll, wide fixture fits`)
 }
}finally{await browser.close()}
