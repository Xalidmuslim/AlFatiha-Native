from pathlib import Path
import re, shutil

p=Path('app/src/main/java/app/alfatiha/tafsir/MainActivity.java')
if not p.exists(): raise SystemExit('Запусти команду из корня репозитория: MainActivity.java не найден')
s=p.read_text(encoding='utf-8')
backup=Path('/tmp/MainActivity.java.before_full_course')
if not backup.exists(): shutil.copy2(p,backup)

def span(sig):
    a=s.find(sig)
    if a<0: raise SystemExit('Не найден метод: '+sig)
    b=s.find('{',a); d=0; i=b; state='code'
    while i<len(s):
        c=s[i]; n=s[i+1] if i+1<len(s) else ''
        if state=='code':
            if c=='"': state='str'
            elif c=="'": state='char'
            elif c=='/' and n=='/': state='line'; i+=1
            elif c=='/' and n=='*': state='block'; i+=1
            elif c=='{': d+=1
            elif c=='}':
                d-=1
                if d==0: return a,i+1
        elif state=='str':
            if c=='\\': i+=1
            elif c=='"': state='code'
        elif state=='char':
            if c=='\\': i+=1
            elif c=="'": state='code'
        elif state=='line':
            if c=='\n': state='code'
        elif state=='block':
            if c=='*' and n=='/': state='code'; i+=1
        i+=1
    raise SystemExit('Не закрыт метод: '+sig)

def meth(sig,code):
    global s
    a,b=span(sig); s=s[:a]+code.strip()+s[b:]
    print('✓',sig)

def inside(sig,old,new):
    global s
    a,b=span(sig); part=s[a:b]
    if old not in part:
        print('• уже изменено/не найдено:',old[:45]); return
    s=s[:a]+part.replace(old,new,1)+s[b:]
    print('✓ дополнен',sig)

# Системные отступы + dark UI
s=s.replace('page.setPadding(dp(18),dp(10),dp(18),dp(110));','page.setPadding(dp(18),dp(10),dp(18),dp(136));')
s=s.replace('        setContentView(root);\n        buildBottom();','        setContentView(root);\n        if(Build.VERSION.SDK_INT>=20)root.post(()->root.requestApplyInsets());\n        buildBottom();')
s=s.replace('TextView mark=text("ف",26,C_SAGE,true);','TextView mark=text("ف",26,dark?blend(C_SAGE,Color.WHITE,.46f):C_SAGE,true);')

meth('private TextView kicker(String s,int color)',r'''
    private TextView kicker(String s,int color){
        int tc=dark?blend(color,Color.WHITE,.48f):color;
        TextView t=text(s,11.5f,tc,true);t.setLetterSpacing(.06f);t.setAllCaps(true);
        int fill=dark?blend(color,Color.BLACK,.58f):blend(color,Color.WHITE,.70f);
        int border=dark?blend(color,Color.WHITE,.34f):blend(color,Color.WHITE,.55f);
        t.setBackground(solidBg(fill,99,border));t.setPadding(dp(10),dp(6),dp(10),dp(6));return t;
    }
''')

