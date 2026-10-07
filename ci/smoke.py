import subprocess,time,xml.etree.ElementTree as ET,re,pathlib
out=pathlib.Path('qa');out.mkdir(exist_ok=True)
def adb(*args):return subprocess.check_output(['adb',*args])
def state():
 adb('shell','uiautomator','dump','/sdcard/window.xml')
 return ET.fromstring(adb('shell','cat','/sdcard/window.xml'))
def click(label,scroll=False):
 for attempt in range(7 if scroll else 3):
  root=state();parents={child:parent for parent in root.iter() for child in parent}
  for n in root.iter('node'):
   texts=n.attrib.get('text','').splitlines()
   if label in texts or n.attrib.get('content-desc')==label:
    target=n
    while target.attrib.get('clickable')!='true' and target in parents:target=parents[target]
    if target.attrib.get('clickable')!='true':continue
    v=list(map(int,re.findall(r'\d+',target.attrib['bounds'])));adb('shell','input','tap',str((v[0]+v[2])//2),str((v[1]+v[3])//2));time.sleep(1);return
  if scroll:adb('shell','input','swipe','200','900','200','450','350')
  time.sleep(1)
 raise RuntimeError('UI element not found: '+label)
def shot(name):
 (out/(name+'.png')).write_bytes(adb('exec-out','screencap','-p'))
 (out/(name+'.xml')).write_bytes(ET.tostring(state(),encoding='utf-8'))
adb('shell','am','start','-n','ru.madarij.nativeapp.premiumpreview/ru.madarij.nativeapp.MainActivity');time.sleep(5)
shot('01-home');click('Продолжить чтение',True);time.sleep(3);shot('02-reader')
click('⋯');click('Содержание раздела');shot('03-chapter-contents');click('Начало раздела');shot('04-reader-back-at-start')
click('⋯');click('Закладка главы');click('Сохранить');shot('05-bookmark-saved')
click('Закладки');shot('06-bookmarks')
click('Поиск');shot('07-search')
click('Оглавление');shot('08-contents')
click('Ещё');click('Настройки',True);shot('09-settings');click('Тёплая',True);click('Книжный',True);shot('10-warm-setting')
adb('shell','input','keyevent','4');time.sleep(1);click('Главная');click('Продолжить чтение',True);shot('11-reader-warm')
adb('shell','input','swipe','200','900','200','450','350');time.sleep(1);shot('12-reader-scrolled')
adb('shell','input','keyevent','4');time.sleep(1);shot('13-back')
logs=adb('logcat','-d','-s','AndroidRuntime:E').decode(errors='replace');(out/'runtime-log.txt').write_text(logs)
if 'FATAL EXCEPTION' in logs:raise RuntimeError('Android runtime crash')
# Confirm saved bookmarks survive killing the app process.
adb('shell','am','force-stop','ru.madarij.nativeapp.premiumpreview')
adb('shell','am','start','-n','ru.madarij.nativeapp.premiumpreview/ru.madarij.nativeapp.MainActivity');time.sleep(3)
click('Закладки');shot('14-bookmarks-after-restart')
texts='\n'.join(n.attrib.get('text','') for n in state().iter('node'))
assert 'Предисловие' in texts, 'Saved bookmark missing after restart'
click('Оглавление');click('Читать',True);shot('15-reader-from-contents')
adb('shell','input','keyevent','4');time.sleep(1)
# A second existing chapter from the authoritative corpus; this is not reference text.
click('Оглавление')
root=state();buttons=[n for n in root.iter('node') if n.attrib.get('text')=='Читать']
if len(buttons)>1:
 v=list(map(int,re.findall(r'\d+',buttons[1].attrib['bounds'])));adb('shell','input','tap',str((v[0]+v[2])//2),str((v[1]+v[3])//2));time.sleep(2);shot('16-second-chapter')
adb('shell','settings','put','system','font_scale','1.3');time.sleep(2);shot('17-large-font')
adb('shell','settings','put','system','font_scale','1.0')
(out/'result.txt').write_text('PASS: launch, reader, same-reader chapter panel, bookmark create/list, search screen, contents, settings, warm theme, reader scroll and Back. Text selection, exact bookmark offset, multiple chapters, process-death and performance not fully verified.')
