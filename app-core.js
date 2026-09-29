const ADHKAR = [...window.ADHKAR_PART1, ...window.ADHKAR_PART2];

const DEFAULT_SETTINGS = {
  arabicFont: '"Noto Naskh Arabic", serif',
  russianFont: '"Literata", serif',
  arabicSize: 32,
  russianSize: 17,
  lineHeight: 1.65,
  readerStyle: 'book',
  showTranslation: true,
  showSources: true,
  showNotes: true,
  theme: 'auto'
};

const ARABIC_FONTS = [
  ['"Noto Naskh Arabic", serif','Noto Naskh','Чёткий'],
  ['"Noto Sans Arabic", sans-serif','Noto Sans','Очень читаемый'],
  ['"Amiri", serif','Amiri','Классический'],
  ['"Scheherazade New", serif','Scheherazade','Мягкий']
];
const RUSSIAN_FONTS = [
  ['"Literata", serif','Literata','Книжный'],
  ['"PT Serif", serif','PT Serif','Классика'],
  ['"Inter", sans-serif','Inter','Современный'],
  ['"Manrope", sans-serif','Manrope','Компактный'],
  ['system-ui, sans-serif','Android Sans','Системный'],
  ['serif','Android Serif','Системный']
];

const state = {
  period: (new Date().getHours() >= 16 || new Date().getHours() < 4) ? 'evening' : 'morning',
  viewMode: localStorage.getItem('azkar-view-mode') || 'cards',
  activeIndex: 0,
  settings: loadJson('azkar-settings', DEFAULT_SETTINGS),
  progress: loadJson(progressKey(), {}),
  modal: null,
  selectedId: null,
  touch: null,
  direction: 'next'
};

function loadJson(key, fallback) {
  try { const v = localStorage.getItem(key); return v ? {...fallback, ...JSON.parse(v)} : fallback; } catch { return fallback; }
}
function save() {
  localStorage.setItem('azkar-view-mode', state.viewMode);
  localStorage.setItem('azkar-settings', JSON.stringify(state.settings));
  localStorage.setItem(progressKey(), JSON.stringify(state.progress));
}
function progressKey() {
  const d=new Date();
  return 'azkar-progress-'+d.getFullYear()+'-'+String(d.getMonth()+1).padStart(2,'0')+'-'+String(d.getDate()).padStart(2,'0');
}
function visible() { return ADHKAR.filter(x=>x.times.includes(state.period)); }
function esc(s='') { return String(s).replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[m])); }
function nl(s='') { return esc(s).split('\n').join('<br>'); }
function itemText(item) {
  if(state.period==='evening' && item.eveningArabic) return {arabic:item.eveningArabic, translation:item.eveningTranslation};
  return {arabic:item.arabic, translation:item.translation};
}
function completed(item) { return (state.progress[item.id]||0) >= item.count; }
function currentCount(item) { return Math.min(state.progress[item.id]||0,item.count); }

function applyTheme() {
  const t=state.settings.theme;
  const dark=t==='dark' || (t==='auto' && matchMedia('(prefers-color-scheme: dark)').matches);
  document.documentElement.classList.toggle('dark',dark);
  document.querySelector('meta[name="theme-color"]').setAttribute('content', dark ? '#111613' : '#f4f0e7');
}