# Общие helpers
anchor='    private void clear(String type,String arg,boolean push)'
helpers=r'''
    private Button smallAction(String label){Button b=outline(label);b.setTextSize(sz(11.2f));b.setSingleLine(true);b.setMinWidth(0);b.setMinimumWidth(0);return b;}
    private void copyText(String x){ClipboardManager c=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);if(c!=null){c.setPrimaryClip(ClipData.newPlainText("Аль-Фатиха",x));toast("Скопировано");}}
    private void shareText(String x){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,x);startActivity(Intent.createChooser(i,"Поделиться"));}
    private LinearLayout shareRow(String x){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);r.setPadding(0,dp(8),0,dp(2));Button c=smallAction("⧉ Копировать"),h=smallAction("↗ Поделиться");c.setOnClickListener(v->copyText(x));h.setOnClickListener(v->shareText(x));r.addView(c,new LinearLayout.LayoutParams(0,dp(46),1));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(46),1);lp.setMargins(dp(7),0,0,0);r.addView(h,lp);return r;}
    private void markDone(Button b,String label){b.setText(label);b.setTextColor(muted());b.setBackground(surfaceBg(dark?Color.rgb(43,49,46):Color.rgb(241,240,235),dark?Color.rgb(39,45,42):Color.rgb(247,245,240),18,line()));b.setAlpha(.68f);b.setEnabled(false);}
    private boolean isMindMode(String m){return "mind_quick".equals(m)||"mind_exam".equals(m);}
    private JSONArray quickMindArray(){JSONArray a=arr("mind_exam.json"),o=new JSONArray();Set<Integer> ids=new HashSet<>(Arrays.asList(1,2,3,4,5,6,7,8,9,11,12,13,14,15,20));for(int i=0;i<a.length();i++){JSONObject q=a.optJSONObject(i);if(q!=null&&ids.contains(q.optInt("id",-1)))o.put(q);}return o;}
    private ArrayAdapter<String> themedSpinnerAdapter(ArrayList<String> vals){ArrayAdapter<String> a=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,vals){private TextView tune(TextView t,boolean drop){t.setTextColor(ink());t.setTextSize(sz(15));t.setPadding(dp(12),dp(12),dp(12),dp(12));if(drop)t.setBackgroundColor(panel());else t.setBackgroundColor(Color.TRANSPARENT);return t;}@Override public View getView(int p,View v,ViewGroup g){return tune((TextView)super.getView(p,v,g),false);}@Override public View getDropDownView(int p,View v,ViewGroup g){return tune((TextView)super.getDropDownView(p,v,g),true);}};a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);return a;}
    private String lessonText(JSONObject o){return o.optString("t")+"\n"+o.optString("ru")+"\n\nПонять смысл:\n"+o.optString("meaning")+"\n\nЧто должно быть в сердце:\n"+o.optString("heart")+"\n\nВо время намаза:\n"+o.optString("prompt")+"\n\nИсточник: "+o.optString("src");}
    private String questionText(JSONObject q,String mode){StringBuilder b=new StringBuilder(qText(q,mode));JSONArray a=optionsAsArray(q,mode);if(a.length()>0){b.append("\n\nВарианты:");for(int i=0;i<a.length();i++)b.append("\n").append(i+1).append(". ").append(a.optString(i));}return b.toString();}
    private String answerText(JSONObject q,String mode,int correct){StringBuilder b=new StringBuilder(questionText(q,mode));JSONArray a=optionsAsArray(q,mode);if(correct>=0&&correct<a.length())b.append("\n\nПравильный ответ: ").append(correct+1).append(". ").append(a.optString(correct));String e=explanation(q,mode,correct);if(!e.isEmpty())b.append("\n\nРазбор:\n").append(e);String src=sources(q);if(!src.isEmpty())b.append("\n\nИсточник: ").append(src);return b.toString();}
'''
if 'private JSONArray quickMindArray()' not in s:
    s=s.replace(anchor,helpers+'\n'+anchor,1)

# Хаб 01–06
meth('private void renderMindHub(boolean push)',r'''
    private void renderMindHub(boolean push){
        clear("mindHub","",push);currentSection="mind";appTop();
        header("Осознанное чтение Аль-Фатихи","Практический курс: понять смысл, связать его с состоянием сердца, потренироваться и проверить усвоение.");
        LinearLayout intro=card(sandSoft());intro.addView(kicker("ВАЖНОЕ ПРЕДИСЛОВИЕ",Color.rgb(145,104,42)));intro.addView(text("Зачем читать Аль-Фатиху осознанно",20.5f,ink(),true));intro.addView(text("Почему важно не только произносить слова, но понимать, признавать сердцем и действительно обращаться к Аллаху.",14,muted(),false));intro.setOnClickListener(v->renderIntro(true));
        courseStep("01","Разбор Аль-Фатихи","8 смысловых частей: слова → смысл → состояние сердца → применение в намазе.",C_BLUE,()->renderMindLesson(0,true),false);
        courseStep("02","Медленное чтение","Читайте по одному аяту: произнесите аят целиком медленно, сделайте паузу 3–5 секунд, поразмышляйте над смыслом и тем, к чему он вас обязывает.",Color.rgb(145,104,42),()->renderMindSlow(0,true),false);
        courseStep("03","Одна мысль для намаза","Выберите один смысл и постарайтесь удержать его в следующей молитве.",Color.rgb(112,96,134),()->renderMindFocus(0,true),false);
        courseStep("04","Тренировка без подсказок","Подсказок становится меньше, пока смысл не удерживается самостоятельно.",C_SAGE,()->renderMindStages(0,1,true),false);
        courseStep("05","Проверка понимания","15 заданий по ключевым смыслам, состояниям сердца и практическому применению.",C_BLUE,()->renderNativeQuiz("mind_quick",prefs.getInt("idx_mind_quick",0),true),false);
        boolean lock=seenMindCount()<8;courseStep("06","Итоговый экзамен",lock?("Откроется после основного урока · сейчас "+seenMindCount()+"/8."):"30 заданий повышенной сложности и диагностика слабых мест.",Color.rgb(145,104,42),()->{if(lock)toast("Сначала пройдите все 8 смысловых частей");else renderNativeQuiz("mind_exam",prefs.getInt("idx_mind_exam",0),true);},lock);
    }
''')

