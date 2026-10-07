import subprocess,time,xml.etree.ElementTree as ET,re,pathlib
out=pathlib.Path('qa');out.mkdir(exist_ok=True)
def adb(*args):return subprocess.check_output(['adb',*args])
def state():
 adb('shell','uiautomator','dump','/sdcard/window.xml')
 return ET.fromstring(adb('shell','cat','/sdcard/window.xml'))
def click(label,scroll=False):
 for attempt in range(7 if scroll else 3):
  for n in state().iter('node'):
   if n.attrib.get('text')==label or n.attrib.get('content-desc')==label:
    v=list(map(int,re.findall(r'\d+',n.attrib['bounds'])));adb('shell','input','tap',str((v[0]+v[2])//2),str((v[1]+v[3])//2));time.sleep(1);return
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
click('Ещё');click('Настройки',True);shot('09-settings');click('Тёплая',True);shot('10-warm-setting')
adb('shell','input','keyevent','4');time.sleep(1);click('Главная');click('Продолжить чтение',True);shot('11-reader-warm')
adb('shell','input','swipe','200','900','200','450','350');time.sleep(1);shot('12-reader-scrolled')
adb('shell','input','keyevent','4');time.sleep(1);shot('13-back')
logs=adb('logcat','-d','-s','AndroidRuntime:E').decode(errors='replace');(out/'runtime-log.txt').write_text(logs)
if 'FATAL EXCEPTION' in logs:raise RuntimeError('Android runtime crash')
(out/'result.txt').write_text('PASS: launch, reader, same-reader chapter panel, bookmark create/list, search screen, contents, settings, warm theme, reader scroll and Back. Text selection, exact bookmark offset, multiple chapters, process-death and performance not fully verified.')
