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
assert 'renderTaskModeQuestions(mode,filter,true)' in nav
assert 'for(int start=0;start<list.size();start+=5)' not in nav, "All 555 cannot be rendered in one huge overview grid"
assert 'private void renderTaskModeQuestions(String mode,int filter,boolean push)' in src
assert 'private void appendTaskQuestionCards(' in src
assert 'navigatorQuestionLabel(' in src
assert 'taskQuestionStatus(' in src
assert 'renderTaskModeQuestions(mode,filter,false)' in src, "Filter action must stay in selected quiz mode"
assert 'min(24' in src or 'Math.min(24' in src, "Questions should render in small paginated batches"
assert 'openNavigatorQuestion(mode,q.index)' in src, "Question click must preserve exact chosen mode and index"
assert 'private void openNavigatorQuestion(String mode,int index)' in src
assert 'case"taskModeQuestions":' in src, "Back navigation must restore quiz mode and filter"
assert '"analytics".equals(type)' in src and 'renderTaskNavigator(0,true)' in src.split('private void openContents(',1)[1].split('private LinearLayout dialogSheet(',1)[0]
assert 'showContentsDialog();' in src.split('private void openContents(',1)[1].split('private LinearLayout dialogSheet(',1)[0], "Other screens retain existing TOCs"
assert '"Задания"' in src.split('private void updateContentsTabLabel(',1)[1].split('private void navBtn(',1)[0]
print("Quiz navigator UX contracts OK: 13 modes, named paged cards, status keys, analytics contextual navigation")
