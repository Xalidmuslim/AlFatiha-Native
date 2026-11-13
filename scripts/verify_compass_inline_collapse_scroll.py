#!/usr/bin/env python3
"""Guard against the abrupt ScrollView clamp on inline panel close."""
from pathlib import Path
p=Path(__file__).resolve().parents[1]
src=(p/"app/src/main/java/app/alfatiha/tafsir/MainActivity.java").read_text(encoding="utf-8")
start=src.index("private void toggleInline(View holder,Button b,String closed,String open){")
end=src.index("private void addDeep(",start)
handler=src[start:end]
assert "InlineDisclosureScrollTarget.whenCollapsing(" in handler, "Compute a position legal AFTER the panel shrinks"
assert "ValueAnimator.ofInt(startY,targetY)" in handler, "Scroll back smoothly before the panel vanishes"
assert "scroll.scrollTo(0,(Integer)frame.getAnimatedValue())" in handler, "Animate ScrollView's actual scroll position"
assert "else completeCollapse.run()" in handler
assert "move.addListener(new android.animation.AnimatorListenerAdapter()" in handler
assert "if(cancelled)b.setEnabled(true)" in handler, "Interrupted scroll cannot leave a button disabled"
assert handler.index("completeCollapse.run();") > handler.index("ValueAnimator.ofInt(startY,targetY)")
assert "holder.setVisibility(View.GONE)" in handler
assert "Math.abs(startY-targetY)>dp(3)" in handler
assert "morphAnimationsEnabled()" in handler, "Respect reduced-motion settings"
assert "cancelInlineCollapseScroll();" in src.split("private void clear(",1)[1].split("private TextView chromeText",1)[0]
assert "cancelInlineCollapseScroll();" in src.split("private void buildShell()",1)[1].split("private int",1)[0]
uses=src.count("toggleInline(")-1
assert uses >= 12, f"Shared fix no longer covers prior 12+ disclosures: {uses}"
for label in ("addQudsiDisclosure(", "addPrayerMeaningDetails(", "addPrayerSources(", "renderMindConnections(", "addMindHeartInline(", "addAllExplanations("):
    assert label in src, label
print(f"Native disclosure scroll anchor, reduced-motion safety, and {uses} shared calls: OK")
