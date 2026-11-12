#!/usr/bin/env python3
"""Regression for mode-specific quiz browsing and context-sensitive progress navigation."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=(root/"app/src/main/java/app/alfatiha/tafsir/MainActivity.java").read_text(encoding="utf-8")
nav=src.split('private void renderTaskNavigator(',1)[1].split('private void repeatMistakes(',1)[0]
assert 'private static final String[] NAVIGATOR_QUIZ_MODES' in src
modes=src.split('private static final String[] NAVIGATOR_QUIZ_MODES',1)[1].split('};',1)[0]
for mode in ('classic','alfatiha_medium','multi','match','hadith','free','expert','prayer_medium','prayer_hard','mind_quick','mind_exam','mind_mistakes','mind_life'):
    assert '"'+mode+'"' in modes, "Missing real quiz mode "+mode
assert 'renderTaskModeQuestions(mode,selectedFilter,true)' in nav
assert 'for(int start=0;start<list.size();start+=5)' not in nav, "All 555 cannot be rendered in one huge overview grid"
assert 'page.addView(actions);' in src
profile=src.split('private void renderProfile(boolean push)',1)[1].split('private View profileRing(',1)[0]
assert profile.index('page.addView(actions);') < profile.index('Button taskNavigator=outline("Разделы викторин') < profile.index('TextView sections=text("Освоение по разделам"'), "Mode navigator shortcut must be visible above the long progress meters"
assert 'private void renderTaskModeQuestions(String mode,int filter,boolean push)' in src
assert 'private int appendTaskQuestionCards(' in src
assert 'navigatorQuestionLabel(' in src
assert 'taskQuestionStatus(' in src
assert 'renderTaskModeQuestions(mode,f,false)' in src, "Filter action must stay in selected quiz mode"
assert 'appendTaskQuestionCards(holder,visible,mode,next[0],24,model)' in src, "Questions should render in batches of 24"
assert 'openNavigatorQuestion(mode,q.index)' in src, "Question click must preserve exact chosen mode and index"
assert 'entry.stateIds.addAll(ref.stateIds)' in src, "Canonical analytics aliases must remain shared across modes"
assert 'private void openNavigatorQuestion(String mode,int index)' in src
assert 'case"taskModeQuestions":' in src, "Back navigation must restore quiz mode and filter"
assert "taskNavigatorVisibleCounts.put(pageKey,next[0])" in src and "restoreCount" in src, "Back must reconstruct all previously loaded batches before restoring saved scroll"
assert '"analytics".equals(type)' in src and 'renderTaskNavigator(0,true)' in src.split('private void openContents(',1)[1].split('private LinearLayout dialogSheet(',1)[0]
assert 'showContentsDialog();' in src.split('private void openContents(',1)[1].split('private LinearLayout dialogSheet(',1)[0], "Other screens retain existing TOCs"
assert '"Задания"' in src.split('private void updateContentsTabLabel(',1)[1].split('private void navBtn(',1)[0]
print("Quiz navigator UX contracts OK: 13 modes, named paged cards, status keys, analytics contextual navigation")
