from pathlib import Path
import sys

root=Path(sys.argv[1]) if len(sys.argv)>1 else Path('.')
ui=root/'app/src/main/java/com/xalidmuslim/arabicgrammar/ui'
data=root/'app/src/main/java/com/xalidmuslim/arabicgrammar/data'

def replace_exact(base, path, old, new, label, count=1):
    p=base/path
    s=p.read_text(encoding='utf-8')
    n=s.count(old)
    if n < count:
        raise SystemExit(f'{label}: expected at least {count}, found {n} in {path}')
    p.write_text(s.replace(old,new,count),encoding='utf-8')

# 1) Screen navigation: immediate response, shorter transitions, and correct reverse motion on Back.
nav_path=ui/'Navigation.kt'
s=nav_path.read_text(encoding='utf-8')
old='''@Composable fun ArabicGrammarApp(repository:BookRepository,progress:ProgressStore){val settings=progress.settings;val stack=remember{mutableStateListOf<Screen>(Screen.Home)};val holder=rememberSaveableStateHolder();val current=stack.last();DisposableEffect(repository){onDispose{repository.close()}};fun nav(s:Screen){if(stack.lastOrNull()!=s)stack.add(s)};fun back(){if(stack.size>1)stack.removeAt(stack.lastIndex)};fun top(s:Screen){if(s==Screen.Home){stack.clear();stack.add(Screen.Home);return};if(stack.firstOrNull()!=Screen.Home){stack.clear();stack.add(Screen.Home)};if(stack.lastOrNull()!=s)stack.add(s)};BackHandler(true){if(stack.size>1)back()};ArabicGrammarTheme(settings,isSystemInDarkTheme()){Scaffold(containerColor=MaterialTheme.colorScheme.background,bottomBar={AppBottomBar(current,::top)}){padding->AnimatedContent(targetState=current,modifier=Modifier.fillMaxSize().padding(padding),transitionSpec={if(settings.animations){val smooth=CubicBezierEasing(.2f,.8f,.2f,1f);(slideInHorizontally(tween(360,easing=smooth)){it/18}+fadeIn(tween(280,easing=smooth))+scaleIn(initialScale=.992f,animationSpec=tween(360,easing=smooth))) togetherWith (slideOutHorizontally(tween(300,easing=smooth)){-it/24}+fadeOut(tween(220,easing=smooth))+scaleOut(targetScale=.996f,animationSpec=tween(300,easing=smooth)))} else fadeIn(tween(1)) togetherWith fadeOut(tween(1))},label="screen"){screen->holder.SaveableStateProvider(screenStateKey(screen)){when(screen){Screen.Home->HomeScreen(repository,progress,::nav);is Screen.Library->LibraryScreen(repository,progress,screen.chapter,::nav,::back);is Screen.Reader->ReaderScreen(repository,progress,screen.sectionId,::nav,::back);Screen.Practice->PracticeScreen(repository,progress,::nav);is Screen.Quiz->QuizScreen(repository,progress,screen.reviewErrors,::back);is Screen.Flashcards->FlashcardsScreen(repository,progress,screen.reviewOnly,::back);is Screen.ConjugationTrainer->ConjugationTrainerScreen(repository,progress,screen.reviewOnly,::back);Screen.Search->SearchScreen(repository,progress,::nav);Screen.Settings->SettingsScreen(progress);Screen.Bookmarks->BookmarksScreen(repository,progress,::nav,::back);Screen.Proverbs->ProverbsScreen(repository,progress,::nav,::back)}}}}}}'''
new='''@Composable fun ArabicGrammarApp(repository:BookRepository,progress:ProgressStore){val settings=progress.settings;val stack=remember{mutableStateListOf<Screen>(Screen.Home)};var reverse by remember{mutableStateOf(false)};val holder=rememberSaveableStateHolder();val current=stack.last();DisposableEffect(repository){onDispose{repository.close()}};fun nav(s:Screen){if(stack.lastOrNull()!=s){reverse=false;stack.add(s)}};fun back(){if(stack.size>1){reverse=true;stack.removeAt(stack.lastIndex)}};fun top(s:Screen){reverse=false;if(s==Screen.Home){stack.clear();stack.add(Screen.Home);return};if(stack.firstOrNull()!=Screen.Home){stack.clear();stack.add(Screen.Home)};if(stack.lastOrNull()!=s)stack.add(s)};BackHandler(true){if(stack.size>1)back()};ArabicGrammarTheme(settings,isSystemInDarkTheme()){Scaffold(containerColor=MaterialTheme.colorScheme.background,bottomBar={AppBottomBar(current,::top)}){padding->AnimatedContent(targetState=current,modifier=Modifier.fillMaxSize().padding(padding),transitionSpec={if(settings.animations){val smooth=CubicBezierEasing(.2f,.86f,.2f,1f);if(reverse)(slideInHorizontally(tween(180,easing=smooth)){-it/26}+fadeIn(tween(150,easing=smooth))) togetherWith (slideOutHorizontally(tween(130,easing=smooth)){it/34}+fadeOut(tween(105,easing=smooth))) else (slideInHorizontally(tween(200,easing=smooth)){it/26}+fadeIn(tween(165,easing=smooth))) togetherWith (slideOutHorizontally(tween(140,easing=smooth)){-it/34}+fadeOut(tween(115,easing=smooth)))} else fadeIn(tween(1)) togetherWith fadeOut(tween(1))},label="screen"){screen->holder.SaveableStateProvider(screenStateKey(screen)){when(screen){Screen.Home->HomeScreen(repository,progress,::nav);is Screen.Library->LibraryScreen(repository,progress,screen.chapter,::nav,::back);is Screen.Reader->ReaderScreen(repository,progress,screen.sectionId,::nav,::back);Screen.Practice->PracticeScreen(repository,progress,::nav);is Screen.Quiz->QuizScreen(repository,progress,screen.reviewErrors,::back);is Screen.Flashcards->FlashcardsScreen(repository,progress,screen.reviewOnly,::back);is Screen.ConjugationTrainer->ConjugationTrainerScreen(repository,progress,screen.reviewOnly,::back);Screen.Search->SearchScreen(repository,progress,::nav);Screen.Settings->SettingsScreen(progress);Screen.Bookmarks->BookmarksScreen(repository,progress,::nav,::back);Screen.Proverbs->ProverbsScreen(repository,progress,::nav,::back)}}}}}}'''
if old not in s:
    raise SystemExit('navigation base pattern not found')