course=r'''
    private void courseStep(String n,String title,String sub,int accent,Runnable go,boolean locked){LinearLayout c=card(panel());LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER_VERTICAL);TextView num=text(n,15,dark?blend(accent,Color.WHITE,.45f):accent,true);num.setGravity(Gravity.CENTER);num.setBackground(solidBg(dark?blend(accent,Color.BLACK,.58f):blend(accent,Color.WHITE,.82f),14,line()));row.addView(num,new LinearLayout.LayoutParams(dp(48),dp(48)));LinearLayout tx=new LinearLayout(this);tx.setOrientation(LinearLayout.VERTICAL);tx.setPadding(dp(12),0,0,0);tx.addView(text(title,18,ink(),true));tx.addView(text(sub,13.5f,muted(),false));row.addView(tx,new LinearLayout.LayoutParams(0,-2,1));c.addView(row);c.setAlpha(locked ? .68f : 1f);c.setOnClickListener(v->go.run());}
    private String[][] slowPrompts(){return new String[][]{
      {"Какую хвалу я сейчас действительно обращаю к Аллаху? Замечаю ли я Его совершенство и хотя бы одно благо, которое обычно воспринимаю как привычное?","Признать, что благо не возникло само по себе, благодарить Аллаха и не произносить хвалу как пустую привычную формулу."},
      {"Я произношу имена милости. В какой милости Аллаха я особенно нуждаюсь сейчас — в прощении, исправлении, принятии поклонения или благом исходе?","Не отчаиваться в милости Аллаха, обращаться к Нему с надеждой и искать Его милости через покаяние и поклонение."},
      {"Если бы мне пришлось отвечать за мои дела, что я хотел бы исправить уже сегодня? Как этот намаз выглядит перед Днём расчёта?","Помнить об ответственности перед Аллахом, исправлять поступки сейчас и относиться к поклонению серьёзно, а не формально."},
      {"Для Кого я сейчас стою в намазе? Есть ли в сердце что-то, что отвлекает меня от искреннего поклонения Аллаху одному?","Возвращать намерение к Аллаху, очищать поклонение от показного и не превращать намаз в одни движения."},
      {"В чём прямо сейчас я особенно нуждаюсь в помощи Аллаха — в самом намазе, в оставлении греха, в терпении, знании или стойкости?","Не полагаться только на себя: просить помощи Аллаха и признавать свою зависимость от Него даже в совершении благого."},
      {"Где именно мне сегодня нужно наставление: что узнать, что принять, что сделать, что оставить или в чём укрепиться?","Искать руководство Аллаха не только как информацию, а принимать истину, поступать по ней и просить стойкости."},
      {"Чей путь я прошу для себя? Хочу ли я только знать истину — или действительно жить так, как живут те, кого Аллах облагодетельствовал?","Соединять веру, знание и повиновение; стремиться не к собственному удобному пути, а к пути тех, кто следует истине."},
      {"Есть ли истина, которую я уже знаю, но откладываю? И нет ли у меня действий или суждений в религии без достаточного знания?","Соединять правильное знание с действием: не оставлять известную истину и не действовать в религии без руководства."}};}
    private LinearLayout practiceCard(String n,String title,String body,int tone,int accent){LinearLayout c=card(tone);LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);r.setGravity(Gravity.TOP);TextView x=text(n,14,accent,true);x.setGravity(Gravity.CENTER);x.setBackground(solidBg(panel(),13,line()));r.addView(x,new LinearLayout.LayoutParams(dp(42),dp(42)));LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.setPadding(dp(11),0,0,0);t.addView(text(title,16,ink(),true));t.addView(text(body,14.5f,muted(),false));r.addView(t,new LinearLayout.LayoutParams(0,-2,1));c.addView(r);return c;}
    private void renderMindSlow(int idx,boolean push){JSONArray a=arr("mind_data.json");if(idx<0||idx>=a.length())idx=0;clear("mindSlow",String.valueOf(idx),push);currentSection="mind";appTop();JSONObject q=a.optJSONObject(idx);String[][] p=slowPrompts();header("Медленное чтение","Практика "+(idx+1)+" из "+a.length());LinearLayout ay=card(panel());ay.addView(kicker("АЯТ И ПЕРЕВОД",C_BLUE));ay.addView(text(q.optString("t"),21,ink(),true));ay.addView(text(q.optString("ru"),14.5f,muted(),false));practiceCard("1","Прочитай весь аят медленно","Прочитай аят целиком спокойно и без спешки, стараясь понимать, что ты сейчас произносишь.",panel(),C_SAGE);practiceCard("•","После аята остановись на 3–5 секунд","Ничего не произноси. Дай смыслу аята закрепиться в сознании, прежде чем переходить к следующему.",sandSoft(),Color.rgb(145,104,42));practiceCard("2","Размышляй над тем, что произнёс",p[idx][0],blueSoft(),C_BLUE);practiceCard("3","К чему меня обязывает этот смысл?",p[idx][1],sandSoft(),Color.rgb(145,104,42));practiceCard("4","Прочитай аят ещё раз","Повтори его медленно, уже удерживая смысл и практический вывод. Затем переходи к следующему аяту.",sageSoft(),C_SAGE);page.addView(shareRow(q.optString("t")+"\n"+q.optString("ru")+"\n\n"+p[idx][0]+"\n\n"+p[idx][1]));LinearLayout nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);if(idx>0){Button b=outline("← Назад");int x=idx-1;b.setOnClickListener(v->renderMindSlow(x,true));nav.addView(b,new LinearLayout.LayoutParams(0,dp(54),1));}Button n=outline(idx==a.length()-1?"Завершить":"Следующий аят →");int x=idx+1;n.setOnClickListener(v->{if(x<a.length())renderMindSlow(x,true);else renderMindHub(true);});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(54),1);if(idx>0)lp.setMargins(dp(8),0,0,0);nav.addView(n,lp);page.addView(nav);}
    private void renderMindFocus(int idx,boolean push){JSONArray a=arr("mind_data.json");if(idx<0||idx>=a.length())idx=0;clear("mindFocus",String.valueOf(idx),push);currentSection="mind";appTop();JSONObject q=a.optJSONObject(idx);header("Одна мысль для намаза","На ближайшую молитву удерживайте только одну мысль.");LinearLayout c=card(sageSoft());c.addView(kicker("НА БЛИЖАЙШУЮ МОЛИТВУ",C_SAGE));c.addView(text(q.optString("t"),20,ink(),true));c.addView(text(q.optString("ru"),14,muted(),false));c.addView(text(q.optString("prompt"),17,ink(),true));c.addView(text("Не старайся удержать сразу все смыслы Аль-Фатихи. На этот намаз достаточно одной мысли — верни к ней сердце, когда произносишь эту часть.",13.5f,muted(),false));page.addView(shareRow(q.optString("t")+"\n"+q.optString("ru")+"\n\n"+q.optString("prompt")));Button b=action("Другая мысль",C_SAGE);int x=(idx+1)%a.length();b.setOnClickListener(v->renderMindFocus(x,true));page.addView(b);}
    private void renderMindStages(int idx,int stage,boolean push){JSONArray a=arr("mind_data.json");if(idx<0||idx>=a.length())idx=0;if(stage<1||stage>4)stage=1;clear("mindStages",idx+":"+stage,push);currentSection="mind";appTop();JSONObject q=a.optJSONObject(idx);header("Тренировка без подсказок","Этап "+stage+" из 4 · часть "+(idx+1)+" из "+a.length());LinearLayout c=card(panel());c.addView(kicker("ПРОЧИТАЙ ОСОЗНАННО",C_BLUE));c.addView(text(q.optString("t"),21,ink(),true));if(stage==1){c.addView(text(q.optString("ru"),14,muted(),false));c.addView(text(q.optString("heart"),14.5f,ink(),false));}else if(stage==2){c.addView(text(q.optString("ru"),14,muted(),false));c.addView(text("Ключ: "+q.optString("prompt"),14.5f,ink(),true));}else if(stage==3){JSONArray w=q.optJSONArray("words");StringBuilder z=new StringBuilder();for(int i=0;w!=null&&i<Math.min(2,w.length());i++){JSONArray e=w.optJSONArray(i);if(i>0)z.append(" · ");z.append(e.optString(0)).append(" — ").append(e.optString(1));}c.addView(text(z.toString(),14.5f,ink(),false));}else c.addView(text("Произнеси эту часть самостоятельно и удержи её смысл без подсказки. Затем проверь себя.",14.5f,ink(),false));int fi=idx,fs=stage;LinearLayout nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);Button prev=outline("← Назад");prev.setOnClickListener(v->{if(fi>0)renderMindStages(fi-1,fs,true);else if(fs>1)renderMindStages(a.length()-1,fs-1,true);else renderMindHub(true);});nav.addView(prev,new LinearLayout.LayoutParams(0,dp(54),1));Button next=outline(stage==4&&idx==a.length()-1?"Завершить":"Далее →");next.setOnClickListener(v->{if(fi<a.length()-1)renderMindStages(fi+1,fs,true);else if(fs<4)renderMindStages(0,fs+1,true);else renderMindHub(true);});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(54),1);lp.setMargins(dp(8),0,0,0);nav.addView(next,lp);page.addView(nav);}
'''
anchor2='    private void addParagraphs(LinearLayout parent,String body,float size)'
if 'private void courseStep(' not in s:s=s.replace(anchor2,course+'\n'+anchor2,1)