function render() {
  applyTheme();
  const list=visible();
  if(state.activeIndex >= list.length) state.activeIndex=0;
  const done=list.filter(completed).length;
  const pct=list.length ? Math.round(done/list.length*100) : 0;
  const current=list[state.activeIndex];
  const mainItems=state.viewMode==='cards' ? [current] : list;

  document.documentElement.style.setProperty('--arabic-font',state.settings.arabicFont);
  document.documentElement.style.setProperty('--reader-font',state.settings.russianFont);
  document.documentElement.style.setProperty('--arabic-size',state.settings.arabicSize+'px');
  document.documentElement.style.setProperty('--russian-size',state.settings.russianSize+'px');
  document.documentElement.style.setProperty('--reader-line-height',state.settings.lineHeight);

  document.getElementById('app').innerHTML = `
  <main class="app-shell">
    <header class="topbar">
      <div class="brand"><span class="brand-icon">✦</span><div><h1>Азкар</h1><small>утро · вечер</small></div></div>
      <button class="icon-btn" data-action="settings" aria-label="Настройки">⚙</button>
    </header>

    <div class="source-note">Основа списка — Абдуль-Азиз ат-Тарифи, «Утренние и вечерние азкары: передача и исследование». Спорные оценки отмечены отдельно.</div>

    <div class="period-tabs">
      <button class="period-btn ${state.period==='morning'?'active':''}" data-period="morning">☀ Утро</button>
      <button class="period-btn ${state.period==='evening'?'active':''}" data-period="evening">☾ Вечер</button>
    </div>

    <section class="progress-card">
      <div class="progress-head"><span>${state.period==='morning'?'Утренние':'Вечерние'} азкары <b>${done} из ${list.length}</b></span><button class="text-btn" data-action="reset">↻ Сбросить</button></div>
      <div class="progress-track"><span style="width:${pct}%"></span></div>
    </section>

    <div class="reading-toolbar" id="reading-start">
      <button class="toolbar-btn" data-action="contents">☷ <span>Содержание</span></button>
      <span class="position">${state.viewMode==='cards' ? (state.activeIndex+1)+' из '+list.length : list.length+' азкаров'}</span>
      <button class="icon-btn compact" data-action="settings" aria-label="Настройки">⚙</button>
    </div>

    <section class="cards ${state.settings.readerStyle==='compact'?'compact':''} ${state.viewMode==='cards'?'paged slide-'+state.direction:''}" id="cards">
      ${mainItems.map(item=>renderCard(item,list.indexOf(item))).join('')}
    </section>

    ${state.viewMode==='cards' ? `<div class="pager">
      <button class="pager-btn secondary" data-action="prev" ${state.activeIndex===0?'disabled':''}>‹ Назад</button>
      <div class="pager-mid"><b>${state.activeIndex+1}</b><span>из ${list.length}</span><small>свайп влево/вправо</small></div>
      <button class="pager-btn primary" data-action="next" ${state.activeIndex===list.length-1?'disabled':''}>Далее ›</button>
    </div>` : ''}

    <footer>Русский текст — смысловой перевод. Разногласия и дополнительные оценки вынесены в примечания и разъяснения.</footer>
  </main>
  ${renderModal(list)}`;
  bindEvents();
}

function renderCard(item,index) {
  const txt=itemText(item), cur=currentCount(item), done=completed(item);
  return `<article class="dhikr-card ${done?'done':''}" id="adhkar-${item.id}">
    <div class="card-top"><span class="num">${String(index+1).padStart(2,'0')}</span><div class="heading"><h2>${esc(item.title)}</h2>${item.disputed && state.settings.showNotes?'<span class="badge">есть разногласие</span>':''}</div>${done?'<span class="done-dot">✓</span>':''}</div>
    <div class="arabic" lang="ar" dir="rtl">${nl(txt.arabic)}</div>
    ${state.settings.showTranslation?`<div class="translation">${nl(txt.translation)}</div>`:''}
    ${state.settings.showSources?`<div class="source-row"><b>Источник</b><span>${esc(item.source)}</span></div>`:''}
    ${state.settings.showNotes && item.note?`<div class="note-box">ⓘ <span>${esc(item.note)}</span></div>`:''}
    ${item.insight?`<button class="explain-btn" data-insight="${item.id}">▣ Разъяснение · история · слова учёных</button>`:''}
    <div class="counter-row"><div class="counter">${done?'<span>Выполнено</span>':'<b>'+cur+'</b> / '+item.count}</div><button class="count-btn" data-count="${item.id}" ${done?'disabled':''}>${done?'✓ Готово':item.count===1?'Прочитано':'+1 · осталось '+(item.count-cur)}</button></div>
  </article>`;
}

function renderModal(list) {
  if(!state.modal) return '';
  if(state.modal==='contents') {
    return `<div class="overlay" data-close><div class="sheet contents-sheet" role="dialog" aria-modal="true" onclick="event.stopPropagation()">
      <div class="handle"></div><button class="sheet-close" data-action="close">×</button>
      <header class="sheet-head"><small>НАВИГАЦИЯ</small><h2>Содержание</h2><p>Быстрый переход к любому зикру.</p></header>
      <div class="mode-switch"><button data-mode="cards" class="${state.viewMode==='cards'?'active':''}">По одному</button><button data-mode="list" class="${state.viewMode==='list'?'active':''}">Список</button></div>
      <div class="contents-list">${list.map((item,i)=>`<button class="contents-item ${state.viewMode==='cards'&&i===state.activeIndex?'active':''}" data-jump="${i}"><span class="num">${String(i+1).padStart(2,'0')}</span><span><b>${esc(item.title)}</b><small>${item.count>1?item.count+' повторений':'1 раз'}</small></span>${completed(item)?'<i>✓</i>':''}</button>`).join('')}</div>
    </div></div>`;
  }
  if(state.modal==='settings') return renderSettings();
  if(state.modal==='insight') {
    const item=ADHKAR.find(x=>x.id===state.selectedId); if(!item?.insight) return '';
    const x=item.insight;
    return `<div class="overlay" data-close><div class="sheet insight-sheet" role="dialog" aria-modal="true" onclick="event.stopPropagation()">
      <div class="handle"></div><button class="sheet-close" data-action="close">×</button>
      <header class="sheet-head"><small>РАЗЪЯСНЕНИЕ</small><h2>${esc(item.title)}</h2><p>Смысл, достоверный контекст и примечания учёных.</p></header>
      <div class="insight-body"><section><h3>Смысл</h3><p>${esc(x.meaning)}</p></section>
      ${x.context?`<section><h3>Связанный случай</h3><p>${esc(x.context)}</p></section>`:''}
      <section><h3>Примечания учёных</h3>${x.scholars.map((s,i)=>`<div class="scholar"><span>${i+1}</span><p>${esc(s)}</p></div>`).join('')}</section>
      <section class="refs"><h3>Источники разбора</h3>${x.references.map(r=>`<span>${esc(r)}</span>`).join('')}</section></div>
    </div></div>`;
  }
  return '';
}

