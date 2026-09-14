package app.alfatiha.tafsir;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MainActivity extends Activity {
    private static final int C_BG=Color.rgb(246,242,232), C_INK=Color.rgb(39,49,45), C_MUTED=Color.rgb(121,129,124),
        C_SAGE=Color.rgb(102,137,121), C_SAGE_SOFT=Color.rgb(236,243,238), C_BLUE=Color.rgb(105,137,166), C_BLUE_SOFT=Color.rgb(238,243,247), C_WHITE=Color.WHITE;
    private LinearLayout page, bottom;
    private ScrollView scroll;
    private SharedPreferences prefs;
    private ArrayDeque<Screen> history=new ArrayDeque<>();
    private Screen current=new Screen("home","");
    private float fontScale=1f;
    private boolean dark=false;
    private HashMap<String,JSONArray> cache=new HashMap<>();
    private String currentSection="home";

    static class Screen { String type,arg; Screen(String t,String a){type=t;arg=a==null?"":a;} }

    @Override public void onCreate(Bundle b){ super.onCreate(b); prefs=getSharedPreferences("alfatiha_native",MODE_PRIVATE); fontScale=prefs.getFloat("fontScale",1f); dark=prefs.getBoolean("dark",false); buildShell(); renderHome(false); }

    private int bg(){return dark?Color.rgb(24,29,27):C_BG;} private int ink(){return dark?Color.rgb(235,238,235):C_INK;} private int muted(){return dark?Color.rgb(170,179,173):C_MUTED;} private int card(){return dark?Color.rgb(38,45,41):C_WHITE;}
    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);} private int sp(float v){return (int)(v*fontScale);}

    private void buildShell(){
        getWindow().setStatusBarColor(bg()); getWindow().setNavigationBarColor(bg());
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(bg());
        scroll=new ScrollView(this); scroll.setFillViewport(true); page=new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(dp(18),dp(14),dp(18),dp(110)); scroll.addView(page,new ScrollView.LayoutParams(-1,-2));
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        bottom=new LinearLayout(this); bottom.setOrientation(LinearLayout.HORIZONTAL); bottom.setPadding(dp(8),dp(8),dp(8),dp(8)); bottom.setGravity(Gravity.CENTER); bottom.setBackground(makeBg(dark?Color.rgb(31,36,33):Color.rgb(250,248,242),0,0));
        root.addView(bottom,new LinearLayout.LayoutParams(-1,dp(70))); setContentView(root); buildBottom();
    }

    private void buildBottom(){ bottom.removeAllViews(); navBtn("⌂\nГлавная",()->renderHome(true)); navBtn("≡\nВ раздел",()->openSection()); navBtn("‹\nНазад",()->goBack()); navBtn("▦\nМеню",()->renderMenu(true)); }
    private void navBtn(String text,Runnable r){ Button b=new Button(this); b.setText(text); b.setTextSize(12); b.setTextColor(muted()); b.setAllCaps(false); b.setBackgroundColor(Color.TRANSPARENT); b.setOnClickListener(v->r.run()); bottom.addView(b,new LinearLayout.LayoutParams(0,-1,1)); }

    private GradientDrawable makeBg(int color,float radius,int stroke){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));if(stroke>0)g.setStroke(dp(1),stroke);return g;}
    private void clear(String type,String arg,boolean push){ if(push && current!=null && !current.type.equals(type)) history.push(current); current=new Screen(type,arg); page.removeAllViews(); page.setBackgroundColor(bg()); scroll.scrollTo(0,0); }
    private TextView text(String s,float size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp(size));t.setTextColor(color);t.setLineSpacing(dp(2),1.08f);t.setTypeface(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL);t.setPadding(0,dp(4),0,dp(4));return t;}
    private void add(TextView t){page.addView(t,new LinearLayout.LayoutParams(-1,-2));}
    private Space gap(int h){Space s=new Space(this);s.setLayoutParams(new LinearLayout.LayoutParams(1,dp(h)));page.addView(s);return s;}
    private LinearLayout card(int color){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(18),dp(16),dp(18),dp(16));c.setBackground(makeBg(color,22,dark?Color.rgb(57,65,61):Color.rgb(219,221,215)));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(7),0,dp(7));page.addView(c,lp);return c;}
    private Button action(String label,int color){Button b=new Button(this);b.setText(label);b.setTextSize(sp(16));b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setGravity(Gravity.CENTER);b.setBackground(makeBg(color,18,0));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(58));lp.setMargins(0,dp(10),0,0);b.setLayoutParams(lp);return b;}
    private Button outline(String label){Button b=new Button(this);b.setText(label);b.setTextSize(sp(15));b.setTextColor(ink());b.setAllCaps(false);b.setBackground(makeBg(dark?Color.rgb(43,50,46):Color.rgb(250,249,245),16,dark?Color.rgb(72,82,77):Color.rgb(208,211,206)));return b;}

    private void header(String title,String sub){ add(text(title,28,ink(),false)); if(sub!=null&&!sub.isEmpty()) add(text(sub,15,muted(),false)); gap(10); }

    private void renderHome(boolean push){ clear("home","",push); currentSection="home"; TextView ar=text("اهدِنَا الصِّرَاطَ المُستَقِيمَ",32,ink(),false); ar.setGravity(Gravity.CENTER); ar.setTypeface(Typeface.SERIF); add(ar); TextView tr=text("«Веди нас прямым путём»",15,muted(),false);tr.setGravity(Gravity.CENTER);add(tr);gap(18);
        LinearLayout m=card(dark?Color.rgb(40,54,47):C_SAGE_SOFT);m.addView(text("ГЛАВНЫЙ РАЗДЕЛ",12,C_SAGE,true));m.addView(text("Осознанное чтение Аль-Фатихи",27,ink(),false));m.addView(text("Понимайте смысл произносимых слов и удерживайте его в сердце во время намаза.",16,muted(),false));Button bm=action("Продолжить",C_SAGE);bm.setOnClickListener(v->renderMindHub(true));m.addView(bm);
        LinearLayout q=card(dark?Color.rgb(40,48,57):C_BLUE_SOFT);q.addView(text("ПРОВЕРКА ЗНАНИЙ",12,C_BLUE,true));q.addView(text("Викторина по Аль-Фатихе",27,ink(),false));q.addView(text("Сложные вопросы по тафсиру: близкие варианты, анализ, сопоставление и экспертные режимы.",16,muted(),false));q.addView(text("Всего заданий: 310+",14,muted(),false));Button bq=action("Открыть викторину",C_BLUE);bq.setOnClickListener(v->renderQuizHub(true));q.addView(bq);
        gap(8); LinearLayout stats=card(card()); stats.addView(text("Ваш прогресс",20,ink(),true));stats.addView(text(progressSummary(),15,muted(),false));Button bp=outline("Открыть профиль");bp.setOnClickListener(v->renderProfile(true));stats.addView(bp);
    }

    private String progressSummary(){int a=prefs.getInt("answered_total",0),c=prefs.getInt("correct_total",0);return "Отвечено: "+a+"   •   Верно: "+c+"   •   Точность: "+(a==0?0:(c*100/a))+"%";}

    private void renderMindHub(boolean push){clear("mindHub","",push);currentSection="mind";header("Осознанное чтение Аль-Фатихи","Практический курс по восьми аятам и смысловым частям с источниками и разбором.");LinearLayout intro=card(dark?Color.rgb(58,53,40):Color.rgb(250,246,225));intro.addView(text("ВАЖНОЕ ПРЕДИСЛОВИЕ",13,Color.rgb(147,118,54),true));intro.addView(text("Почему присутствие сердца меняет чтение Аль-Фатихи",20,ink(),true));Button bi=outline("Открыть предисловие");bi.setOnClickListener(v->renderIntro(true));intro.addView(bi);JSONArray data=arr("mind_data.json");for(int i=0;i<data.length();i++){JSONObject o=data.optJSONObject(i);LinearLayout c=card(card());c.addView(text((i+1)+". "+o.optString("t"),20,ink(),true));c.addView(text(o.optString("ru"),14,muted(),false));final int idx=i;Button b=outline("Открыть урок");b.setOnClickListener(v->renderMindLesson(idx,true));c.addView(b);}LinearLayout exam=card(dark?Color.rgb(40,48,57):C_BLUE_SOFT);exam.addView(text("Экзамен по осознанному чтению",21,ink(),true));exam.addView(text("30 сложных заданий: варианты ответа и самостоятельные ответы.",14,muted(),false));Button be=action("Начать экзамен",C_BLUE);be.setOnClickListener(v->renderNativeQuiz("mind_exam",0,true));exam.addView(be);}

    private void renderIntro(boolean push){clear("intro","",push);currentSection="mind";header("Важное предисловие","");JSONArray a=arr("mind_intro.json");for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);LinearLayout c=card(card());c.addView(text(o.optString("title"),20,ink(),true));c.addView(text(o.optString("text"),16,ink(),false));}Button b=action("Начать урок",C_SAGE);b.setOnClickListener(v->renderMindLesson(0,true));page.addView(b);}

    private void renderMindLesson(int idx,boolean push){clear("mindLesson",String.valueOf(idx),push);currentSection="mind";JSONArray data=arr("mind_data.json"), deep=arr("mind_deep_data.json"), qd=arr("mind_qayyim_deep.json"), scholars=arr("mind_scholars.json");JSONObject o=data.optJSONObject(idx);header((idx+1)+". "+o.optString("t"),o.optString("ru"));LinearLayout words=card(dark?Color.rgb(40,54,47):C_SAGE_SOFT);words.addView(text("Слова и смысл",18,C_SAGE,true));JSONArray wa=o.optJSONArray("words");for(int i=0;i<wa.length();i++){JSONArray w=wa.optJSONArray(i);words.addView(text("• "+w.optString(0)+" — "+w.optString(1),15,ink(),false));}
        sectionCard("Понять смысл",o.optString("meaning"),C_BLUE_SOFT);sectionCard("Что должно быть в сердце",o.optString("heart"),C_SAGE_SOFT);sectionCard("Во время намаза",o.optString("prompt"),Color.rgb(249,245,220));
        if(deep.length()>idx){JSONArray d=deep.optJSONArray(idx); if(d!=null){LinearLayout dc=card(card());dc.addView(text("Раскрыть смысл глубже",19,ink(),true));for(int j=0;j<d.length();j++){Object x=d.opt(j);if(x instanceof JSONArray){JSONArray xa=(JSONArray)x;for(int k=0;k<xa.length();k++)dc.addView(text("• "+xa.optString(k),15,ink(),false));}else dc.addView(text(String.valueOf(x),15,ink(),false));}}
        if(qd.length()>idx){JSONObject qo=qd.optJSONObject(idx);LinearLayout qc=card(dark?Color.rgb(49,46,40):Color.rgb(246,239,224));qc.addView(text("Разбор Ибн аль-Каййима",19,Color.rgb(145,111,50),true));qc.addView(text(qo.optString("words"),15,ink(),false));qc.addView(text(qo.optString("detail"),15,ink(),false));JSONArray bens=qo.optJSONArray("benefits");if(bens!=null)for(int k=0;k<bens.length();k++)qc.addView(text("• "+bens.optString(k),15,ink(),false));qc.addView(text(qo.optString("src"),13,muted(),false));}
        if(scholars.length()>idx){JSONObject so=scholars.optJSONObject(idx);LinearLayout sc=card(card());sc.addView(text("Дополнительный разбор учёных",19,ink(),true));Iterator<String> it=so.keys();while(it.hasNext()){String key=it.next();JSONObject v=so.optJSONObject(key);if(v!=null){sc.addView(text(v.optString("title"),16,ink(),true));sc.addView(text(v.optString("text"),14,muted(),false));JSONArray vb=v.optJSONArray("benefits");if(vb!=null)for(int k=0;k<vb.length();k++)sc.addView(text("• "+vb.optString(k),14,ink(),false));String vs=v.optString("src");if(!vs.isEmpty())sc.addView(text(vs,12,muted(),false));}}}
        sectionCard("Источник",o.optString("src"),card());
        LinearLayout nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);if(idx>0){Button prev=outline("← Предыдущий");prev.setOnClickListener(v->renderMindLesson(idx-1,true));nav.addView(prev,new LinearLayout.LayoutParams(0,dp(54),1));}if(idx<data.length()-1){Button next=outline("Следующий →");next.setOnClickListener(v->renderMindLesson(idx+1,true));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(54),1);lp.setMargins(dp(8),0,0,0);nav.addView(next,lp);}page.addView(nav);
    }
    private void sectionCard(String title,String body,int color){LinearLayout c=card(dark?card():color);c.addView(text(title,18,ink(),true));c.addView(text(body,15,ink(),false));}

    private void renderQuizHub(boolean push){clear("quizHub","",push);currentSection="quiz";header("Викторина по Аль-Фатихе","Нативные режимы. Все вопросы загружаются из версии 19.04, без WebView.");modeCard("Классическая викторина","200 вопросов · 4 близких варианта","classic",C_BLUE);modeCard("Два правильных","20 заданий · выбрать два варианта","multi",C_SAGE);modeCard("Сопоставление","20 заданий · термин ↔ смысл","match",Color.rgb(127,111,154));modeCard("Хадис → смысл","30 заданий","hadith",Color.rgb(153,116,85));modeCard("Без вариантов","20 заданий · самостоятельный ответ","free",Color.rgb(111,137,125));modeCard("Эксперт","20 сложных вопросов","expert",Color.rgb(85,105,126));}
    private void modeCard(String title,String sub,String mode,int color){LinearLayout c=card(card());c.addView(text(title,20,ink(),true));c.addView(text(sub,14,muted(),false));int saved=prefs.getInt("idx_"+mode,0);c.addView(text("Продолжить с задания "+(saved+1),13,muted(),false));Button b=action("Открыть",color);b.setOnClickListener(v->renderNativeQuiz(mode,saved,true));c.addView(b);}

    private JSONArray quizArray(String mode){switch(mode){case"classic":return arr("quiz_data.json");case"multi":return arr("multi_data.json");case"match":return arr("match_data.json");case"hadith":return arr("hadith_data.json");case"free":return arr("free_data.json");case"expert":return arr("expert_data.json");case"mind_exam":return arr("mind_exam.json");default:return new JSONArray();}}

    private void renderNativeQuiz(String mode,int idx,boolean push){JSONArray a=quizArray(mode);if(a.length()==0){toast("Нет данных");return;}if(idx<0)idx=0;if(idx>=a.length())idx=0;clear("quiz",mode+":"+idx,push);currentSection=mode.equals("mind_exam")?"mind":"quiz";prefs.edit().putInt("idx_"+mode,idx).apply();JSONObject q=a.optJSONObject(idx);header(modeTitle(mode),"Задание "+(idx+1)+" из "+a.length());if(mode.equals("match")){renderMatch(q,mode,idx,a.length());return;}if(mode.equals("free") || (mode.equals("mind_exam")&&"self".equals(q.optString("type")))){renderFree(q,mode,idx,a.length());return;}if(mode.equals("multi")){renderMulti(q,mode,idx,a.length());return;}renderMcq(q,mode,idx,a.length());}
    private String modeTitle(String m){switch(m){case"classic":return"Классическая викторина";case"multi":return"Два правильных";case"match":return"Сопоставление";case"hadith":return"Хадис → смысл";case"free":return"Без вариантов";case"expert":return"Эксперт";case"mind_exam":return"Экзамен по осознанному чтению";}return"Викторина";}
    private String qText(JSONObject q,String mode){if(mode.equals("hadith"))return q.optString("hadith")+"\n\n"+q.optString("question");if(mode.equals("mind_exam"))return q.optString("q");return q.optString("question");}
    private JSONArray optionsAsArray(JSONObject q,String mode){Object o=q.opt("options");if(o instanceof JSONArray)return(JSONArray)o;if(o instanceof JSONObject){JSONObject jo=(JSONObject)o;JSONArray a=new JSONArray();for(String k:new String[]{"A","B","C","D","E","F"})if(jo.has(k)){Object v=jo.opt(k);if(v instanceof JSONObject)a.put(((JSONObject)v).optString("text"));else a.put(String.valueOf(v));}return a;}o=q.opt("opts");return o instanceof JSONArray?(JSONArray)o:new JSONArray();}
    private int correctIndex(JSONObject q,String mode){Object c=q.opt("correct");if(c instanceof Number)return((Number)c).intValue();if(c instanceof String){String s=(String)c;if(s.length()==1&&Character.isLetter(s.charAt(0)))return Character.toUpperCase(s.charAt(0))-'A';try{return Integer.parseInt(s);}catch(Exception e){return-1;}}if(mode.equals("mind_exam"))return q.optInt("a",-1);return-1;}
    private String explanation(JSONObject q,String mode,int selected){if(mode.equals("classic")){JSONObject opts=q.optJSONObject("options");String key=String.valueOf((char)('A'+selected));return opts!=null&&opts.optJSONObject(key)!=null?opts.optJSONObject(key).optString("explanation"):q.optString("note");}JSONArray ex=q.optJSONArray("explanations");if(ex!=null&&selected>=0&&selected<ex.length())return ex.optString(selected);return q.optString("why",q.optString("note"));}
    private String sources(JSONObject q){
        Object so=q.opt("sources"); if(so instanceof String && !((String)so).isEmpty()) return (String)so;
        JSONArray a=so instanceof JSONArray?(JSONArray)so:q.optJSONArray("sourceList");
        if(a==null)return ""; StringBuilder b=new StringBuilder();
        for(int i=0;i<a.length();i++){Object x=a.opt(i); if(x instanceof JSONObject){JSONObject o=(JSONObject)x; b.append("• ").append(o.optString("label")); String d=o.optString("detail"); if(!d.isEmpty())b.append(" — ").append(d); b.append("\n");} else b.append("• ").append(String.valueOf(x)).append("\n");}
        return b.toString().trim();
    }

    private void styleChoice(CompoundButton b, boolean checked){
        int fill=checked?(dark?Color.rgb(48,72,61):Color.rgb(220,233,226)):(dark?Color.rgb(43,50,46):Color.rgb(250,249,245));
        int stroke=checked?(dark?Color.rgb(134,168,148):C_SAGE):(dark?Color.rgb(72,82,77):Color.rgb(207,214,209));
        b.setBackground(makeBg(fill,18,stroke));
        b.setTextColor(checked?(dark?Color.rgb(244,248,245):Color.rgb(41,56,49)):ink());
        b.setTypeface(Typeface.DEFAULT,checked?Typeface.BOLD:Typeface.NORMAL);
        int[][] states=new int[][]{new int[]{android.R.attr.state_checked},new int[]{}};
        int[] colors=new int[]{dark?Color.rgb(134,168,148):C_SAGE,muted()};
        b.setButtonTintList(new ColorStateList(states,colors));
        b.setCompoundDrawablePadding(dp(8));
        b.setPadding(dp(14),dp(14),dp(14),dp(14));
    }

    private void renderMcq(JSONObject q,String mode,int idx,int total){
        LinearLayout qc=card(card());qc.addView(text(qText(q,mode),20,ink(),true));
        JSONArray opts=optionsAsArray(q,mode); RadioGroup group=new RadioGroup(this); group.setOrientation(RadioGroup.VERTICAL);
        ArrayList<RadioButton> radios=new ArrayList<>();
        for(int i=0;i<opts.length();i++){
            RadioButton r=new RadioButton(this); r.setId(1000+i); r.setText(((char)('A'+i))+". "+opts.optString(i)); r.setTextSize(sp(16));
            styleChoice(r,false); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,dp(5),0,dp(5)); group.addView(r,lp); radios.add(r);
        }
        group.setOnCheckedChangeListener((g,id)->{ for(RadioButton r:radios) styleChoice(r,r.getId()==id); });
        qc.addView(group); Button check=action("Проверить",C_SAGE);qc.addView(check); LinearLayout result=card(card());result.setVisibility(View.GONE);
        check.setOnClickListener(v->{int id=group.getCheckedRadioButtonId();if(id<1000){toast("Выберите ответ");return;}int sel=id-1000,correct=correctIndex(q,mode);boolean ok=sel==correct;record(mode,ok);TextView mark=text(ok?"✓ Верно":"✕ Неверно. Правильный ответ: "+((char)('A'+correct)),18,ok?C_SAGE:Color.rgb(174,77,67),true);result.addView(mark);String ex=explanation(q,mode,sel);if(!ex.isEmpty())result.addView(text(ex,15,ink(),false));String src=sources(q);if(!src.isEmpty())result.addView(text("Источник: "+src,13,muted(),false));Button next=action(idx+1<total?"Следующий вопрос":"Завершить",C_BLUE);next.setOnClickListener(x->{if(idx+1<total)renderNativeQuiz(mode,idx+1,true);else renderQuizHub(true);});result.addView(next);result.setVisibility(View.VISIBLE);check.setEnabled(false);});bookmarkButton(q,mode,qc);
    }

    private void renderMulti(JSONObject q,String mode,int idx,int total){
        LinearLayout qc=card(card());qc.addView(text(qText(q,mode),20,ink(),true));qc.addView(text("Выберите все верные варианты. Выбранный ответ выделяется цветом и галочкой.",14,muted(),false));
        JSONArray opts=optionsAsArray(q,mode);ArrayList<CheckBox> boxes=new ArrayList<>();
        for(int i=0;i<opts.length();i++){
            CheckBox cb=new CheckBox(this); cb.setText(((char)('A'+i))+". "+opts.optString(i)); cb.setTextSize(sp(16)); styleChoice(cb,false);
            cb.setOnCheckedChangeListener((button,checked)->styleChoice(button,checked));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(5),0,dp(5));qc.addView(cb,lp);boxes.add(cb);
        }
        Button check=action("Проверить выбранные ответы",C_SAGE);qc.addView(check);
        check.setOnClickListener(v->{JSONArray cor=q.optJSONArray("correct");HashSet<Integer> cs=new HashSet<>();if(cor!=null)for(int i=0;i<cor.length();i++){Object x=cor.opt(i);if(x instanceof Number)cs.add(((Number)x).intValue());else{String z=String.valueOf(x);cs.add(z.length()==1&&Character.isLetter(z.charAt(0))?Character.toUpperCase(z.charAt(0))-'A':Integer.parseInt(z));}}HashSet<Integer> us=new HashSet<>();for(int i=0;i<boxes.size();i++)if(boxes.get(i).isChecked())us.add(i);if(us.size()!=cs.size()){toast("Нужно выбрать "+cs.size()+" варианта");return;}boolean ok=us.equals(cs);record(mode,ok);LinearLayout r=card(card());r.addView(text(ok?"✓ Верно":"✕ Неверно. Правильные: "+letters(cs),18,ok?C_SAGE:Color.rgb(174,77,67),true));String n=q.optString("note");if(!n.isEmpty())r.addView(text(n,15,ink(),false));String src=sources(q);if(!src.isEmpty())r.addView(text("Источник: "+src,13,muted(),false));Button next=action(idx+1<total?"Следующее":"Завершить",C_BLUE);next.setOnClickListener(x->{if(idx+1<total)renderNativeQuiz(mode,idx+1,true);else renderQuizHub(true);});r.addView(next);check.setEnabled(false);});bookmarkButton(q,mode,qc);
    }
    private String letters(Set<Integer>s){ArrayList<Integer>a=new ArrayList<>(s);Collections.sort(a);StringBuilder b=new StringBuilder();for(int x:a){if(b.length()>0)b.append(", ");b.append((char)('A'+x));}return b.toString();}

    private void renderMatch(JSONObject q,String mode,int idx,int total){LinearLayout qc=card(card());qc.addView(text(q.optString("title"),20,ink(),true));JSONArray left=q.optJSONArray("left"),right=q.optJSONArray("right"),ans=q.optJSONArray("answer");ArrayList<Spinner> spins=new ArrayList<>();ArrayList<String> values=new ArrayList<>();values.add("— Выберите —");for(int j=0;j<right.length();j++)values.add((j+1)+". "+right.optString(j));for(int i=0;i<left.length();i++){qc.addView(text((i+1)+". "+left.optString(i),15,ink(),true));Spinner sp=new Spinner(this);ArrayAdapter<String> ad=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,values);sp.setAdapter(ad);qc.addView(sp);spins.add(sp);}Button check=action("Проверить соответствие",C_SAGE);qc.addView(check);check.setOnClickListener(v->{boolean ok=true;for(int i=0;i<spins.size();i++){int sel=spins.get(i).getSelectedItemPosition()-1;if(sel<0){toast("Заполните все соответствия");return;}int correct=ans.optInt(i,-1);if(sel!=correct && sel+1!=correct)ok=false;}record(mode,ok);LinearLayout r=card(card());r.addView(text(ok?"✓ Всё верно":"✕ Есть неточности",18,ok?C_SAGE:Color.rgb(174,77,67),true));JSONArray ex=q.optJSONArray("explanations");if(ex!=null)for(int i=0;i<ex.length();i++)r.addView(text("• "+ex.optString(i),14,ink(),false));String src=sources(q);if(!src.isEmpty())r.addView(text("Источник: "+src,13,muted(),false));Button next=action(idx+1<total?"Следующее":"Завершить",C_BLUE);next.setOnClickListener(x->{if(idx+1<total)renderNativeQuiz(mode,idx+1,true);else renderQuizHub(true);});r.addView(next);check.setEnabled(false);});}

    private void renderFree(JSONObject q,String mode,int idx,int total){LinearLayout qc=card(card());String question=mode.equals("mind_exam")?q.optString("q"):q.optString("question");qc.addView(text(question,20,ink(),true));EditText ed=new EditText(this);ed.setHint("Введите ответ своими словами");ed.setTextColor(ink());ed.setHintTextColor(muted());ed.setTextSize(sp(16));ed.setMinLines(4);ed.setGravity(Gravity.TOP);ed.setBackground(makeBg(dark?Color.rgb(43,50,46):Color.rgb(250,249,245),14,dark?Color.rgb(72,82,77):Color.rgb(210,212,208)));qc.addView(ed,new LinearLayout.LayoutParams(-1,dp(150)));Button show=action("Показать эталон и проверить себя",C_SAGE);qc.addView(show);show.setOnClickListener(v->{hideKeyboard(ed);String model=q.optString("model");String key=q.optString("key");LinearLayout r=card(card());r.addView(text("Эталон ответа",18,ink(),true));r.addView(text(model,15,ink(),false));if(!key.isEmpty())r.addView(text("Ключ: "+key,14,muted(),false));r.addView(text("Оцените себя:",15,ink(),true));LinearLayout row=new LinearLayout(this);Button knew=outline("Знал");Button no=outline("Нужно повторить");row.addView(knew,new LinearLayout.LayoutParams(0,dp(54),1));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(54),1);lp.setMargins(dp(8),0,0,0);row.addView(no,lp);r.addView(row);knew.setOnClickListener(x->{record(mode,true);nextFree(mode,idx,total);});no.setOnClickListener(x->{record(mode,false);nextFree(mode,idx,total);});show.setEnabled(false);});}
    private void nextFree(String mode,int idx,int total){if(idx+1<total)renderNativeQuiz(mode,idx+1,true);else if(mode.equals("mind_exam"))renderMindHub(true);else renderQuizHub(true);}
    private void hideKeyboard(View v){((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(),0);}

    private void bookmarkButton(JSONObject q,String mode,LinearLayout parent){Button b=outline(isBookmarked(mode,q)?"★ В закладках":"☆ В закладки");b.setOnClickListener(v->{toggleBookmark(mode,q);b.setText(isBookmarked(mode,q)?"★ В закладках":"☆ В закладки");});parent.addView(b);}
    private String qid(String mode,JSONObject q){String id=q.optString("num");if(id.isEmpty())id=q.optString("id");return mode+":"+id;}
    private boolean isBookmarked(String m,JSONObject q){return prefs.getStringSet("bookmarks",new HashSet<>()).contains(qid(m,q));}
    private void toggleBookmark(String m,JSONObject q){HashSet<String>s=new HashSet<>(prefs.getStringSet("bookmarks",new HashSet<>()));String id=qid(m,q);if(!s.add(id))s.remove(id);prefs.edit().putStringSet("bookmarks",s).apply();}
    private void record(String mode,boolean ok){prefs.edit().putInt("answered_total",prefs.getInt("answered_total",0)+1).putInt("correct_total",prefs.getInt("correct_total",0)+(ok?1:0)).putInt("answered_"+mode,prefs.getInt("answered_"+mode,0)+1).putInt("correct_"+mode,prefs.getInt("correct_"+mode,0)+(ok?1:0)).apply();}

    private void renderProfile(boolean push){clear("profile","",push);currentSection="profile";header("Профиль знаний","Прогресс хранится локально на устройстве.");LinearLayout c=card(card());c.addView(text(progressSummary(),18,ink(),true));c.addView(text("Закладки: "+prefs.getStringSet("bookmarks",new HashSet<>()).size(),15,muted(),false));for(String m:new String[]{"classic","multi","match","hadith","free","expert","mind_exam"}){int a=prefs.getInt("answered_"+m,0),k=prefs.getInt("correct_"+m,0);c.addView(text(modeTitle(m)+": "+a+" ответов · "+(a==0?0:k*100/a)+"%",14,ink(),false));}Button s=outline("Настройки");s.setOnClickListener(v->renderSettings(true));c.addView(s);Button reset=outline("Сбросить прогресс");reset.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Сбросить прогресс?").setMessage("Закладки и статистика будут удалены.").setNegativeButton("Отмена",null).setPositiveButton("Сбросить",(d,w)->{prefs.edit().clear().apply();fontScale=1f;dark=false;buildShell();renderHome(false);}).show());c.addView(reset);}

    private void renderSettings(boolean push){clear("settings","",push);currentSection="settings";header("Настройки","Изменения применяются сразу.");LinearLayout f=card(card());f.addView(text("Размер текста",18,ink(),true));LinearLayout row=new LinearLayout(this);for(float z:new float[]{0.9f,1f,1.15f}){Button b=outline(z==0.9f?"Компактный":z==1f?"Обычный":"Крупный");b.setOnClickListener(v->{fontScale=z;prefs.edit().putFloat("fontScale",z).apply();renderSettings(false);});row.addView(b,new LinearLayout.LayoutParams(0,dp(55),1));}f.addView(row);LinearLayout t=card(card());t.addView(text("Оформление",18,ink(),true));Button theme=action(dark?"Светлая тема":"Тёмная тема",dark?C_BLUE:C_SAGE);theme.setOnClickListener(v->{dark=!dark;prefs.edit().putBoolean("dark",dark).apply();buildShell();renderSettings(false);});t.addView(theme);}

    private void renderMenu(boolean push){clear("menu","",push);currentSection="menu";header("Меню","");menuBtn("Учиться",()->renderMindHub(true));menuBtn("Викторина",()->renderQuizHub(true));menuBtn("Профиль",()->renderProfile(true));menuBtn("Настройки",()->renderSettings(true));}
    private void menuBtn(String s,Runnable r){Button b=action(s,C_SAGE);b.setOnClickListener(v->r.run());page.addView(b);}
    private void openSection(){if(currentSection.equals("mind"))renderMindHub(true);else if(currentSection.equals("quiz"))renderQuizHub(true);else if(currentSection.equals("profile"))renderProfile(true);else renderHome(true);}
    private void goBack(){if(!history.isEmpty()){Screen s=history.pop();restore(s);}else renderHome(false);}
    private void restore(Screen s){switch(s.type){case"home":renderHome(false);break;case"mindHub":renderMindHub(false);break;case"intro":renderIntro(false);break;case"mindLesson":renderMindLesson(Integer.parseInt(s.arg),false);break;case"quizHub":renderQuizHub(false);break;case"quiz":String[]p=s.arg.split(":");renderNativeQuiz(p[0],Integer.parseInt(p[1]),false);break;case"profile":renderProfile(false);break;case"settings":renderSettings(false);break;case"menu":renderMenu(false);break;default:renderHome(false);}}
    @Override public void onBackPressed(){goBack();}

    private JSONArray arr(String name){if(cache.containsKey(name))return cache.get(name);try{InputStream in=getAssets().open(name);ByteArrayOutputStream out=new ByteArrayOutputStream();byte[]buf=new byte[8192];int n;while((n=in.read(buf))>0)out.write(buf,0,n);JSONArray a=new JSONArray(out.toString("UTF-8"));cache.put(name,a);return a;}catch(Exception e){e.printStackTrace();return new JSONArray();}}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