# Предисловие: копирование/поделиться
inside('private void renderIntro(boolean push)','Button b=action("Начать урок",C_SAGE);','StringBuilder sb=new StringBuilder();for(int j=0;j<a.length();j++){JSONObject x=a.optJSONObject(j);sb.append(x.optString("title")).append("\\n").append(x.optString("text")).append("\\n\\n");}page.addView(shareRow(sb.toString()));Button b=action("Начать урок",C_SAGE);')

# Урок: глубокий разбор с названиями + share
inside('private void renderMindLesson(int idx,boolean push)','sectionCard("Источник",o.optString("src"),panel(),C_SAGE);','sectionCard("Источник",o.optString("src"),panel(),C_SAGE);page.addView(shareRow(lessonText(o)));')
meth('private void addDeep(LinearLayout holder,JSONArray d)',r'''
    private void addDeep(LinearLayout holder,JSONArray d){
        String[] names={"ТАФСИР","ЧТО МОЖНО ИЗВЛЕЧЬ","РАЗМЫШЛЕНИЕ ДЛЯ СЕРДЦА","ПРАКТИКА В НАМАЗЕ","ОПОРА НА ИСТОЧНИКИ"};int[] cols={C_BLUE,Color.rgb(145,104,42),C_SAGE,C_BLUE,C_SAGE};
        for(int i=0;i<d.length()&&i<5;i++){LinearLayout c=newSurface(i==1?sandSoft():i==2?sageSoft():i==3?blueSoft():panel(),16,13,1);c.addView(kicker(names[i],cols[i]));Object x=d.opt(i);if(x instanceof JSONArray){JSONArray a=(JSONArray)x;for(int k=0;k<a.length();k++)c.addView(text("• "+a.optString(k),14.5f,ink(),false));}else addParagraphs(c,String.valueOf(x),14.5f);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(5),0,dp(5));holder.addView(c,lp);}
    }
''')

