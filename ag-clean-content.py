from pathlib import Path
import sqlite3, re, sys, hashlib, json

if len(sys.argv) < 2:
    raise SystemExit("usage: ag-clean-content.py <book.db> [audit.json]")

db=Path(sys.argv[1])
audit=Path(sys.argv[2]) if len(sys.argv)>2 else None
con=sqlite3.connect(str(db))
con.row_factory=sqlite3.Row
before=con.execute("select count(*) from blocks").fetchone()[0]
removed=[]

junk=[
    "©",
    "Система и виды органов исполнительной власти в РФ",
    "Взаимные права и обязанности супругов, родителей и детей",
    "Проверочный расчет прочности и устойчивости",
    "Понятие и признаки полезной модели",
    "Эластичность спроса и предложения",
    "Эффекторные нервные окончания",
    "Спрос и его величина, кривая спроса",
    "Закон спроса, исключения в его действии",
]
page_nav=re.compile(r"^Склоняемые и несклоняемые слова \d+ страница$")
stars=re.compile(r"^\*{3,}$")

for r in con.execute("select block_index,section_id,text from blocks order by block_index").fetchall():
    t=r["text"] or ""
    s=" ".join(t.split())
    reason=None
    if any(x in t for x in junk):
        reason="web-scraper/sidebar contamination"
    elif page_nav.fullmatch(s):
        reason="web page navigation label"
    elif stars.fullmatch(s):
        reason="separator made only of asterisks"
    if reason:
        con.execute("delete from blocks where block_index=?",(r["block_index"],))
        removed.append((r["block_index"],r["section_id"],reason,s[:180]))

fixes={
72:"طَالِبٌ (какой-то) студент الطَّالِبُ (известный) студент",
454:"1) Введите в предложения подходящие переходные глаголы, данные внизу, и определите прямое дополнение (المَفْعُولُ بِهِ):",
503:"«Идафа»",
835:"1) البَيْتُ جَمِيلٌ Дом красив",
837:"2) الكِتَابُ مُمْتِعٌ Книга интересная",
839:"3) زَيْدٌ أَسَدٌ Зейд лев",
843:"4) الْبَيْتُ كَبِيرٌ Дом большой",
845:"5) الامْتِحَانُ سَهْلٌ Экзамен лёгкий",
847:"6) الشِّتَاءُ قَرِيبٌ Зима близка",
1766:"مَا أَجْمَلَ السَّمَاءَ! или أَجْمِلْ بِالسَّمَاءِ! Как красиво небо! (корнем глагола удивления является جَمُلَ– «быть красивым»)",
1767:"مَا أَقْبَحَ هَذَا الوَجْهَ! или أَقْبِحْ بِهَذَا الْوَجْهِ! Как некрасиво это лицо! (корнем глагола удивления является قَبُحَ «быть некрасивым»)",
1790:"2) Определите, какие из следующих глаголов пригодны для образования форм «удивления»:",
1874:"«Сопутствующее» имя",
1912:"(в основе: لاَ تَعْبُدْ أَحَداً إلاَّ اللَّهَ)",
4675:"2) При присоединении к глаголу «тэ» женского рода (تَاءُ التَّأْنِيثِ) последняя коренная буква усекается, если она является ا ،ى «алифом» (см. в глаголах رَمَتْ, دَعَتْ).",
5113:"называют «нун»ом женского рода (نُونُ النِّسْوَةِ). Эти два глагола являются неспрягаемыми (مَبْنِيٌّ), т.е. сохраняют одну и ту же форму во всех наклонениях.",
7042:"(جَمْعُ المُؤَنَّثِ السَّالِم) может указывать как на лиц женского пола, так и на неодушевлённые предметы. Например:",
7055:"(образованы по формуле فَعَلَةٌ и فُعَّالٌ)",
7111:"«Пять имен»",
7114:"1) أَبٌ отец",
7115:"2) أَخٌ брат",
7116:"3) ذُو имеющий, обладающий",
7117:"4) حَمٌ тесть, свёкор, свояк",
7118:"5) فُو рот",
7432:"اِحْمَرَّ краснеть يَحْمَرُّ он краснеет مُحْمَرُّ краснеющий (В основе: مُحْمَرِرٌ. Произошло слияние второй и третьей букв).",
}
for idx,new in fixes.items():
    r=con.execute("select text from blocks where block_index=?",(idx,)).fetchone()
    if not r:
        raise SystemExit(f"missing block {idx}")
    con.execute("update blocks set text=? where block_index=?",(new,idx))

r=con.execute("select text from blocks where block_index=0").fetchone()
if r:
    t=r["text"].replace("Nbsp;\n\n","",1)
    t=re.sub(r"(?m)^\*([^*\n]+)\*$",r"\1",t)
    con.execute("update blocks set text=? where block_index=0",(t,))

def norm(t):
    if not t: return t
    out=[]
    for line in t.splitlines():
        s=line.replace("\u00A0"," ")
        s=re.sub(r"[ \t]+([,.;:!?])",r"\1",s)
        s=re.sub(r"\(\s+","(",s)
        s=re.sub(r"\s+\)",")",s)
        s=re.sub(r"«\s+","«",s)
        s=re.sub(r"\s+»","»",s)
        s=re.sub(r"§\s*\.\s*","§ ",s)
        s=re.sub(r"§(?=\d)","§ ",s)
        s=re.sub(r"^([0-9]+\))(?=\S)",r"\1 ",s)
        s=re.sub(r"^([А-Яа-яA-Za-z]\))(?=\S)",r"\1 ",s)
        s=re.sub(r"([,;:!?])(?=[А-Яа-яЁёA-Za-z])",r"\1 ",s)
        s=re.sub(r"\)(?=[А-Яа-яЁёA-Za-z])",") ",s)
        s=re.sub(r"[ \t]{2,}"," ",s).strip()
        out.append(s)
    return "\n".join(out).strip()

changed=0
for r in con.execute("select block_index,text from blocks order by block_index").fetchall():
    old=r["text"] or ""
    new=norm(old)
    if new!=old:
        changed+=1
        con.execute("update blocks set text=? where block_index=?",(new,r["block_index"]))

for r in con.execute("select id,title from sections").fetchall():
    new=norm(r["title"] or "")
    if new!=r["title"]:
        con.execute("update sections set title=? where id=?",(new,r["id"]))

after=con.execute("select count(*) from blocks").fetchone()[0]
con.execute("update meta set value=? where key='blocks'",(str(after),))
con.commit()

known=[]
bad=[]
for r in con.execute("select block_index,section_id,text from blocks order by block_index"):
    t=r["text"] or ""
    s=" ".join(t.split())
    if any(x in t for x in junk) or page_nav.fullmatch(s) or stars.fullmatch(s):
        known.append((r["block_index"],r["section_id"],s[:160]))
    if "Nbsp;" in t or re.search(r"\(\s*\(",t):
        bad.append((r["block_index"],r["section_id"],s[:160]))

summary={
    "before_blocks":before,
    "after_blocks":after,
    "removed_blocks":len(removed),
    "normalized_blocks":changed,
    "remaining_known_junk":known,
    "remaining_high_signal_corruption":bad,
    "sha256":hashlib.sha256(db.read_bytes()).hexdigest(),
    "removed":removed,
}
if audit:
    audit.write_text(json.dumps(summary,ensure_ascii=False,indent=2),encoding="utf-8")
print(json.dumps({k:v for k,v in summary.items() if k!="removed"},ensure_ascii=False,indent=2))
con.close()