nav_path.write_text(s.replace(old,new,1),encoding='utf-8')

# 2) Static cards: no layout tween; it only adds work on recomposition.
for name in ['Components.kt','ReaderScreen.kt']:
    p=ui/name
    t=p.read_text(encoding='utf-8')
    before=t
    t=t.replace('import androidx.compose.animation.animateContentSize\n','')
    t=t.replace('import androidx.compose.animation.core.FastOutSlowInEasing\n','')
    t=t.replace('import androidx.compose.animation.core.tween\n','')
    t=t.replace('.animateContentSize(tween(320,easing=FastOutSlowInEasing))','')
    if t==before:
        raise SystemExit(f'no static-motion changes in {name}')
    p.write_text(t,encoding='utf-8')

# 3) Flashcards: keep a subtle transition but make it essentially immediate.
replace_exact(ui,'FlashcardsScreen.kt',
'''(fadeIn(tween(260,easing=FastOutSlowInEasing))+scaleIn(initialScale=.992f,animationSpec=tween(300,easing=FastOutSlowInEasing))) togetherWith fadeOut(tween(190,easing=FastOutSlowInEasing))''',
'''(fadeIn(tween(180,easing=FastOutSlowInEasing))+scaleIn(initialScale=.997f,animationSpec=tween(190,easing=FastOutSlowInEasing))) togetherWith fadeOut(tween(125,easing=FastOutSlowInEasing))''',
'flashcard transition')

