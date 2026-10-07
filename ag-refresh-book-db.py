from pathlib import Path
import sys

root=Path(sys.argv[1]) if len(sys.argv)>1 else Path('.')
p=root/'app/src/main/java/com/xalidmuslim/arabicgrammar/data/BookRepository.kt'
s=p.read_text(encoding='utf-8')

old='''        val dbFile = File(appContext.filesDir, "arabic_grammar_book_v4.db")'''
new='''        // v5 forces one clean refresh of the packaged book database.
        // User progress/settings live in SharedPreferences and are not affected.
        val dbFile = File(appContext.filesDir, "arabic_grammar_book_v5.db")'''
if old not in s:
    raise SystemExit('old database filename not found')
s=s.replace(old,new,1)

p.write_text(s,encoding='utf-8')
print('Book DB refresh migration applied: v4 -> v5')
