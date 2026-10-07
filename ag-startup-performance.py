from pathlib import Path
import sys

root=Path(sys.argv[1]) if len(sys.argv)>1 else Path('.')
src=root/'app/src/main/java/com/xalidmuslim/arabicgrammar'

# MainActivity: render immediately; initialize SQLite repository off the UI thread.
p=src/'MainActivity.kt'
s=p.read_text(encoding='utf-8')
old='''package com.xalidmuslim.arabicgrammar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.xalidmuslim.arabicgrammar.data.BookRepository
import com.xalidmuslim.arabicgrammar.data.ProgressStore
import com.xalidmuslim.arabicgrammar.ui.ArabicGrammarApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = BookRepository(this)
        val progress = ProgressStore(this)
        setContent { ArabicGrammarApp(repository = repository, progress = progress) }
    }
}
'''
new='''package com.xalidmuslim.arabicgrammar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.xalidmuslim.arabicgrammar.data.BookRepository
import com.xalidmuslim.arabicgrammar.data.ProgressStore
import com.xalidmuslim.arabicgrammar.ui.ArabicGrammarApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val progress = ProgressStore(this)
        setContent {
            var repository by remember { mutableStateOf<BookRepository?>(null) }
            LaunchedEffect(Unit) {
                repository = withContext(Dispatchers.IO) { BookRepository(applicationContext) }
            }
            AnimatedContent(
                targetState = repository,
                transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(70)) },
                label = "startup"
            ) { repo ->
                if (repo == null) {
                    Box(Modifier.fillMaxSize().background(Color(0xFFF5F1E8)))
                } else {
                    ArabicGrammarApp(repository = repo, progress = progress)
                }
            }
        }
    }
}
'''
if old not in s:
    raise SystemExit('MainActivity source pattern not found')
p.write_text(s.replace(old,new,1),encoding='utf-8')

# ProgressStore: one-time migration turns animations back on, while keeping the setting functional afterwards.
p=src/'data/ProgressStore.kt'
s=p.read_text(encoding='utf-8')
old='''    private fun loadSettings():ReaderSettings{val storedArabic=prefs.getString("arabic_font_choice","naskh")?:"naskh";val normalizedArabic=if(storedArabic=="serif")"naskh" else storedArabic;return ReaderSettings(theme=prefs.getString("theme","light")?:"light",textScale=prefs.getFloat("text_scale",1f),arabicScale=prefs.getFloat("arabic_scale",1.12f),lineHeight=prefs.getFloat("line_height",1.55f),paragraphGap=prefs.getFloat("paragraph_gap",12f),fontChoice=prefs.getString("font_choice","sans")?:"sans",arabicFontChoice=normalizedArabic,animations=prefs.getBoolean("animations",true))}'''
new='''    private fun loadSettings():ReaderSettings{if(!prefs.getBoolean("motion_migration_v10",false)){prefs.edit().putBoolean("animations",true).putBoolean("motion_migration_v10",true).apply()};val storedArabic=prefs.getString("arabic_font_choice","naskh")?:"naskh";val normalizedArabic=if(storedArabic=="serif")"naskh" else storedArabic;return ReaderSettings(theme=prefs.getString("theme","light")?:"light",textScale=prefs.getFloat("text_scale",1f),arabicScale=prefs.getFloat("arabic_scale",1.12f),lineHeight=prefs.getFloat("line_height",1.55f),paragraphGap=prefs.getFloat("paragraph_gap",12f),fontChoice=prefs.getString("font_choice","sans")?:"sans",arabicFontChoice=normalizedArabic,animations=prefs.getBoolean("animations",true))}'''
if old not in s:
    raise SystemExit('ProgressStore loadSettings pattern not found')
p.write_text(s.replace(old,new,1),encoding='utf-8')
print('Startup responsiveness patch applied.')