# Короткая проверка + итоговый экзамен
meth('private JSONArray quizArray(String mode)',r'''
    private JSONArray quizArray(String mode){switch(mode){case"classic":return arr("quiz_data.json");case"multi":return arr("multi_data.json");case"match":return arr("match_data.json");case"hadith":return arr("hadith_data.json");case"free":return arr("free_data.json");case"expert":return arr("expert_data.json");case"mind_quick":return quickMindArray();case"mind_exam":return arr("mind_exam.json");default:return new JSONArray();}}
''')
meth('private void renderNativeQuiz(String mode,int idx,boolean push)',r'''
    private void renderNativeQuiz(String mode,int idx,boolean push){JSONArray a=quizArray(mode);if(a.length()==0){toast("Нет данных");return;}if(idx<0||idx>=a.length())idx=0;clear("quiz",mode+":"+idx,push);currentSection=isMindMode(mode)?"mind":"quiz";prefs.edit().putInt("idx_"+mode,idx).putString("last_mode",mode).apply();appTop();LinearLayout meta=new LinearLayout(this);meta.setOrientation(LinearLayout.HORIZONTAL);meta.setGravity(Gravity.CENTER_VERTICAL);String lab="mind_quick".equals(mode)?"КОРОТКАЯ ПРОВЕРКА":"mind_exam".equals(mode)?"ИТОГОВЫЙ ЭКЗАМЕН":modeTitle(mode).toUpperCase(Locale.ROOT);meta.addView(kicker(lab,isMindMode(mode)?C_SAGE:C_BLUE),new LinearLayout.LayoutParams(0,-2,1));Button jump=outline((idx+1)+" / "+a.length()+"  ▾");int ci=idx;jump.setOnClickListener(v->showQuestionPicker(mode,ci,a.length()));meta.addView(jump,new LinearLayout.LayoutParams(dp(100),dp(44)));page.addView(meta);ProgressBar pb=progressBar((idx+1)*100/a.length(),isMindMode(mode)?C_SAGE:C_BLUE);LinearLayout.LayoutParams plp=new LinearLayout.LayoutParams(-1,dp(8));plp.setMargins(0,dp(8),0,dp(12));page.addView(pb,plp);JSONObject q=a.optJSONObject(idx);if(mode.equals("match")){renderMatch(q,mode,idx,a.length());return;}if(mode.equals("free")||(isMindMode(mode)&&"self".equals(q.optString("type")))){renderFree(q,mode,idx,a.length());return;}if(mode.equals("multi")){renderMulti(q,mode,idx,a.length());return;}renderMcq(q,mode,idx,a.length());}
''')
meth('private void showQuestionPicker(String mode,int currentIdx,int total)',r'''
    private void showQuestionPicker(String mode,int currentIdx,int total){final Dialog d=new Dialog(this);LinearLayout box=newSurface(panel(),24,15,0);box.addView(text("Перейти к заданию",20,ink(),true));GridView g=new GridView(this);g.setNumColumns(5);g.setHorizontalSpacing(dp(7));g.setVerticalSpacing(dp(7));g.setAdapter(new BaseAdapter(){public int getCount(){return total;}public Object getItem(int p){return p;}public long getItemId(int p){return p;}public View getView(int p,View v,ViewGroup parent){TextView t=v instanceof TextView?(TextView)v:text("",14,ink(),true);t.setGravity(Gravity.CENTER);t.setMinHeight(dp(48));t.setMinimumHeight(dp(48));t.setText(String.valueOf(p+1));boolean on=p==currentIdx;t.setTextColor(on?Color.WHITE:ink());t.setBackground(solidBg(on?C_SAGE:panel(),13,on?C_SAGE:line()));return t;}});box.addView(g,new LinearLayout.LayoutParams(-1,0,1));Button x=outline("Отмена");x.setOnClickListener(v->d.dismiss());box.addView(x,new LinearLayout.LayoutParams(-1,dp(50)));d.setContentView(box);g.setOnItemClickListener((a,v,p,id)->{d.dismiss();renderNativeQuiz(mode,p,true);});d.show();Window w=d.getWindow();if(w!=null){w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));int sw=getResources().getDisplayMetrics().widthPixels,sh=getResources().getDisplayMetrics().heightPixels;w.setLayout((int)(sw*.92f),(int)(sh*.78f));}}
''')
meth('private String modeTitle(String m)',r'''    private String modeTitle(String m){switch(m){case"classic":return"Классическая викторина";case"multi":return"Два правильных";case"match":return"Сопоставление";case"hadith":return"Хадис → смысл";case"free":return"Без вариантов";case"expert":return"Эксперт";case"mind_quick":return"Проверка понимания";case"mind_exam":return"Итоговый экзамен";}return"Викторина";}''')
meth('private String qText(JSONObject q,String mode)',r'''    private String qText(JSONObject q,String mode){if(mode.equals("hadith"))return q.optString("hadith")+"\n\n"+q.optString("question");if(isMindMode(mode))return q.optString("q");return q.optString("question");}''')
meth('private int correctIndex(JSONObject q,String mode)',r'''    private int correctIndex(JSONObject q,String mode){Object c=q.opt("correct");if(c instanceof Number)return((Number)c).intValue();if(c!=null){String z=String.valueOf(c);if(z.length()==1&&Character.isLetter(z.charAt(0)))return Character.toUpperCase(z.charAt(0))-'A';try{return Integer.parseInt(z);}catch(Exception e){}}if(isMindMode(mode))return q.optInt("a",-1);return-1;}''')
# Завершение обоих mind-режимов должно возвращать в курс
s=s.replace('mode.equals("mind_exam")','isMindMode(mode)')

