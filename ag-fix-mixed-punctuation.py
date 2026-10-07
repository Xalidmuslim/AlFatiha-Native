from pathlib import Path
import sys

root=Path(sys.argv[1]) if len(sys.argv)>1 else Path('.')
p=root/'app/src/main/java/com/xalidmuslim/arabicgrammar/ui/ReaderScreen.kt'
s=p.read_text(encoding='utf-8')

old='''    flush()
    return if(out.any{it.arabic}&&out.any{!it.arabic})out else null
}'''

new='''    flush()
    if(out.size>=2){
        for(i in 0 until out.lastIndex){
            val current=out[i]
            val next=out[i+1]
            if(current.arabic && !next.arabic && current.text.endsWith("(")){
                val cleaned=current.text.dropLast(1).trimEnd()
                val moved="("+next.text.trimStart()
                out[i]=current.copy(text=cleaned)
                out[i+1]=next.copy(text=moved)
            }
        }
    }
    return if(out.any{it.arabic}&&out.any{!it.arabic})out else null
}'''

if old not in s:
    raise SystemExit('splitScriptChunks insertion point not found')
s=s.replace(old,new,1)
p.write_text(s,encoding='utf-8')
print('Mixed Arabic/Russian punctuation renderer fix applied.')