function renderSettings() {
  const s=state.settings;
  return `<div class="overlay" data-close><div class="sheet settings-sheet" role="dialog" aria-modal="true" onclick="event.stopPropagation()">
    <div class="handle"></div><button class="sheet-close" data-action="close">×</button>
    <header class="sheet-head compact-head"><small>НАСТРОЙКИ ЧТЕНИЯ</small><h2>Текст в азкарах</h2><p>Изменения видны сразу и сохраняются.</p></header>
    <div class="settings-body">
      <div class="live-preview"><div class="preview-ar" dir="rtl" style="font-family:${s.arabicFont};font-size:${Math.min(s.arabicSize,24)}px;line-height:${s.lineHeight}">بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ</div><div style="font-family:${s.russianFont};font-size:${Math.min(s.russianSize,15)}px;line-height:${s.lineHeight}">Так будет выглядеть перевод.</div></div>
      <section><h3>Русский шрифт</h3><div class="font-grid russian">${RUSSIAN_FONTS.map(f=>fontTile(f,'russianFont')).join('')}</div></section>
      <section><h3>Арабский шрифт</h3><div class="font-grid arabic-grid">${ARABIC_FONTS.map(f=>fontTile(f,'arabicFont',true)).join('')}</div></section>
      <section class="slider-grid">
        ${slider('Арабский текст','arabicSize',22,44,1,s.arabicSize+' px')}
        ${slider('Русский текст','russianSize',13,25,1,s.russianSize+' px')}
      </section>
      <section>${slider('Межстрочный интервал','lineHeight',1.25,2,0.05,Number(s.lineHeight).toFixed(2))}</section>
      <section><h3>Оформление</h3><div class="style-grid"><button data-style="book" class="${s.readerStyle==='book'?'active':''}"><b>Книжный</b><small>Больше воздуха</small></button><button data-style="compact" class="${s.readerStyle==='compact'?'active':''}"><b>Компактный</b><small>Больше текста</small></button></div></section>
      <section class="toggle-grid">${toggle('Перевод','showTranslation',s.showTranslation)}${toggle('Источники','showSources',s.showSources)}${toggle('Примечания','showNotes',s.showNotes)}
        <div class="theme-row"><span><b>Тема</b><small>светлая / тёмная / системная</small></span><select data-setting="theme"><option value="auto" ${s.theme==='auto'?'selected':''}>Системная</option><option value="light" ${s.theme==='light'?'selected':''}>Светлая</option><option value="dark" ${s.theme==='dark'?'selected':''}>Тёмная</option></select></div>
      </section>
    </div>
  </div></div>`;
}
function fontTile(f,key,arabic=false) {
  const active=state.settings[key]===f[0];
  return `<button class="font-tile ${active?'active':''}" data-font-key="${key}" data-font-value="${esc(f[0])}"><b style="font-family:${f[0]}">${esc(f[1])}</b><small>${esc(f[2])}</small><span dir="${arabic?'rtl':'ltr'}" style="font-family:${f[0]}">${arabic?'بِسْمِ اللَّهِ':'Пример текста'}</span></button>`;
}
function slider(label,key,min,max,step,value) { return `<div class="control"><div><b>${label}</b><span>${value}</span></div><input type="range" min="${min}" max="${max}" step="${step}" value="${state.settings[key]}" data-range="${key}"></div>`; }
function toggle(label,key,on) { return `<label class="toggle-item"><span>${label}</span><input type="checkbox" data-toggle="${key}" ${on?'checked':''}><i></i></label>`; }