# Сопоставление dark
inside('private void renderMatch(JSONObject q,String mode,int idx,int total)','ArrayAdapter<String> ad=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,values);sp.setAdapter(ad);','ArrayAdapter<String> ad=themedSpinnerAdapter(values);sp.setAdapter(ad);sp.setPopupBackgroundDrawable(solidBg(panel(),14,line()));')

# Поделиться вопросами и нормальное состояние кнопки
inside('private void renderMcq(JSONObject q,String mode,int idx,int total)','Button check=action("Проверить",C_SAGE);qc.addView(check);bookmarkButton(q,mode,qc);','Button check=action("Проверить",C_SAGE);qc.addView(check);bookmarkButton(q,mode,qc);qc.addView(shareRow(questionText(q,mode)));')
inside('private void renderMcq(JSONObject q,String mode,int idx,int total)','check.setEnabled(false);','markDone(check,"Ответ проверен");')
inside('private void renderMcq(JSONObject q,String mode,int idx,int total)','addAllExplanations(q,mode,result,opts.length());Button next=action','addAllExplanations(q,mode,result,opts.length());result.addView(shareRow(answerText(q,mode,correct)));Button next=action')
inside('private void renderMulti(JSONObject q,String mode,int idx,int total)','Button check=action("Проверить выбранные ответы",C_SAGE);qc.addView(check);bookmarkButton(q,mode,qc);','Button check=action("Проверить выбранные ответы",C_SAGE);qc.addView(check);bookmarkButton(q,mode,qc);qc.addView(shareRow(questionText(q,mode)));')
inside('private void renderMulti(JSONObject q,String mode,int idx,int total)','check.setEnabled(false);','markDone(check,"Ответ проверен");')
inside('private void renderMulti(JSONObject q,String mode,int idx,int total)','String src=sources(q);if(!src.isEmpty())r.addView(text("Источник: "+src,12.7f,muted(),false));Button next=action',r'String src=sources(q);if(!src.isEmpty())r.addView(text("Источник: "+src,12.7f,muted(),false));r.addView(shareRow(questionText(q,mode)+"\n\n"+(ok?"Ответ верный.":"Правильные варианты: "+numbers(cs))+(!q.optString("note").isEmpty()?"\n\n"+q.optString("note"):"")+(!src.isEmpty()?"\n\nИсточник: "+src:"")));Button next=action')
inside('private void renderMatch(JSONObject q,String mode,int idx,int total)','Button check=action("Проверить соответствие",C_SAGE);qc.addView(check);','Button check=action("Проверить соответствие",C_SAGE);qc.addView(check);qc.addView(shareRow(q.optString("title")));')
inside('private void renderMatch(JSONObject q,String mode,int idx,int total)','check.setEnabled(false);','markDone(check,"Ответ проверен");')
inside('private void renderMatch(JSONObject q,String mode,int idx,int total)','String src=sources(q);if(!src.isEmpty())r.addView(text("Источник: "+src,12.7f,muted(),false));Button next=action',r'String src=sources(q);if(!src.isEmpty())r.addView(text("Источник: "+src,12.7f,muted(),false));r.addView(shareRow(q.optString("title")+"\n\n"+(ok?"Все соответствия верны.":"В соответствиях есть неточности.")+(!src.isEmpty()?"\n\nИсточник: "+src:"")));Button next=action')
inside('private void renderFree(JSONObject q,String mode,int idx,int total)','qc.addView(ed,new LinearLayout.LayoutParams(-1,dp(150)));Button show=action','qc.addView(ed,new LinearLayout.LayoutParams(-1,dp(150)));qc.addView(shareRow(questionText(q,mode)));Button show=action')
inside('private void renderFree(JSONObject q,String mode,int idx,int total)','show.setEnabled(false);','markDone(show,"Эталон показан");')
inside('private void renderFree(JSONObject q,String mode,int idx,int total)','if(!key.isEmpty())r.addView(text("Ключ: "+key,13,muted(),false));r.addView(text("Оцените себя:",14.5f,ink(),true));',r'if(!key.isEmpty())r.addView(text("Ключ: "+key,13,muted(),false));r.addView(shareRow(questionText(q,mode)+"\n\nЭталон:\n"+model+(key.isEmpty()?"":"\n\nКлюч: "+key)));r.addView(text("Оцените себя:",14.5f,ink(),true));')

