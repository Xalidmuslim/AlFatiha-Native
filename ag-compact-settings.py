from pathlib import Path
import sys

root = Path(sys.argv[1]) if len(sys.argv) > 1 else Path('.')
ui = root / 'app/src/main/java/com/xalidmuslim/arabicgrammar/ui'

# Settings screen only: reduce empty vertical space while keeping text and controls readable.
p = ui / 'SearchSettingsScreens.kt'
s = p.read_text(encoding='utf-8')
repls = [
    (
        'fun SettingsScreen(progress:ProgressStore){val settings=progress.settings;LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){',
        'fun SettingsScreen(progress:ProgressStore){val settings=progress.settings;LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(horizontal=14.dp,vertical=10.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){'
    ),
    (
        'modifier=Modifier.fillMaxWidth().padding(top=10.dp)',
        'modifier=Modifier.fillMaxWidth().padding(top=4.dp)'
    ),
    (
        'lineHeight=(40f*settings.arabicScale).sp',
        'lineHeight=(36f*settings.arabicScale).sp'
    ),
    (
        'Spacer(Modifier.height(4.dp));Text("Шрифт меняется отдельно только для арабского текста, русский шрифт остаётся прежним."',
        'Spacer(Modifier.height(2.dp));Text("Шрифт меняется отдельно только для арабского текста, русский шрифт остаётся прежним."'
    ),
]
for old,new in repls:
    if old not in s:
        raise SystemExit(f'Missing settings pattern: {old[:90]}')
    s = s.replace(old,new,1)
p.write_text(s,encoding='utf-8')

# Settings-only helper cards. These helpers are not used outside the settings screen.
p = ui / 'Components.kt'
s = p.read_text(encoding='utf-8')
old = '@Composable fun SettingsCard(title:String,content: @Composable () -> Unit){OutlinedCard(modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(16.dp)){Text(title,style=MaterialTheme.typography.titleSmall,color=CardHeadlineBrown);Spacer(Modifier.height(10.dp));content()}}}'
new = '@Composable fun SettingsCard(title:String,content: @Composable () -> Unit){OutlinedCard(modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(horizontal=14.dp,vertical=10.dp)){Text(title,style=MaterialTheme.typography.titleSmall,color=CardHeadlineBrown);Spacer(Modifier.height(6.dp));content()}}}'
if old not in s:
    raise SystemExit('Missing SettingsCard post-motion pattern')
s = s.replace(old,new,1)
old = '@Composable fun SliderCard(title:String,value:Float,range:ClosedFloatingPointRange<Float>,onChange:(Float)->Unit){SettingsCard(title){Slider(value=value,onValueChange=onChange,valueRange=range)}}'
new = '@Composable fun SliderCard(title:String,value:Float,range:ClosedFloatingPointRange<Float>,onChange:(Float)->Unit){OutlinedCard(modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(horizontal=14.dp,vertical=8.dp)){Text(title,style=MaterialTheme.typography.titleSmall,color=CardHeadlineBrown);Spacer(Modifier.height(2.dp));Slider(value=value,onValueChange=onChange,valueRange=range,modifier=Modifier.fillMaxWidth().height(40.dp))}}}'
if old not in s:
    raise SystemExit('Missing SliderCard pattern')
s = s.replace(old,new,1)
p.write_text(s,encoding='utf-8')

print('Compact settings patch applied.')
