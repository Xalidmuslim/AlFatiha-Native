from pathlib import Path
import re, sys
root=Path(sys.argv[1]) if len(sys.argv)>1 else Path('.')
ui=root/'app/src/main/java/com/xalidmuslim/arabicgrammar/ui'

def replace_exact(path, old, new, label):
    p=ui/path
    s=p.read_text(encoding='utf-8')
    if old not in s:
        raise SystemExit(f'{label}: pattern not found in {path}')
    p.write_text(s.replace(old,new,1),encoding='utf-8')

# Navigation: keep motion but make response immediate and short; remove scale compositor work.
replace_exact('Navigation.kt',
'''if(settings.animations){val smooth=CubicBezierEasing(.2f,.8f,.2f,1f);(slideInHorizontally(tween(360,easing=smooth)){it/18}+fadeIn(tween(280,easing=smooth))+scaleIn(initialScale=.992f,animationSpec=tween(360,easing=smooth))) togetherWith (slideOutHorizontally(tween(300,easing=smooth)){-it/24}+fadeOut(tween(220,easing=smooth))+scaleOut(targetScale=.996f,animationSpec=tween(300,easing=smooth)))} else''',
'''if(settings.animations){val smooth=CubicBezierEasing(.2f,.8f,.2f,1f);(slideInHorizontally(tween(165,easing=smooth)){it/30}+fadeIn(tween(120,easing=smooth))) togetherWith (slideOutHorizontally(tween(110,easing=smooth)){-it/36}+fadeOut(tween(90,easing=smooth)))} else''',
'navigation motion')

# Static surfaces don't change size on press/navigation; layout animation only adds work.
for path in ['Components.kt','ReaderScreen.kt']:
    p=ui/path
    s=p.read_text(encoding='utf-8')
    before=s
    s=s.replace('import androidx.compose.animation.animateContentSize\n','')
    s=s.replace('import androidx.compose.animation.core.FastOutSlowInEasing\n','')
    s=s.replace('import androidx.compose.animation.core.tween\n','')
    s=s.replace('.animateContentSize(tween(320,easing=FastOutSlowInEasing))','')
    if s==before:
        raise SystemExit(f'static layout motion: no changes in {path}')
    p.write_text(s,encoding='utf-8')

# Flashcards: short reveal; state changes immediately on tap.
replace_exact('FlashcardsScreen.kt',
'''(fadeIn(tween(260,easing=FastOutSlowInEasing))+scaleIn(initialScale=.992f,animationSpec=tween(300,easing=FastOutSlowInEasing))) togetherWith fadeOut(tween(190,easing=FastOutSlowInEasing))''',
'''(fadeIn(tween(120,easing=FastOutSlowInEasing))+scaleIn(initialScale=.998f,animationSpec=tween(145,easing=FastOutSlowInEasing))) togetherWith fadeOut(tween(85,easing=FastOutSlowInEasing))''',
'flashcard reveal')

print('Fast motion patch applied.')