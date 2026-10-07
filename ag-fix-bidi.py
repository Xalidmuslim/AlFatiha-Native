from pathlib import Path
import sqlite3, re, sys, json, hashlib

if len(sys.argv) < 2:
    raise SystemExit("usage: ag-fix-bidi.py <book.db> [audit.json]")

db=Path(sys.argv[1])
audit=Path(sys.argv[2]) if len(sys.argv)>2 else None
con=sqlite3.connect(str(db))
con.row_factory=sqlite3.Row

AR=r"\u0600-\u06FF\u0750-\u077F\u08A0-\u08FF"
arabic=re.compile("["+AR+"]")
cyrillic=re.compile(r"[А-Яа-яЁё]")
# Only Arabic verb vowel markers used by the book: (а), (и), (у), and slash-combinations.
pattern=re.compile(r"^(["+AR+r"\s]+)\(([аиу](?:/[аиу])?)\)\s+(.+)$",re.I)

def fix_line(line:str):
    m=pattern.match(line.strip())
    if not m:
        return None
    first_ar=m.group(1).rstrip()
    marker=m.group(2)
    rest=m.group(3).lstrip()

    # Always separate the first Arabic form from its Russian vowel-marker/translation.
    next_ar=arabic.search(rest)
    if not next_ar:
        return f"{first_ar}\n({marker}) {rest}"

    before_next=rest[:next_ar.start()].rstrip()
    suffix=rest[next_ar.start():].lstrip()

    # If the Russian explanation already opened a parenthetical clause, keep the embedded
    # Arabic inside that explanation instead of breaking its brackets apart.
    if not before_next or "(" in before_next or ")" in before_next or len(before_next)>80:
        return f"{first_ar}\n({marker}) {rest}"

    # Common morphology card: Arabic verb + marker/translation + Arabic derivative + translation.
    next_ru=cyrillic.search(suffix)
    if next_ru:
        second_ar=suffix[:next_ru.start()].rstrip()
        second_ru=suffix[next_ru.start():].lstrip()
        if second_ar:
            return f"{first_ar}\n({marker}) {before_next}\n{second_ar}\n{second_ru}"
    return f"{first_ar}\n({marker}) {before_next}\n{suffix}"

changed=[]
for row in con.execute("select block_index,section_id,type,text from blocks order by block_index").fetchall():
    old=row["text"] or ""
    out=[]
    row_changed=False
    for line in old.splitlines():
        fixed=fix_line(line)
        if fixed is not None and fixed != line:
            out.append(fixed)
            row_changed=True
        else:
            out.append(line)
    new="\n".join(out)
    if row_changed and new != old:
        con.execute("update blocks set text=? where block_index=?", (new,row["block_index"]))
        changed.append({
            "block_index": row["block_index"],
            "section_id": row["section_id"],
            "type": row["type"],
            "before": old,
            "after": new,
        })

con.commit()

# No Arabic-leading line may still contain a vowel marker attached to the Arabic run.
remaining=[]
for row in con.execute("select block_index,section_id,type,text from blocks order by block_index"):
    for line in (row["text"] or "").splitlines():
        if pattern.match(line.strip()):
            remaining.append((row["block_index"],row["section_id"],row["type"],line))
con.close()

summary={
    "changed_blocks": len(changed),
    "remaining_attached_vowel_markers": len(remaining),
    "remaining_examples": remaining[:20],
    "example_changes": changed[:40],
    "book_db_sha256": hashlib.sha256(db.read_bytes()).hexdigest(),
}
if audit:
    audit.write_text(json.dumps(summary,ensure_ascii=False,indent=2),encoding="utf-8")
print(json.dumps({k:v for k,v in summary.items() if k!="example_changes"},ensure_ascii=False,indent=2))

if len(changed) < 20:
    raise SystemExit(f"unexpectedly few bidi fixes: {len(changed)}")
if remaining:
    raise SystemExit(f"attached vowel markers remain: {remaining[:5]}")
