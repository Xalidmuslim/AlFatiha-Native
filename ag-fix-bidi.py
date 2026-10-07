from pathlib import Path
import sqlite3, re, sys, json, hashlib

if len(sys.argv) < 2:
    raise SystemExit("usage: ag-fix-bidi.py <book.db> [audit.json]")

db=Path(sys.argv[1])
audit=Path(sys.argv[2]) if len(sys.argv)>2 else None
con=sqlite3.connect(str(db))
con.row_factory=sqlite3.Row

arabic=re.compile(r"[\u0600-\u06FF\u0750-\u077F\u08A0-\u08FF]")
pattern=re.compile(r"^([\u0600-\u06FF\u0750-\u077F\u08A0-\u08FF\s]+)\(([А-Яа-яЁё]{1,3})\)\s+(.+)$")
changed=[]

for row in con.execute("select block_index,section_id,type,text from blocks where type='mixed' order by block_index").fetchall():
    old=row["text"] or ""
    out=[]
    row_changed=False
    for line in old.splitlines():
        m=pattern.match(line.strip())
        # Only split the safe case: one Arabic token/run + Russian vowel marker + Russian explanation.
        # If the remainder contains Arabic, keep the original because it may be a genuine mixed construction.
        if m and not arabic.search(m.group(3)):
            out.append(m.group(1).rstrip())
            out.append(f"({m.group(2)}) {m.group(3).lstrip()}")
            row_changed=True
        else:
            out.append(line)
    new="\n".join(out)
    if row_changed and new != old:
        con.execute("update blocks set text=? where block_index=?", (new,row["block_index"]))
        changed.append({
            "block_index": row["block_index"],
            "section_id": row["section_id"],
            "before": old,
            "after": new,
        })

con.commit()

# High-signal post-check: the safe pattern must no longer remain.
remaining=[]
for row in con.execute("select block_index,section_id,text from blocks where type='mixed' order by block_index"):
    for line in (row["text"] or "").splitlines():
        m=pattern.match(line.strip())
        if m and not arabic.search(m.group(3)):
            remaining.append((row["block_index"],row["section_id"],line))
con.close()

summary={
    "changed_blocks": len(changed),
    "remaining_safe_bidi_patterns": len(remaining),
    "remaining_examples": remaining[:20],
    "example_changes": changed[:30],
    "book_db_sha256": hashlib.sha256(db.read_bytes()).hexdigest(),
}
if audit:
    audit.write_text(json.dumps(summary,ensure_ascii=False,indent=2),encoding="utf-8")
print(json.dumps({k:v for k,v in summary.items() if k!="example_changes"},ensure_ascii=False,indent=2))

if len(changed) < 250:
    raise SystemExit(f"unexpectedly few bidi fixes: {len(changed)}")
if remaining:
    raise SystemExit(f"bidi patterns remain: {remaining[:5]}")
