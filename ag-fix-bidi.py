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
marker=re.compile(r"\(([аиу](?:/[аиу])?)\)",re.I)
same_line=re.compile(r"^(["+AR+r"\s]+)\(([аиу](?:/[аиу])?)\)\s+(.+)$",re.I)
marker_line=re.compile(r"^\(([аиу](?:/[аиу])?)\)\s+(.+)$",re.I)

def split_compound(first_ar, mark, rest):
    rest=rest.strip()
    next_ar=arabic.search(rest)
    if not next_ar:
        return f"{first_ar}\n{mark} {rest}"

    before_next=rest[:next_ar.start()].rstrip()
    suffix=rest[next_ar.start():].lstrip()
    # Keep explanatory parentheses/embedded Arabic intact when this is not a simple card pair.
    if not before_next or "(" in before_next or ")" in before_next or len(before_next)>80:
        return f"{first_ar}\n{mark} {rest}"

    next_ru=cyrillic.search(suffix)
    if next_ru:
        second_ar=suffix[:next_ru.start()].rstrip()
        second_ru=suffix[next_ru.start():].lstrip()
        if second_ar:
            return f"{first_ar}\n{mark} {before_next}\n{second_ar}\n{second_ru}"
    return f"{first_ar}\n{mark} {before_next}\n{suffix}"

changed=[]
for row in con.execute("select block_index,section_id,type,text from blocks order by block_index").fetchall():
    old=row["text"] or ""
    lines=old.splitlines()
    out=[]
    row_changed=False
    previous_arabic=False

    for line in lines:
        stripped=line.strip()

        m=same_line.match(stripped)
        if m:
            fixed=split_compound(m.group(1).rstrip(),m.group(2),m.group(3))
            out.extend(fixed.splitlines())
            row_changed=True
            previous_arabic=False
            continue

        ml=marker_line.match(stripped)
        if ml and previous_arabic:
            # The marker belongs to the Arabic verb on the previous line.
            out.append(f"{ml.group(1)} {ml.group(2).lstrip()}")
            row_changed=True
            previous_arabic=False
            continue

        out.append(line)
        previous_arabic = bool(stripped) and bool(arabic.search(stripped)) and not bool(cyrillic.search(stripped))

    new="\n".join(out)
    if row_changed and new != old:
        con.execute("update blocks set text=? where block_index=?", (new,row["block_index"]))
        changed.append({
            "block_index":row["block_index"],
            "section_id":row["section_id"],
            "type":row["type"],
            "before":old,
            "after":new,
        })

con.commit()

# Audit only the card-like defect: an Arabic-only line immediately followed by a parenthesized vowel marker,
# or an Arabic-leading line with that marker still attached.
remaining=[]
for row in con.execute("select block_index,section_id,type,text from blocks order by block_index"):
    lines=(row["text"] or "").splitlines()
    for i,line in enumerate(lines):
        s=line.strip()
        if same_line.match(s):
            remaining.append((row["block_index"],row["section_id"],row["type"],s))
        if i>0 and marker_line.match(s):
            prev=lines[i-1].strip()
            if prev and arabic.search(prev) and not cyrillic.search(prev):
                remaining.append((row["block_index"],row["section_id"],row["type"],s))
con.close()

summary={
    "changed_blocks":len(changed),
    "remaining_card_marker_parentheses":len(remaining),
    "remaining_examples":remaining[:30],
    "example_changes":changed[:40],
    "book_db_sha256":hashlib.sha256(db.read_bytes()).hexdigest(),
}
if audit:
    audit.write_text(json.dumps(summary,ensure_ascii=False,indent=2),encoding="utf-8")
print(json.dumps({k:v for k,v in summary.items() if k!="example_changes"},ensure_ascii=False,indent=2))

if len(changed) < 300:
    raise SystemExit(f"unexpectedly few marker fixes: {len(changed)}")
if remaining:
    raise SystemExit(f"card marker parentheses remain: {remaining[:5]}")