# 4) Read-only book cache: avoid repeated SQLite + text regrouping when entering/backing out of screens.
br=data/'BookRepository.kt'
t=br.read_text(encoding='utf-8')
t=t.replace('import java.io.File\n','import java.io.File\nimport java.util.concurrent.ConcurrentHashMap\n',1)
t=t.replace('''    private val database: SQLiteDatabase\n''','''    private val database: SQLiteDatabase\n    private val sectionsCache = ConcurrentHashMap<String,List<BookSection>>()\n    private val blocksCache = ConcurrentHashMap<String,List<BookBlock>>()\n    private val readerGroupsCache = ConcurrentHashMap<String,List<ReaderGroup>>()\n    @Volatile private var proverbsCache: List<ProverbEntry>? = null\n''',1)
t=t.replace('''        database = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)\n    }''','''        database = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)\n        Thread({ runCatching { sections(); sections("syntax"); sections("morphology"); sections("extras") } }, "book-prewarm").apply { priority=Thread.MIN_PRIORITY; start() }\n    }''',1)
old_sections='''    fun sections(chapter:String?=null):List<BookSection>{val sql=if(chapter==null)"SELECT id,chapter,number,title,found,exercise_count FROM sections ORDER BY CASE chapter WHEN 'syntax' THEN 1 WHEN 'morphology' THEN 2 ELSE 3 END, number" else "SELECT id,chapter,number,title,found,exercise_count FROM sections WHERE chapter=? ORDER BY number";return querySections(sql,chapter?.let{arrayOf(it)})}'''
new_sections='''    fun sections(chapter:String?=null):List<BookSection>{val key=chapter?:"*";sectionsCache[key]?.let{return it};val sql=if(chapter==null)"SELECT id,chapter,number,title,found,exercise_count FROM sections ORDER BY CASE chapter WHEN 'syntax' THEN 1 WHEN 'morphology' THEN 2 ELSE 3 END, number" else "SELECT id,chapter,number,title,found,exercise_count FROM sections WHERE chapter=? ORDER BY number";val result=querySections(sql,chapter?.let{arrayOf(it)});sectionsCache.putIfAbsent(key,result);return sectionsCache[key]?:result}'''
if old_sections not in t: raise SystemExit('sections pattern not found')
t=t.replace(old_sections,new_sections,1)
old_section='''    fun section(id:String):BookSection?{database.rawQuery("SELECT id,chapter,number,title,found,exercise_count FROM sections WHERE id=? LIMIT 1",arrayOf(id)).use{c->return if(c.moveToFirst())c.toSection() else null}}'''
new_section='''    fun section(id:String):BookSection?=sections().firstOrNull{it.id==id}'''
if old_section not in t: raise SystemExit('section pattern not found')
t=t.replace(old_section,new_section,1)
old_blocks='''    fun blocks(sectionId:String):List<BookBlock>{val out=mutableListOf<BookBlock>();database.rawQuery("SELECT block_index,type,text FROM blocks WHERE section_id=? AND type!='section-heading' ORDER BY block_index",arrayOf(sectionId)).use{c->while(c.moveToNext())out+=BookBlock(c.getInt(0),c.getString(1),cleanBookText(c.getString(2)))};return out}'''
new_blocks='''    fun blocks(sectionId:String):List<BookBlock>{blocksCache[sectionId]?.let{return it};val out=mutableListOf<BookBlock>();database.rawQuery("SELECT block_index,type,text FROM blocks WHERE section_id=? AND type!='section-heading' ORDER BY block_index",arrayOf(sectionId)).use{c->while(c.moveToNext())out+=BookBlock(c.getInt(0),c.getString(1),cleanBookText(c.getString(2)))};blocksCache.putIfAbsent(sectionId,out);return blocksCache[sectionId]?:out}\n    fun readerGroups(sectionId:String):List<ReaderGroup>{readerGroupsCache[sectionId]?.let{return it};val result=buildReaderGroups(blocks(sectionId));readerGroupsCache.putIfAbsent(sectionId,result);return readerGroupsCache[sectionId]?:result}'''
if old_blocks not in t: raise SystemExit('blocks pattern not found')
t=t.replace(old_blocks,new_blocks,1)
# Cache expensive proverb scan after first/background calculation.
needle='''    fun proverbs():List<ProverbEntry>{val ar=Regex('''
if needle not in t: raise SystemExit('proverbs start not found')
t=t.replace(needle,'''    fun proverbs():List<ProverbEntry>{proverbsCache?.let{return it};val ar=Regex(''',1)
end=''';return out.distinctBy{it.arabic}}\n    fun quizQuestions'''
if end not in t: raise SystemExit('proverbs end not found')
t=t.replace(end,''';val result=out.distinctBy{it.arabic};proverbsCache=result;return result}\n    fun quizQuestions''',1)
t=t.replace('for (group in buildReaderGroups(blocks(section.id))) {','for (group in readerGroups(section.id)) {',1)
br.write_text(t,encoding='utf-8')

# Reuse cached pre-grouped reader data in the hot navigation paths.
replace_exact(ui,'ReaderScreen.kt','val groups=remember(sectionId){buildReaderGroups(repository.blocks(sectionId))}','val groups=remember(sectionId){repository.readerGroups(sectionId)}','reader groups')
replace_exact(ui,'HomeScreen.kt','val totalItems = buildReaderGroups(repository.blocks(it.id)).size + 1','val totalItems = repository.readerGroups(it.id).size + 1','home reader groups')

print('Ultra-fast motion + read-only cache patch applied.')