function bindEvents() {
  document.querySelectorAll('[data-period]').forEach(b=>b.onclick=()=>{state.period=b.dataset.period; state.activeIndex=0; state.direction='next'; state.progress=loadJson(progressKey(),{}); save(); render();});
  document.querySelectorAll('[data-action="settings"]').forEach(b=>b.onclick=()=>{state.modal='settings'; render();});
  document.querySelector('[data-action="contents"]')?.addEventListener('click',()=>{state.modal='contents';render();});
  document.querySelector('[data-action="reset"]')?.addEventListener('click',()=>{visible().forEach(x=>delete state.progress[x.id]);save();render();});
  document.querySelectorAll('[data-count]').forEach(b=>b.onclick=()=>{const item=ADHKAR.find(x=>x.id===b.dataset.count); state.progress[item.id]=Math.min(item.count,(state.progress[item.id]||0)+1);save();render();});
  document.querySelectorAll('[data-insight]').forEach(b=>b.onclick=()=>{state.selectedId=b.dataset.insight;state.modal='insight';render();});
  document.querySelectorAll('[data-action="close"]').forEach(b=>b.onclick=()=>{state.modal=null;render();});
  document.querySelectorAll('[data-close]').forEach(x=>x.onclick=()=>{state.modal=null;render();});
  document.querySelectorAll('[data-mode]').forEach(b=>b.onclick=()=>{state.viewMode=b.dataset.mode; save(); state.modal=null; render(); scrollToReading(true);});
  document.querySelectorAll('[data-jump]').forEach(b=>b.onclick=()=>{const i=Number(b.dataset.jump); state.modal=null; if(state.viewMode==='cards') goTo(i); else {render(); requestAnimationFrame(()=>document.getElementById('adhkar-'+visible()[i].id)?.scrollIntoView({behavior:'smooth',block:'start'}));} });
  document.querySelector('[data-action="prev"]')?.addEventListener('click',()=>goTo(state.activeIndex-1));
  document.querySelector('[data-action="next"]')?.addEventListener('click',()=>goTo(state.activeIndex+1));

  document.querySelectorAll('[data-font-key]').forEach(b=>b.onclick=()=>{state.settings[b.dataset.fontKey]=b.dataset.fontValue;save();render();});
  document.querySelectorAll('[data-range]').forEach(r=>{
    const applyRange=()=>{
      const key=r.dataset.range;
      const value=Number(r.value);
      state.settings[key]=value;
      if(key==='arabicSize') document.documentElement.style.setProperty('--arabic-size',value+'px');
      if(key==='russianSize') document.documentElement.style.setProperty('--russian-size',value+'px');
      if(key==='lineHeight') document.documentElement.style.setProperty('--reader-line-height',value);
      const label=r.parentElement?.querySelector('div > span');
      if(label) label.textContent=key==='lineHeight'?value.toFixed(2):value+' px';
      const ar=document.querySelector('.preview-ar');
      const ru=document.querySelector('.live-preview > div:nth-child(2)');
      if(ar){
        ar.style.fontSize=Math.min(state.settings.arabicSize,24)+'px';
        ar.style.lineHeight=state.settings.lineHeight;
      }
      if(ru){
        ru.style.fontSize=Math.min(state.settings.russianSize,15)+'px';
        ru.style.lineHeight=state.settings.lineHeight;
      }
      save();
    };
    r.oninput=applyRange;
    r.onchange=applyRange;
  });
  document.querySelectorAll('[data-toggle]').forEach(t=>t.onchange=()=>{state.settings[t.dataset.toggle]=t.checked;save();render();});
  document.querySelectorAll('[data-style]').forEach(b=>b.onclick=()=>{state.settings.readerStyle=b.dataset.style;save();render();});
  document.querySelector('[data-setting="theme"]')?.addEventListener('change',e=>{state.settings.theme=e.target.value;save();render();});

  const cards=document.getElementById('cards');
  if(cards && state.viewMode==='cards') {
    cards.addEventListener('touchstart',e=>{const t=e.touches[0];state.touch={x:t.clientX,y:t.clientY};},{passive:true});
    cards.addEventListener('touchend',e=>{if(!state.touch)return;const t=e.changedTouches[0];const dx=t.clientX-state.touch.x,dy=t.clientY-state.touch.y;state.touch=null;if(Math.abs(dx)<55||Math.abs(dx)<=Math.abs(dy)*1.15)return;if(dx<0)goTo(state.activeIndex+1);else goTo(state.activeIndex-1);},{passive:true});
  }
}

function scrollToReading(smooth=false) {
  const a=document.getElementById('reading-start'); if(!a)return;
  const top=a.getBoundingClientRect().top+window.scrollY-4;
  window.scrollTo({top,behavior:smooth?'smooth':'auto'});
}
function goTo(i) {
  const list=visible();
  const n=Math.max(0,Math.min(i,list.length-1));
  if(n===state.activeIndex) {scrollToReading(true);return;}
  scrollToReading(false);
  state.direction=n>state.activeIndex?'next':'prev';
  requestAnimationFrame(()=>{state.activeIndex=n;save();render();});
}

matchMedia('(prefers-color-scheme: dark)').addEventListener?.('change',()=>{if(state.settings.theme==='auto')render();});
if('serviceWorker' in navigator) window.addEventListener('load',()=>navigator.serviceWorker.register('/sw.js').catch(()=>{}));
render();