meth('private String sources(JSONObject q)',r'''
    private String sources(JSONObject q){Object so=q.opt("sources");if(so instanceof String&&!((String)so).isEmpty())return(String)so;JSONArray a=so instanceof JSONArray?(JSONArray)so:q.optJSONArray("sourceList");StringBuilder b=new StringBuilder();if(a!=null)for(int i=0;i<a.length();i++){Object x=a.opt(i);if(x instanceof JSONObject){JSONObject o=(JSONObject)x;b.append("• ").append(o.optString("label"));String d=o.optString("detail");if(!d.isEmpty())b.append(" — ").append(d);b.append("\n");}else b.append("• ").append(String.valueOf(x)).append("\n");}String src=q.optString("src");if(!src.isEmpty()){if(b.length()>0)b.append("\n");b.append(src);}return b.toString().trim();}
''')

inside('private void renderProfile(boolean push)','for(String m:new String[]{"classic","multi","match","hadith","free","expert","mind_exam"})','for(String m:new String[]{"classic","multi","match","hadith","free","expert","mind_quick","mind_exam"})')

# Навигация новых экранов
meth('private void restore(Screen s)',r'''
    private void restore(Screen s){switch(s.type){case"home":renderHome(false);break;case"mindHub":renderMindHub(false);break;case"intro":renderIntro(false);break;case"mindLesson":renderMindLesson(Integer.parseInt(s.arg),false);break;case"mindSlow":renderMindSlow(Integer.parseInt(s.arg),false);break;case"mindFocus":renderMindFocus(Integer.parseInt(s.arg),false);break;case"mindStages":String[]m=s.arg.split(":");renderMindStages(Integer.parseInt(m[0]),Integer.parseInt(m[1]),false);break;case"quizHub":renderQuizHub(false);break;case"quiz":String[]q=s.arg.split(":");renderNativeQuiz(q[0],Integer.parseInt(q[1]),false);break;case"repeat":renderRepeatHub(false);break;case"profile":renderProfile(false);break;case"settings":renderSettings(false);break;case"menu":renderMenu(false);break;default:renderHome(false);}}
''')

p.write_text(s,encoding='utf-8')
for x in ['courseStep("01"','renderMindSlow(int idx','renderMindFocus(int idx','renderMindStages(int idx','case"mind_quick"','ЧТО МОЖНО ИЗВЛЕЧЬ','themedSpinnerAdapter','shareRow(questionText']:
    if x not in s: raise SystemExit('Проверка не пройдена: '+x)
print('\nГОТОВО: 01–06 возвращены, deep-блоки подписаны, dark/spinner/share/picker исправлены.')
