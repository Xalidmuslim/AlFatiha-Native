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
import java.util.*;

public class MainActivity extends Activity {
    // Palette based on the final 19.05 UI: warm paper, sage, muted blue, low-contrast borders.
    private static final int C_BG = Color.rgb(245,241,232);
    private static final int C_BG_2 = Color.rgb(238,232,219);
    private static final int C_PANEL = Color.rgb(255,254,250);
    private static final int C_INK = Color.rgb(29,36,33);
    private static final int C_MUTED = Color.rgb(109,116,111);
    private static final int C_LINE = Color.rgb(222,221,214);
    private static final int C_SAGE = Color.rgb(53,109,87);
    private static final int C_SAGE_2 = Color.rgb(38,79,64);
    private static final int C_SAGE_SOFT = Color.rgb(228,238,233);
    private static final int C_BLUE = Color.rgb(103,135,162);
    private static final int C_BLUE_SOFT = Color.rgb(234,241,246);
    private static final int C_SAND_SOFT = Color.rgb(247,241,224);
    private static final int C_LAV_SOFT = Color.rgb(241,237,246);
    private static final int C_GOOD = Color.rgb(47,117,87);
    private static final int C_GOOD_BG = Color.rgb(228,242,235);
    private static final int C_BAD = Color.rgb(157,78,72);
    private static final int C_BAD_BG = Color.rgb(247,232,230);

    private LinearLayout page, bottom;
    private ScrollView scroll;
    private SharedPreferences prefs;
    private final ArrayDeque<Screen> history = new ArrayDeque<>();
    private Screen current = new Screen("home", "");
    private float fontScale = 1f;
    private boolean dark = false;
    private String fontMode = "modern";
    private final HashMap<String, JSONArray> cache = new HashMap<>();
    private String currentSection = "home";

    static class Screen {
        String type, arg;
        Screen(String t, String a) { type=t; arg=a==null?"":a; }
    }

    static class ChoiceView {
        LinearLayout root;
        TextView marker;
        TextView label;
        int index;
        boolean selected;
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs=getSharedPreferences("alfatiha_native",MODE_PRIVATE);
        fontScale=prefs.getFloat("fontScale",1f);
        dark=prefs.getBoolean("dark",false);
        fontMode=prefs.getString("fontMode","modern");
        buildShell();
        renderHome(false);
    }

    private int bg(){return dark?Color.rgb(23,28,26):C_BG;}
    private int bg2(){return dark?Color.rgb(29,34,31):C_BG_2;}
    private int ink(){return dark?Color.rgb(236,239,236):C_INK;}
    private int muted(){return dark?Color.rgb(170,179,173):C_MUTED;}
    private int panel(){return dark?Color.rgb(37,44,40):C_PANEL;}
    private int line(){return dark?Color.rgb(61,70,65):C_LINE;}
    private int sageSoft(){return dark?Color.rgb(39,54,47):C_SAGE_SOFT;}
    private int blueSoft(){return dark?Color.rgb(39,48,57):C_BLUE_SOFT;}
    private int sandSoft(){return dark?Color.rgb(53,49,39):C_SAND_SOFT;}
    private int lavSoft(){return dark?Color.rgb(49,44,55):C_LAV_SOFT;}
    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private float sz(float v){return v*fontScale;}

    private Typeface tf(boolean bold){
        String family="sans-serif";
        if("classic".equals(fontMode)) family="serif";
        else if("compact".equals(fontMode)) family="sans-serif-condensed";
        return Typeface.create(family,bold?Typeface.BOLD:Typeface.NORMAL);
    }

    private void applyWindowChrome(){
        getWindow().setStatusBarColor(bg());
        getWindow().setNavigationBarColor(bg());
        int flags=0;
        if(!dark && Build.VERSION.SDK_INT>=23) flags|=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if(!dark && Build.VERSION.SDK_INT>=26) flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    private void buildShell(){
        applyWindowChrome();
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg());

        if(Build.VERSION.SDK_INT>=20){
            root.setOnApplyWindowInsetsListener((v,insets)->{
                v.setPadding(
                    0,
                    insets.getSystemWindowInsetTop(),
                    0,
                    insets.getSystemWindowInsetBottom()
                );
                return insets;
            });
        }

        scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setBackgroundColor(bg());
        page=new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(18),dp(10),dp(18),dp(126));
        scroll.addView(page,new ScrollView.LayoutParams(-1,-2));
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        bottom=new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setGravity(Gravity.CENTER);
        bottom.setPadding(dp(8),dp(7),dp(8),dp(7));
        bottom.setBackground(surfaceBg(dark?Color.rgb(31,37,34):Color.rgb(250,248,242),dark?Color.rgb(28,33,31):Color.rgb(246,243,235),28,line()));
        bottom.setElevation(dp(10));
        LinearLayout.LayoutParams blp=new LinearLayout.LayoutParams(-1,dp(76));
        blp.setMargins(dp(18),0,dp(18),dp(12));
        root.addView(bottom,blp);
        setContentView(root);
        if(Build.VERSION.SDK_INT>=20){
            root.post(()->root.requestApplyInsets());
        }
        buildBottom();
    }

    private void buildBottom(){
        bottom.removeAllViews();
        navBtn("⌂","Главная",()->renderHome(true));
        navBtn("≡","В раздел",this::openSection);
        navBtn("‹","Назад",this::goBack);
        navBtn("▦","Меню",()->renderMenu(true));
    }

    private void navBtn(String icon,String label,Runnable r){
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(4),dp(2),dp(4),dp(2));
        TextView i=text(icon,20,muted(),false); i.setGravity(Gravity.CENTER); i.setPadding(0,0,0,0);
        TextView l=text(label,11.5f,muted(),false); l.setGravity(Gravity.CENTER); l.setPadding(0,0,0,0);
        box.addView(i); box.addView(l);
        box.setOnClickListener(v->r.run());
        bottom.addView(box,new LinearLayout.LayoutParams(0,-1,1));
    }

    private GradientDrawable solidBg(int color,float radius,int stroke){
        GradientDrawable g=new GradientDrawable();
        g.setColor(color); g.setCornerRadius(dp(radius));
        if(stroke!=0)g.setStroke(dp(1),stroke);
        return g;
    }

    private GradientDrawable surfaceBg(int start,int end,float radius,int stroke){
        GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{start,end});
        g.setCornerRadius(dp(radius));
        if(stroke!=0)g.setStroke(dp(1),stroke);
        return g;
    }

    private TextView text(String s,float size,int color,boolean bold){
        TextView t=new TextView(this);
        t.setText(s==null?"":s);
        t.setTextSize(sz(size));
        t.setTextColor(color);
        t.setLineSpacing(dp(2),1.09f);
        t.setTypeface(tf(bold));
        t.setIncludeFontPadding(false);
        t.setPadding(0,dp(4),0,dp(4));
        return t;
    }

    private void add(TextView t){page.addView(t,new LinearLayout.LayoutParams(-1,-2));}
    private Space gap(int h){Space s=new Space(this);page.addView(s,new LinearLayout.LayoutParams(1,dp(h)));return s;}

    private LinearLayout newSurface(int color,float radius,int padding,float elevation){
        LinearLayout c=new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(padding),dp(padding),dp(padding),dp(padding));
        int end=dark?color:blend(color,Color.WHITE,0.22f);
        c.setBackground(surfaceBg(color,end,radius,line()));
        c.setElevation(dp(elevation));
        return c;
    }

    private LinearLayout card(int color){
        LinearLayout c=newSurface(color,24,18,4);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.setMargins(0,dp(7),0,dp(7));
        page.addView(c,lp);
        return c;
    }

    private int blend(int a,int b,float f){
        int r=(int)(Color.red(a)*(1-f)+Color.red(b)*f);
        int g=(int)(Color.green(a)*(1-f)+Color.green(b)*f);
        int bl=(int)(Color.blue(a)*(1-f)+Color.blue(b)*f);
        return Color.rgb(r,g,bl);
    }

    private Button action(String label,int color){
        Button b=new Button(this);
        b.setText(label); b.setTextSize(sz(16)); b.setTextColor(Color.WHITE); b.setAllCaps(false);
        b.setTypeface(tf(false)); b.setGravity(Gravity.CENTER);
        b.setPadding(dp(14),0,dp(14),0);
        b.setBackground(surfaceBg(color,blend(color,Color.BLACK,.08f),18,0));
        b.setElevation(dp(3));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(56));
        lp.setMargins(0,dp(10),0,0); b.setLayoutParams(lp);
        return b;
    }

    private Button outline(String label){
        Button b=new Button(this);
        b.setText(label); b.setTextSize(sz(15)); b.setTextColor(ink()); b.setAllCaps(false);
        b.setTypeface(tf(false)); b.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
        b.setPadding(dp(12),0,dp(12),0);
        b.setBackground(surfaceBg(dark?Color.rgb(43,50,46):Color.rgb(251,249,244),dark?Color.rgb(39,46,42):Color.rgb(247,244,237),16,line()));
        b.setElevation(dp(1));
        return b;
    }

    private Button miniButton(String label){
        Button b=outline(label);
        b.setTextSize(sz("Aa".equals(label)?16:21));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(50),dp(50));
        lp.setMargins(dp(8),0,0,0); b.setLayoutParams(lp);
        return b;
    }


    private Button tinyAction(String label){
        Button b=outline(label);
        b.setTextSize(sz(11.5f));
        b.setAllCaps(false);
        b.setSingleLine(true);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        return b;
    }

    private void copyContent(String content){
        ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        if(cm!=null){
            cm.setPrimaryClip(ClipData.newPlainText("Аль-Фатиха",content));
            toast("Скопировано");
        }
    }

    private void shareContent(String content){
        try{
            Intent i=new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT,content);
            startActivity(Intent.createChooser(i,"Поделиться"));
        }catch(Exception e){
            toast("Не удалось открыть меню «Поделиться»");
        }
    }

    private boolean isMaterialSaved(String id){
        return prefs.getStringSet("material_bookmarks",new HashSet<>()).contains(id);
    }

    private int materialSavedCount(){
        return prefs.getStringSet("material_bookmarks",new HashSet<>()).size();
    }

    private void toggleMaterialSaved(String id){
        HashSet<String> set=new HashSet<>(
                prefs.getStringSet("material_bookmarks",new HashSet<>()));
        if(!set.add(id))set.remove(id);
        prefs.edit().putStringSet("material_bookmarks",set).apply();
    }

    private void addActionCell(LinearLayout row,Button b,boolean first){
        LinearLayout.LayoutParams lp=
                new LinearLayout.LayoutParams(0,dp(46),1);
        if(!first)lp.setMargins(dp(6),0,0,0);
        row.addView(b,lp);
    }

    private LinearLayout contentActions(String saveId,String content,boolean allowSave){
        LinearLayout row=new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0,dp(9),0,dp(3));

        boolean first=true;

        if(allowSave){
            Button save=tinyAction(
                    isMaterialSaved(saveId)?"★ Сохранено":"☆ Сохранить");
            save.setOnClickListener(v->{
                toggleMaterialSaved(saveId);
                save.setText(
                        isMaterialSaved(saveId)?"★ Сохранено":"☆ Сохранить");
            });
            addActionCell(row,save,true);
            first=false;
        }

        Button copy=tinyAction("⧉ Копировать");
        copy.setOnClickListener(v->copyContent(content));
        addActionCell(row,copy,first);

        Button share=tinyAction("↗ Поделиться");
        share.setOnClickListener(v->shareContent(content));
        addActionCell(row,share,false);

        return row;
    }

    private void markDone(Button b,String label){
        b.setText(label);
        b.setTextColor(muted());
        b.setBackground(surfaceBg(
                dark?Color.rgb(43,49,46):Color.rgb(241,240,235),
                dark?Color.rgb(39,45,42):Color.rgb(247,245,240),
                18,line()));
        b.setAlpha(.68f);
        b.setEnabled(false);
    }

    private String introShareText(){
        JSONArray a=arr("mind_intro.json");
        StringBuilder b=new StringBuilder("Важное предисловие\n\n");
        for(int i=0;i<a.length();i++){
            JSONObject o=a.optJSONObject(i);
            if(o==null)continue;
            String title=o.optString("title");
            String body=o.optString("text");
            if(!title.isEmpty())b.append(title).append("\n");
            if(!body.isEmpty())b.append(body).append("\n\n");
        }
        return b.toString().trim();
    }

    private String lessonShareText(JSONObject o){
        StringBuilder b=new StringBuilder();

        b.append(o.optString("t"));
        String ru=o.optString("ru");
        if(!ru.isEmpty())b.append("\n").append(ru);

        JSONArray wa=o.optJSONArray("words");
        if(wa!=null && wa.length()>0){
            b.append("\n\nСлова и смысл:");
            for(int i=0;i<wa.length();i++){
                JSONArray w=wa.optJSONArray(i);
                if(w==null)continue;
                b.append("\n• ")
                 .append(w.optString(0))
                 .append(" — ")
                 .append(w.optString(1));
            }
        }

        String meaning=o.optString("meaning");
        if(!meaning.isEmpty())
            b.append("\n\nПонять смысл:\n").append(meaning);

        String heart=o.optString("heart");
        if(!heart.isEmpty())
            b.append("\n\nЧто должно быть в сердце:\n").append(heart);

        String prompt=o.optString("prompt");
        if(!prompt.isEmpty())
            b.append("\n\nВо время намаза:\n").append(prompt);

        String src=o.optString("src");
        if(!src.isEmpty())
            b.append("\n\nИсточник: ").append(src);

        return b.toString().trim();
    }

    private String deepShareText(String title,JSONArray d){
        StringBuilder b=new StringBuilder(title).append("\n\n");
        for(int j=0;j<d.length();j++){
            Object x=d.opt(j);
            if(x instanceof JSONArray){
                JSONArray a=(JSONArray)x;
                for(int k=0;k<a.length();k++)
                    b.append("• ").append(a.optString(k)).append("\n");
            }else if(x!=null){
                b.append(String.valueOf(x)).append("\n\n");
            }
        }
        return b.toString().trim();
    }

    private String qayyimShareText(JSONObject o){
        StringBuilder b=new StringBuilder("Разбор Ибн аль-Каййима\n\n");

        String words=o.optString("words");
        if(!words.isEmpty())
            b.append("Слова и смысл разбора:\n").append(words).append("\n\n");

        String detail=o.optString("detail");
        if(!detail.isEmpty())
            b.append("Подробнее:\n").append(detail).append("\n\n");

        JSONArray benefits=o.optJSONArray("benefits");
        if(benefits!=null && benefits.length()>0){
            b.append("Пользы для осознанного чтения:\n");
            for(int i=0;i<benefits.length();i++)
                b.append("• ").append(benefits.optString(i)).append("\n");
        }

        String src=o.optString("src");
        if(!src.isEmpty())
            b.append("\nИсточник: ").append(src);

        return b.toString().trim();
    }

    private String quizQuestionShareText(JSONObject q,String mode){
        StringBuilder b=new StringBuilder();

        if("match".equals(mode)){
            b.append(q.optString("title"));
            JSONArray left=q.optJSONArray("left");
            JSONArray right=q.optJSONArray("right");

            if(left!=null){
                b.append("\n\nТермины:");
                for(int i=0;i<left.length();i++)
                    b.append("\n").append(i+1).append(". ").append(left.optString(i));
            }

            if(right!=null){
                b.append("\n\nВарианты:");
                for(int i=0;i<right.length();i++)
                    b.append("\n").append(i+1).append(". ").append(right.optString(i));
            }

            return b.toString().trim();
        }

        b.append(qText(q,mode));

        JSONArray opts=optionsAsArray(q,mode);
        if(opts.length()>0){
            b.append("\n\nВарианты:");
            for(int i=0;i<opts.length();i++)
                b.append("\n").append(i+1).append(". ").append(opts.optString(i));
        }

        return b.toString().trim();
    }

    private String quizResultShareText(JSONObject q,String mode,int correct){
        StringBuilder b=new StringBuilder(quizQuestionShareText(q,mode));
        JSONArray opts=optionsAsArray(q,mode);

        if(correct>=0 && correct<opts.length()){
            b.append("\n\nПравильный ответ: ")
             .append(correct+1)
             .append(". ")
             .append(opts.optString(correct));
        }

        String ex=explanation(q,mode,correct);
        if(!ex.isEmpty())
            b.append("\n\nРазбор:\n").append(ex);

        String src=sources(q);
        if(!src.isEmpty())
            b.append("\n\nИсточник: ").append(src);

        return b.toString().trim();
    }

    private String quizMultiResultShareText(
            JSONObject q,String mode,Set<Integer> correct){

        StringBuilder b=new StringBuilder(quizQuestionShareText(q,mode));
        JSONArray opts=optionsAsArray(q,mode);

        b.append("\n\nПравильные ответы:");

        ArrayList<Integer> sorted=new ArrayList<>(correct);
        Collections.sort(sorted);

        for(int i:sorted){
            b.append("\n").append(i+1);
            if(i>=0 && i<opts.length())
                b.append(". ").append(opts.optString(i));
        }

        String note=q.optString("note");
        if(!note.isEmpty())
            b.append("\n\nРазбор:\n").append(note);

        String src=sources(q);
        if(!src.isEmpty())
            b.append("\n\nИсточник: ").append(src);

        return b.toString().trim();
    }

    private String quizMatchResultShareText(JSONObject q){
        StringBuilder b=new StringBuilder(quizQuestionShareText(q,"match"));

        JSONArray ex=q.optJSONArray("explanations");
        if(ex!=null && ex.length()>0){
            b.append("\n\nРазбор:");
            for(int i=0;i<ex.length();i++)
                b.append("\n• ").append(ex.optString(i));
        }

        String src=sources(q);
        if(!src.isEmpty())
            b.append("\n\nИсточник: ").append(src);

        return b.toString().trim();
    }

    private String freeResultShareText(JSONObject q,String mode){
        StringBuilder b=new StringBuilder(quizQuestionShareText(q,mode));

        String model=q.optString("model");
        if(!model.isEmpty())
            b.append("\n\nЭталон ответа:\n").append(model);

        String key=q.optString("key");
        if(!key.isEmpty())
            b.append("\n\nКлюч: ").append(key);

        String src=sources(q);
        if(!src.isEmpty())
            b.append("\n\nИсточник: ").append(src);

        return b.toString().trim();
    }

    private ArrayAdapter<String> themedSpinnerAdapter(ArrayList<String> values){
        ArrayAdapter<String> ad=new ArrayAdapter<String>(
                this,android.R.layout.simple_spinner_item,values){

            private TextView tune(TextView t,boolean dropdown){
                t.setTextColor(ink());
                t.setTextSize(sz(15));
                t.setTypeface(tf(false));
                t.setPadding(dp(12),dp(12),dp(12),dp(12));

                if(dropdown)
                    t.setBackgroundColor(
                            dark?Color.rgb(42,49,45):Color.rgb(255,254,250));
                else
                    t.setBackgroundColor(Color.TRANSPARENT);

                return t;
            }

            @Override public View getView(
                    int position,View convertView,ViewGroup parent){
                return tune(
                        (TextView)super.getView(position,convertView,parent),
                        false);
            }

            @Override public View getDropDownView(
                    int position,View convertView,ViewGroup parent){
                return tune(
                        (TextView)super.getDropDownView(
                                position,convertView,parent),
                        true);
            }
        };

        ad.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        return ad;
    }


    private Button smallAction(String label){Button b=outline(label);b.setTextSize(sz(11.2f));b.setSingleLine(true);b.setMinWidth(0);b.setMinimumWidth(0);return b;}
    private void copyText(String x){ClipboardManager c=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);if(c!=null){c.setPrimaryClip(ClipData.newPlainText("Аль-Фатиха",x));toast("Скопировано");}}
    private void shareText(String x){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,x);startActivity(Intent.createChooser(i,"Поделиться"));}
    private LinearLayout shareRow(String x){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);r.setPadding(0,dp(8),0,dp(2));Button c=smallAction("⧉ Копировать"),h=smallAction("↗ Поделиться");c.setOnClickListener(v->copyText(x));h.setOnClickListener(v->shareText(x));r.addView(c,new LinearLayout.LayoutParams(0,dp(46),1));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(46),1);lp.setMargins(dp(7),0,0,0);r.addView(h,lp);return r;}
    private boolean isMindMode(String m){return "mind_quick".equals(m)||"mind_exam".equals(m);}
    private JSONArray quickMindArray(){JSONArray a=arr("mind_exam.json"),o=new JSONArray();Set<Integer> ids=new HashSet<>(Arrays.asList(1,2,3,4,5,6,7,8,9,11,12,13,14,15,20));for(int i=0;i<a.length();i++){JSONObject q=a.optJSONObject(i);if(q!=null&&ids.contains(q.optInt("id",-1)))o.put(q);}return o;}
    private String lessonText(JSONObject o){return o.optString("t")+"\n"+o.optString("ru")+"\n\nПонять смысл:\n"+o.optString("meaning")+"\n\nЧто должно быть в сердце:\n"+o.optString("heart")+"\n\nВо время намаза:\n"+o.optString("prompt")+"\n\nИсточник: "+o.optString("src");}
    private String questionText(JSONObject q,String mode){StringBuilder b=new StringBuilder(qText(q,mode));JSONArray a=optionsAsArray(q,mode);if(a.length()>0){b.append("\n\nВарианты:");for(int i=0;i<a.length();i++)b.append("\n").append(i+1).append(". ").append(a.optString(i));}return b.toString();}
    private String answerText(JSONObject q,String mode,int correct){StringBuilder b=new StringBuilder(questionText(q,mode));JSONArray a=optionsAsArray(q,mode);if(correct>=0&&correct<a.length())b.append("\n\nПравильный ответ: ").append(correct+1).append(". ").append(a.optString(correct));String e=explanation(q,mode,correct);if(!e.isEmpty())b.append("\n\nРазбор:\n").append(e);String src=sources(q);if(!src.isEmpty())b.append("\n\nИсточник: ").append(src);return b.toString();}

    private void clear(String type,String arg,boolean push){
        if(push && current!=null && (!current.type.equals(type)||!current.arg.equals(arg))) history.push(current);
        current=new Screen(type,arg);
        page.removeAllViews(); page.setBackgroundColor(bg()); scroll.scrollTo(0,0);
        page.setAlpha(0f); page.setTranslationY(dp(7));
        page.post(()->page.animate().alpha(1f).translationY(0f).setDuration(155).start());
    }

    private void appTop(){
        LinearLayout top=newSurface(dark?Color.rgb(36,43,39):Color.rgb(252,250,245),26,12,5);
        top.setOrientation(LinearLayout.HORIZONTAL); top.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams tlp=new LinearLayout.LayoutParams(-1,-2); tlp.setMargins(0,0,0,dp(10));
        page.addView(top,tlp);

        TextView mark=text("ف",26,dark?blend(C_SAGE,Color.WHITE,.48f):C_SAGE,true); mark.setGravity(Gravity.CENTER); mark.setTypeface(Typeface.create("serif",Typeface.BOLD));
        mark.setBackground(surfaceBg(dark?Color.rgb(45,63,54):Color.rgb(233,241,236),dark?Color.rgb(42,58,50):Color.rgb(242,246,241),16,dark?Color.rgb(67,88,77):Color.rgb(204,219,210)));
        top.addView(mark,new LinearLayout.LayoutParams(dp(54),dp(54)));

        LinearLayout titles=new LinearLayout(this); titles.setOrientation(LinearLayout.VERTICAL); titles.setPadding(dp(12),0,dp(5),0);
        titles.addView(text("Аль-Фатиха",20,ink(),false));
        titles.addView(text("Изучение Аль-Фатихи",12.5f,muted(),false));
        top.addView(titles,new LinearLayout.LayoutParams(0,-2,1));

        Button theme=miniButton(dark?"☀":"☾");
        theme.setContentDescription(dark?"Светлая тема":"Тёмная тема");
        theme.setOnClickListener(v->{Screen s=current;dark=!dark;prefs.edit().putBoolean("dark",dark).apply();buildShell();restore(s);});
        top.addView(theme);
        Button aa=miniButton("Aa"); aa.setContentDescription("Настройки текста"); aa.setOnClickListener(v->renderSettings(true)); top.addView(aa);

        if(!"home".equals(current.type)) return;
        addCourseDots();
    }

    private void addCourseDots(){
        LinearLayout dots=new LinearLayout(this); dots.setOrientation(LinearLayout.HORIZONTAL); dots.setGravity(Gravity.CENTER);
        HashSet<String> seen=new HashSet<>(prefs.getStringSet("mind_seen",new HashSet<>()));
        for(int i=0;i<8;i++){
            View v=new View(this);
            v.setBackground(solidBg(seen.contains(String.valueOf(i))?C_BLUE:blend(C_BLUE,Color.WHITE,.55f),99,0));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(7),1); lp.setMargins(dp(2),dp(6),dp(2),dp(8)); dots.addView(v,lp);
        }
        page.addView(dots,new LinearLayout.LayoutParams(-1,-2));
    }

    private void heroArabic(){
        gap(2);
        TextView ar=text("اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ",30,ink(),true);
        ar.setGravity(Gravity.CENTER); ar.setTextDirection(View.TEXT_DIRECTION_RTL); ar.setTypeface(Typeface.create("serif",Typeface.BOLD));
        add(ar);
        TextView tr=text("«Веди нас прямым путём»",14,muted(),false); tr.setGravity(Gravity.CENTER); add(tr); gap(13);
    }

    private void header(String title,String sub){
        TextView t=text(title,27,ink(),false); add(t);
        if(sub!=null&&!sub.isEmpty()){TextView s=text(sub,14.5f,muted(),false); s.setPadding(0,dp(4),0,dp(5));add(s);} gap(8);
    }

    private TextView kicker(String s,int color){
        int tc=dark?blend(color,Color.WHITE,.48f):color;
        TextView t=text(s,11.5f,tc,true);t.setLetterSpacing(.06f);t.setAllCaps(true);
        int fill=dark?blend(color,Color.BLACK,.58f):blend(color,Color.WHITE,.70f);
        int border=dark?blend(color,Color.WHITE,.34f):blend(color,Color.WHITE,.55f);
        t.setBackground(solidBg(fill,99,border));t.setPadding(dp(10),dp(6),dp(10),dp(6));return t;
    }

    private TextView arrowChip(){
        TextView a=text("→",19,muted(),false);a.setGravity(Gravity.CENTER);
        a.setBackground(surfaceBg(dark?Color.rgb(44,51,47):Color.rgb(244,245,241),dark?Color.rgb(40,47,43):Color.rgb(239,242,238),15,line()));
        a.setElevation(dp(1)); return a;
    }

    private void cardHead(LinearLayout card,String tag,int tagColor,String title){
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.TOP);
        LinearLayout left=new LinearLayout(this);left.setOrientation(LinearLayout.VERTICAL);
        left.addView(kicker(tag,tagColor),new LinearLayout.LayoutParams(-2,-2));
        TextView h=text(title,25,ink(),false); h.setPadding(0,dp(10),0,0); left.addView(h);
        row.addView(left,new LinearLayout.LayoutParams(0,-2,1));
        row.addView(arrowChip(),new LinearLayout.LayoutParams(dp(48),dp(48)));
        card.addView(row);
    }

    private ProgressBar progressBar(int progress,int color){
        ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        p.setMax(100);p.setProgress(progress);p.setIndeterminate(false);
        p.setProgressTintList(ColorStateList.valueOf(color));
        p.setProgressBackgroundTintList(ColorStateList.valueOf(dark?Color.rgb(58,67,62):Color.rgb(214,222,217)));
        p.setPadding(0,0,0,0);return p;
    }

    private int seenMindCount(){return prefs.getStringSet("mind_seen",new HashSet<>()).size();}
    private int repeatCount(){return prefs.getStringSet("wrong_ids",new HashSet<>()).size();}

    private void renderHome(boolean push){
        clear("home","",push);currentSection="home";appTop();heroArabic();
        int seen=seenMindCount();int pct=Math.min(100,seen*100/8);
        LinearLayout m=card(sageSoft());cardHead(m,"ГЛАВНЫЙ РАЗДЕЛ",C_SAGE,"Осознанное чтение Аль-Фатихи");
        m.addView(text("Понимайте смысл произносимых слов и удерживайте его в сердце во время намаза.",15.5f,muted(),false));
        LinearLayout meta=new LinearLayout(this);meta.setOrientation(LinearLayout.HORIZONTAL);meta.setGravity(Gravity.CENTER_VERTICAL);
        meta.addView(text("Прогресс основного урока",13,muted(),false),new LinearLayout.LayoutParams(0,-2,1));
        meta.addView(text(seen+" из 8",13,ink(),true));m.addView(meta);
        m.addView(progressBar(pct,C_SAGE),new LinearLayout.LayoutParams(-1,dp(10)));
        Button bm=action(seen>0?"Продолжить":"Начать обучение",C_SAGE);
        Runnable openMind=()->{
            if(seen>0){
                int last=prefs.getInt("mind_last_idx",-1);
                if(last<0 || last>=8){
                    last=Math.min(7,Math.max(0,seen-1));
                }
                renderMindLesson(last,true);
            }else{
                renderMindHub(true);
            }
        };
        bm.setOnClickListener(v->openMind.run());
        m.addView(bm);
        m.setOnClickListener(v->openMind.run());

        LinearLayout q=card(blueSoft());cardHead(q,"ПРОВЕРКА ЗНАНИЙ",C_BLUE,"Викторина по Аль-Фатихе");
        q.addView(text("Сложные вопросы по тафсиру: близкие варианты, анализ, сопоставление и экспертные режимы.",15.5f,muted(),false));
        String lm=prefs.getString("last_mode","");
        if(!lm.isEmpty()){
            int idx=prefs.getInt("idx_"+lm,0),total=quizArray(lm).length();
            LinearLayout resume=newSurface(dark?Color.rgb(43,50,54):Color.rgb(249,249,247),16,12,1);
            resume.addView(text("Последний режим: "+modeTitle(lm)+" · задание "+(idx+1)+" из "+Math.max(total,1),13,ink(),false));
            LinearLayout.LayoutParams rlp=new LinearLayout.LayoutParams(-1,-2);rlp.setMargins(0,dp(8),0,0);q.addView(resume,rlp);
        }
        Button bq=action(lm.isEmpty()?"Открыть викторину":"Продолжить",C_BLUE);
        bq.setOnClickListener(v->{if(lm.isEmpty())renderQuizHub(true);else renderNativeQuiz(lm,prefs.getInt("idx_"+lm,0),true);});q.addView(bq);q.setOnClickListener(v->renderQuizHub(true));

        gap(8);TextView ph=text("ВАШ ПРОГРЕСС",12,muted(),true);ph.setLetterSpacing(.08f);add(ph);
        LinearLayout stats=new LinearLayout(this);stats.setOrientation(LinearLayout.HORIZONTAL);
        int a=prefs.getInt("answered_total",0),c=prefs.getInt("correct_total",0),acc=a==0?0:c*100/a;
        stats.addView(statCard(acc+"%","Общий\nрезультат"),new LinearLayout.LayoutParams(0,dp(104),1));
        LinearLayout.LayoutParams sm=new LinearLayout.LayoutParams(0,dp(104),1);sm.setMargins(dp(8),0,0,0);stats.addView(statCard(String.valueOf(a),"Пройдено"),sm);
        LinearLayout.LayoutParams sm2=new LinearLayout.LayoutParams(0,dp(104),1);sm2.setMargins(dp(8),0,0,0);stats.addView(statCard(String.valueOf(repeatCount()),"На\nповторение"),sm2);
        page.addView(stats,new LinearLayout.LayoutParams(-1,-2));

        gap(14);TextView ah=text("ДОПОЛНИТЕЛЬНО",12,muted(),true);ah.setLetterSpacing(.08f);add(ah);
        LinearLayout tools=new LinearLayout(this);tools.setOrientation(LinearLayout.HORIZONTAL);
        tools.addView(toolCard("↻","Повторение",()->renderRepeatHub(true)),new LinearLayout.LayoutParams(0,dp(88),1));
        LinearLayout.LayoutParams tm=new LinearLayout.LayoutParams(0,dp(88),1);tm.setMargins(dp(8),0,0,0);tools.addView(toolCard("✓","Экзамен",()->renderNativeQuiz("mind_exam",0,true)),tm);
        LinearLayout.LayoutParams tm2=new LinearLayout.LayoutParams(0,dp(88),1);tm2.setMargins(dp(8),0,0,0);tools.addView(toolCard("◎","Профиль",()->renderProfile(true)),tm2);
        page.addView(tools);
    }

    private LinearLayout statCard(String value,String label){
        LinearLayout c=newSurface(panel(),20,10,2);c.setGravity(Gravity.CENTER);
        TextView v=text(value,23,ink(),true);v.setGravity(Gravity.CENTER);c.addView(v);
        TextView l=text(label,11.5f,muted(),false);l.setGravity(Gravity.CENTER);c.addView(l);
        c.setOnClickListener(x->renderProfile(true));return c;
    }

    private LinearLayout toolCard(String icon,String title,Runnable r){
        LinearLayout c=newSurface(panel(),20,10,2);c.setGravity(Gravity.CENTER);
        TextView i=text(icon,22,C_SAGE,false);i.setGravity(Gravity.CENTER);c.addView(i);
        TextView t=text(title,12.5f,ink(),true);t.setGravity(Gravity.CENTER);c.addView(t);
        c.setOnClickListener(v->r.run());return c;
    }

    private String progressSummary(){
        int a=prefs.getInt("answered_total",0),c=prefs.getInt("correct_total",0);
        return "Отвечено: "+a+"   •   Верно: "+c+"   •   Точность: "+(a==0?0:(c*100/a))+"%";
    }

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

    private void addParagraphs(LinearLayout parent,String body,float size){
        String clean=body==null?"":body.trim();
        for(String p:clean.split("\\n\\s*\\n")){
            if(p.trim().isEmpty())continue;
            TextView t=text(p.trim(),size,ink(),false);t.setPadding(0,dp(5),0,dp(8));parent.addView(t);
        }
    }

    private void renderIntro(boolean push){
        clear("intro","",push);currentSection="mind";appTop();header("Важное предисловие","Почему Аль-Фатиху важно читать осознанно и что меняется, когда в чтении присутствует сердце.");
        JSONArray a=arr("mind_intro.json");
        int[] tones={sandSoft(),blueSoft(),sageSoft()};
        for(int i=0;i<a.length();i++){
            JSONObject o=a.optJSONObject(i);LinearLayout c=card(tones[i%tones.length]);
            TextView n=kicker(String.format(Locale.getDefault(),"%02d",i+1),i%3==0?Color.rgb(145,104,42):i%3==1?C_BLUE:C_SAGE);c.addView(n,new LinearLayout.LayoutParams(-2,-2));
            c.addView(text(o.optString("title"),21,ink(),true));addParagraphs(c,o.optString("text"),15.5f);
        }
        page.addView(contentActions("intro",introShareText(),true));
        StringBuilder sb=new StringBuilder();for(int j=0;j<a.length();j++){JSONObject x=a.optJSONObject(j);sb.append(x.optString("title")).append("\n").append(x.optString("text")).append("\n\n");}page.addView(shareRow(sb.toString()));Button b=action("Начать урок",C_SAGE);b.setOnClickListener(v->renderMindLesson(0,true));page.addView(b);
    }

    private void showLessonPicker(int currentIdx){
        JSONArray data=arr("mind_data.json");String[] names=new String[data.length()];
        for(int i=0;i<data.length();i++)names[i]=(i+1)+". "+data.optJSONObject(i).optString("t");
        new AlertDialog.Builder(this).setTitle("Перейти к части").setSingleChoiceItems(names,currentIdx,(d,which)->{d.dismiss();renderMindLesson(which,true);}).setNegativeButton("Отмена",null).show();
    }

    private void renderMindLesson(int idx,boolean push){
        JSONArray data=arr("mind_data.json");if(idx<0||idx>=data.length())idx=0;
        prefs.edit().putInt("mind_last_idx",idx).apply();
        clear("mindLesson",String.valueOf(idx),push);currentSection="mind";appTop();
        HashSet<String> seen=new HashSet<>(prefs.getStringSet("mind_seen",new HashSet<>()));seen.add(String.valueOf(idx));prefs.edit().putStringSet("mind_seen",seen).apply();
        Button part=outline("Часть "+(idx+1)+" из "+data.length()+"   ▾");final int ci=idx;part.setOnClickListener(v->showLessonPicker(ci));page.addView(part,new LinearLayout.LayoutParams(-1,dp(48)));
        JSONObject o=data.optJSONObject(idx);header(o.optString("t"),o.optString("ru"));
        JSONArray deep=arr("mind_deep_data.json"),qd=arr("mind_qayyim_deep.json"),scholars=arr("mind_scholars.json");

        LinearLayout words=card(sageSoft());words.addView(kicker("СЛОВА И СМЫСЛ",C_SAGE));JSONArray wa=o.optJSONArray("words");
        if(wa!=null)for(int i=0;i<wa.length();i++){JSONArray w=wa.optJSONArray(i);LinearLayout line=new LinearLayout(this);line.setOrientation(LinearLayout.HORIZONTAL);line.setGravity(Gravity.TOP);TextView dot=text("•",16,C_SAGE,true);line.addView(dot);LinearLayout wt=new LinearLayout(this);wt.setOrientation(LinearLayout.VERTICAL);wt.setPadding(dp(8),0,0,dp(5));wt.addView(text(w.optString(0),15,ink(),true));wt.addView(text(w.optString(1),14,muted(),false));line.addView(wt,new LinearLayout.LayoutParams(0,-2,1));words.addView(line);}
        sectionCard("Понять смысл",o.optString("meaning"),blueSoft(),C_BLUE);
        sectionCard("Что должно быть в сердце",o.optString("heart"),sageSoft(),C_SAGE);
        sectionCard("Во время намаза",o.optString("prompt"),sandSoft(),Color.rgb(158,120,52));

        if(deep.length()>idx){JSONArray d=deep.optJSONArray(idx);if(d!=null){
            LinearLayout box=card(panel());Button toggle=outline("Раскрыть смысл глубже   ↓");box.addView(toggle,new LinearLayout.LayoutParams(-1,dp(54)));
            LinearLayout holder=newSurface(dark?Color.rgb(40,47,43):Color.rgb(248,246,240),18,14,1);holder.setVisibility(View.GONE);addDeep(holder,d);holder.addView(contentActions("deep:"+idx,deepShareText("Глубокий разбор",d),true));box.addView(holder);
            toggle.setOnClickListener(v->toggleInline(holder,toggle,"Раскрыть смысл глубже   ↓","Скрыть глубокий разбор   ↑"));
        }}
        if(qd.length()>idx){JSONObject qo=qd.optJSONObject(idx);if(qo!=null){
            LinearLayout box=card(sandSoft());Button toggle=outline("Разбор Ибн аль-Каййима   ↓");box.addView(toggle,new LinearLayout.LayoutParams(-1,dp(54)));
            LinearLayout holder=newSurface(panel(),18,14,1);holder.setVisibility(View.GONE);
            holder.addView(text("Слова и смысл разбора",13,Color.rgb(145,104,42),true));addParagraphs(holder,qo.optString("words"),14.5f);
            holder.addView(text("Подробнее",13,Color.rgb(145,104,42),true));addParagraphs(holder,qo.optString("detail"),14.5f);
            JSONArray bens=qo.optJSONArray("benefits");if(bens!=null){holder.addView(text("Пользы для осознанного чтения",13,Color.rgb(145,104,42),true));for(int k=0;k<bens.length();k++)holder.addView(text("• "+bens.optString(k),14.5f,ink(),false));}
            holder.addView(text("Источник: "+qo.optString("src"),12.5f,muted(),false));holder.addView(contentActions("qayyim:"+idx,qayyimShareText(qo),true));box.addView(holder);
            toggle.setOnClickListener(v->toggleInline(holder,toggle,"Разбор Ибн аль-Каййима   ↓","Скрыть разбор Ибн аль-Каййима   ↑"));
        }}
        if(scholars.length()>idx){JSONObject so=scholars.optJSONObject(idx);if(so!=null){
            LinearLayout sc=card(lavSoft());sc.addView(kicker("ДОПОЛНИТЕЛЬНЫЙ РАЗБОР",Color.rgb(112,96,134)));
            Iterator<String> it=so.keys();while(it.hasNext()){String key=it.next();JSONObject sv=so.optJSONObject(key);if(sv==null)continue;Button tb=outline(sv.optString("title")+"   ↓");sc.addView(tb,new LinearLayout.LayoutParams(-1,dp(52)));LinearLayout sh=newSurface(panel(),16,13,1);sh.setVisibility(View.GONE);addParagraphs(sh,sv.optString("text"),14.5f);JSONArray vb=sv.optJSONArray("benefits");if(vb!=null)for(int k=0;k<vb.length();k++)sh.addView(text("• "+vb.optString(k),14,ink(),false));String src=sv.optString("src");if(!src.isEmpty())sh.addView(text("Источник: "+src,12.5f,muted(),false));sc.addView(sh);String title=sv.optString("title");tb.setOnClickListener(v->toggleInline(sh,tb,title+"   ↓","Скрыть: "+title+"   ↑"));}
        }}
        sectionCard("Источник",o.optString("src"),panel(),C_SAGE);page.addView(shareRow(lessonText(o)));

        page.addView(contentActions("lesson:"+idx,lessonShareText(o),true));

        LinearLayout nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);nav.setPadding(0,dp(8),0,0);
        if(idx>0){Button prev=outline("← Предыдущий");final int pi=idx-1;prev.setOnClickListener(v->renderMindLesson(pi,true));nav.addView(prev,new LinearLayout.LayoutParams(0,dp(54),1));}
        if(idx<data.length()-1){Button next=outline("Следующий →");final int ni=idx+1;next.setOnClickListener(v->renderMindLesson(ni,true));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(54),1);lp.setMargins(idx>0?dp(8):0,0,0,0);nav.addView(next,lp);}page.addView(nav);
    }

    private void toggleInline(View holder,Button b,String closed,String open){
        boolean show=holder.getVisibility()!=View.VISIBLE;
        if(show){holder.setAlpha(0f);holder.setTranslationY(-dp(5));holder.setVisibility(View.VISIBLE);holder.animate().alpha(1f).translationY(0).setDuration(170).start();}
        else holder.setVisibility(View.GONE);
        b.setText(show?open:closed);
    }

    private void addDeep(LinearLayout holder,JSONArray d){
        String[] names={"ТАФСИР","ЧТО МОЖНО ИЗВЛЕЧЬ","РАЗМЫШЛЕНИЕ ДЛЯ СЕРДЦА","ПРАКТИКА В НАМАЗЕ","ОПОРА НА ИСТОЧНИКИ"};int[] cols={C_BLUE,Color.rgb(145,104,42),C_SAGE,C_BLUE,C_SAGE};
        for(int i=0;i<d.length()&&i<5;i++){LinearLayout c=newSurface(i==1?sandSoft():i==2?sageSoft():i==3?blueSoft():panel(),16,13,1);c.addView(kicker(names[i],cols[i]));Object x=d.opt(i);if(x instanceof JSONArray){JSONArray a=(JSONArray)x;for(int k=0;k<a.length();k++)c.addView(text("• "+a.optString(k),14.5f,ink(),false));}else addParagraphs(c,String.valueOf(x),14.5f);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(5),0,dp(5));holder.addView(c,lp);}
    }

    private void sectionCard(String title,String body,int color,int accent){
        LinearLayout c=card(color);c.addView(kicker(title.toUpperCase(Locale.ROOT),accent));addParagraphs(c,body,14.8f);
    }

    private void renderQuizHub(boolean push){
        clear("quizHub","",push);currentSection="quiz";appTop();header("Викторина по Аль-Фатихе","Проверка знаний по тафсиру: от классических вопросов до тонких экспертных различий.");
        modeCard("БАЗОВЫЙ","Классическая викторина","200 вопросов · 4 близких варианта","classic",C_BLUE,blueSoft());
        modeCard("2 ИЗ 5","Два правильных","20 заданий · выбрать все верные варианты","multi",C_SAGE,sageSoft());
        modeCard("СОПОСТАВЛЕНИЕ","Термин ↔ смысл","20 заданий","match",Color.rgb(112,96,134),lavSoft());
        modeCard("ХАДИС → СМЫСЛ","Хадис → смысл Аль-Фатихи","30 заданий","hadith",Color.rgb(150,111,80),sandSoft());
        modeCard("БЕЗ ВАРИАНТОВ","Самостоятельный ответ","20 заданий","free",Color.rgb(91,123,109),sageSoft());
        modeCard("АНАЛИЗ","Эксперт","20 сложных вопросов","expert",Color.rgb(82,104,125),blueSoft());
    }

    private void modeCard(String kicker,String title,String sub,String mode,int color,int tone){
        LinearLayout c=card(tone);c.addView(this.kicker(kicker,C_BLUE));c.addView(text(title,20.5f,ink(),true));c.addView(text(sub,14,muted(),false));
        int saved=prefs.getInt("idx_"+mode,0);c.addView(text("Продолжить с задания "+(saved+1),12.5f,muted(),false));Button b=action("Открыть",color);b.setOnClickListener(v->renderNativeQuiz(mode,saved,true));c.addView(b);c.setOnClickListener(v->renderNativeQuiz(mode,saved,true));
    }

    private JSONArray quizArray(String mode){switch(mode){case"classic":return arr("quiz_data.json");case"multi":return arr("multi_data.json");case"match":return arr("match_data.json");case"hadith":return arr("hadith_data.json");case"free":return arr("free_data.json");case"expert":return arr("expert_data.json");case"mind_quick":return quickMindArray();case"mind_exam":return arr("mind_exam.json");default:return new JSONArray();}}

    private void renderNativeQuiz(String mode,int idx,boolean push){JSONArray a=quizArray(mode);if(a.length()==0){toast("Нет данных");return;}if(idx<0||idx>=a.length())idx=0;clear("quiz",mode+":"+idx,push);currentSection=isMindMode(mode)?"mind":"quiz";prefs.edit().putInt("idx_"+mode,idx).putString("last_mode",mode).apply();appTop();LinearLayout meta=new LinearLayout(this);meta.setOrientation(LinearLayout.HORIZONTAL);meta.setGravity(Gravity.CENTER_VERTICAL);String lab="mind_quick".equals(mode)?"КОРОТКАЯ ПРОВЕРКА":"mind_exam".equals(mode)?"ИТОГОВЫЙ ЭКЗАМЕН":modeTitle(mode).toUpperCase(Locale.ROOT);meta.addView(kicker(lab,isMindMode(mode)?C_SAGE:C_BLUE),new LinearLayout.LayoutParams(0,-2,1));Button jump=outline((idx+1)+" / "+a.length()+"  ▾");int ci=idx;jump.setOnClickListener(v->showQuestionPicker(mode,ci,a.length()));meta.addView(jump,new LinearLayout.LayoutParams(dp(100),dp(44)));page.addView(meta);ProgressBar pb=progressBar((idx+1)*100/a.length(),isMindMode(mode)?C_SAGE:C_BLUE);LinearLayout.LayoutParams plp=new LinearLayout.LayoutParams(-1,dp(8));plp.setMargins(0,dp(8),0,dp(12));page.addView(pb,plp);JSONObject q=a.optJSONObject(idx);if(mode.equals("match")){renderMatch(q,mode,idx,a.length());return;}if(mode.equals("free")||(isMindMode(mode)&&"self".equals(q.optString("type")))){renderFree(q,mode,idx,a.length());return;}if(mode.equals("multi")){renderMulti(q,mode,idx,a.length());return;}renderMcq(q,mode,idx,a.length());}

    private void showQuestionPicker(String mode,int currentIdx,int total){final Dialog d=new Dialog(this);LinearLayout box=newSurface(panel(),24,15,0);box.addView(text("Перейти к заданию",20,ink(),true));GridView g=new GridView(this);g.setNumColumns(5);g.setHorizontalSpacing(dp(7));g.setVerticalSpacing(dp(7));g.setAdapter(new BaseAdapter(){public int getCount(){return total;}public Object getItem(int p){return p;}public long getItemId(int p){return p;}public View getView(int p,View v,ViewGroup parent){TextView t=v instanceof TextView?(TextView)v:text("",14,ink(),true);t.setGravity(Gravity.CENTER);t.setMinHeight(dp(48));t.setMinimumHeight(dp(48));t.setText(String.valueOf(p+1));boolean on=p==currentIdx;t.setTextColor(on?Color.WHITE:ink());t.setBackground(solidBg(on?C_SAGE:panel(),13,on?C_SAGE:line()));return t;}});box.addView(g,new LinearLayout.LayoutParams(-1,0,1));Button x=outline("Отмена");x.setOnClickListener(v->d.dismiss());box.addView(x,new LinearLayout.LayoutParams(-1,dp(50)));d.setContentView(box);g.setOnItemClickListener((a,v,p,id)->{d.dismiss();renderNativeQuiz(mode,p,true);});d.show();Window w=d.getWindow();if(w!=null){w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));int sw=getResources().getDisplayMetrics().widthPixels,sh=getResources().getDisplayMetrics().heightPixels;w.setLayout((int)(sw*.92f),(int)(sh*.78f));}}

    private String modeTitle(String m){switch(m){case"classic":return"Классическая викторина";case"multi":return"Два правильных";case"match":return"Сопоставление";case"hadith":return"Хадис → смысл";case"free":return"Без вариантов";case"expert":return"Эксперт";case"mind_quick":return"Проверка понимания";case"mind_exam":return"Итоговый экзамен";}return"Викторина";}
    private String qText(JSONObject q,String mode){if(mode.equals("hadith"))return q.optString("hadith")+"\n\n"+q.optString("question");if(isMindMode(mode))return q.optString("q");return q.optString("question");}

    private JSONArray optionsAsArray(JSONObject q,String mode){
        Object o=q.opt("options");if(o instanceof JSONArray)return(JSONArray)o;
        if(o instanceof JSONObject){JSONObject jo=(JSONObject)o;JSONArray a=new JSONArray();for(String k:new String[]{"A","B","C","D","E","F"})if(jo.has(k)){Object v=jo.opt(k);if(v instanceof JSONObject)a.put(((JSONObject)v).optString("text"));else a.put(String.valueOf(v));}return a;}
        o=q.opt("opts");return o instanceof JSONArray?(JSONArray)o:new JSONArray();
    }

    private int correctIndex(JSONObject q,String mode){Object c=q.opt("correct");if(c instanceof Number)return((Number)c).intValue();if(c!=null){String z=String.valueOf(c);if(z.length()==1&&Character.isLetter(z.charAt(0)))return Character.toUpperCase(z.charAt(0))-'A';try{return Integer.parseInt(z);}catch(Exception e){}}if(isMindMode(mode))return q.optInt("a",-1);return-1;}

    private String explanation(JSONObject q,String mode,int selected){
        if(mode.equals("classic")){JSONObject opts=q.optJSONObject("options");String key=String.valueOf((char)('A'+selected));return opts!=null&&opts.optJSONObject(key)!=null?opts.optJSONObject(key).optString("explanation"):q.optString("note");}
        JSONArray ex=q.optJSONArray("explanations");if(ex!=null&&selected>=0&&selected<ex.length())return ex.optString(selected);return q.optString("why",q.optString("note"));
    }

    private String sources(JSONObject q){Object so=q.opt("sources");if(so instanceof String&&!((String)so).isEmpty())return(String)so;JSONArray a=so instanceof JSONArray?(JSONArray)so:q.optJSONArray("sourceList");StringBuilder b=new StringBuilder();if(a!=null)for(int i=0;i<a.length();i++){Object x=a.opt(i);if(x instanceof JSONObject){JSONObject o=(JSONObject)x;b.append("• ").append(o.optString("label"));String d=o.optString("detail");if(!d.isEmpty())b.append(" — ").append(d);b.append("\n");}else b.append("• ").append(String.valueOf(x)).append("\n");}String src=q.optString("src");if(!src.isEmpty()){if(b.length()>0)b.append("\n");b.append(src);}return b.toString().trim();}

    private int choiceTone(int i){int[] p={blueSoft(),sageSoft(),sandSoft(),lavSoft()};return p[i%p.length];}

    private ChoiceView choice(int i,String label,boolean multi){
        ChoiceView cv=new ChoiceView();cv.index=i;
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.HORIZONTAL);root.setGravity(Gravity.CENTER_VERTICAL);root.setPadding(dp(12),dp(12),dp(14),dp(12));root.setElevation(dp(2));
        TextView marker=text(String.valueOf(i+1),15,i%2==0?C_BLUE:C_SAGE,true);marker.setGravity(Gravity.CENTER);marker.setBackground(solidBg(dark?Color.rgb(48,55,51):blend(choiceTone(i),Color.WHITE,.28f),14,line()));root.addView(marker,new LinearLayout.LayoutParams(dp(48),dp(48)));
        TextView tv=text(label,15.5f,ink(),false);tv.setPadding(dp(14),0,0,0);root.addView(tv,new LinearLayout.LayoutParams(0,-2,1));
        cv.root=root;cv.marker=marker;cv.label=tv;renderChoice(cv,false,false,false);return cv;
    }

    private void renderChoice(ChoiceView cv,boolean selected,boolean correct,boolean wrong){
        cv.selected=selected;int fill=choiceTone(cv.index),stroke=line(),markFill=dark?Color.rgb(48,55,51):blend(fill,Color.WHITE,.18f),markText=cv.index%2==0?C_BLUE:C_SAGE;
        if(selected){stroke=cv.index%2==0?C_BLUE:C_SAGE;fill=dark?blend(stroke,Color.BLACK,.55f):blend(stroke,Color.WHITE,.82f);markFill=stroke;markText=Color.WHITE;}
        if(correct){stroke=C_GOOD;fill=dark?Color.rgb(37,63,50):C_GOOD_BG;markFill=C_GOOD;markText=Color.WHITE;}
        if(wrong){stroke=C_BAD;fill=dark?Color.rgb(68,42,42):C_BAD_BG;markFill=C_BAD;markText=Color.WHITE;}
        cv.root.setBackground(surfaceBg(fill,dark?fill:blend(fill,Color.WHITE,.13f),22,stroke));cv.root.setElevation(dp(selected||correct||wrong?5:2));
        cv.marker.setBackground(solidBg(markFill,14,selected||correct||wrong?stroke:line()));cv.marker.setTextColor(markText);cv.marker.setText(correct?"✓":wrong?"×":String.valueOf(cv.index+1));cv.label.setTypeface(tf(selected||correct));
    }

    private void renderMcq(JSONObject q,String mode,int idx,int total){
        LinearLayout qc=card(panel());qc.addView(text(qText(q,mode),20.5f,ink(),false));JSONArray opts=optionsAsArray(q,mode);ArrayList<ChoiceView> choices=new ArrayList<>();final int[] selected={-1};
        for(int i=0;i<opts.length();i++){ChoiceView cv=choice(i,opts.optString(i),false);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(6),0,dp(6));qc.addView(cv.root,lp);choices.add(cv);final int ix=i;cv.root.setOnClickListener(v->{selected[0]=ix;for(ChoiceView x:choices)renderChoice(x,x.index==ix,false,false);});}
        Button check=action("Проверить",C_SAGE);qc.addView(check);bookmarkButton(q,mode,qc);qc.addView(shareRow(questionText(q,mode)));qc.addView(contentActions("",quizQuestionShareText(q,mode),false));
        check.setOnClickListener(v->{if(selected[0]<0){toast("Выберите ответ");return;}int sel=selected[0],correct=correctIndex(q,mode);boolean ok=sel==correct;for(ChoiceView x:choices)renderChoice(x,x.index==sel,x.index==correct,x.index==sel&&!ok);for(ChoiceView x:choices)x.root.setOnClickListener(null);record(mode,q,ok);
            LinearLayout result=card(ok?(dark?Color.rgb(36,60,48):C_GOOD_BG):(dark?Color.rgb(64,42,41):C_BAD_BG));result.addView(text(ok?"✓ Верно":"✕ Неверно. Правильный ответ: "+(correct>=0?(correct+1):"—"),18,ok?C_GOOD:C_BAD,true));String ex=explanation(q,mode,sel);if(!ex.isEmpty())addParagraphs(result,ex,14.8f);String src=sources(q);if(!src.isEmpty())result.addView(text("Источник: "+src,12.7f,muted(),false));addAllExplanations(q,mode,result,opts.length());result.addView(contentActions("",quizResultShareText(q,mode,correct),false));Button next=action(idx+1<total?"Следующий вопрос":"Завершить",C_BLUE);next.setOnClickListener(x->{if(idx+1<total)renderNativeQuiz(mode,idx+1,true);else if(isMindMode(mode))renderMindHub(true);else renderQuizHub(true);});result.addView(next);markDone(check,"Ответ проверен");
        });
    }

    private void addAllExplanations(JSONObject q,String mode,LinearLayout parent,int count){
        boolean has=false;for(int i=0;i<count;i++)if(!explanation(q,mode,i).isEmpty()){has=true;break;}if(!has)return;
        Button more=outline("Почему другие варианты почти правильные?   ↓");parent.addView(more,new LinearLayout.LayoutParams(-1,dp(52)));LinearLayout h=newSurface(panel(),16,13,1);h.setVisibility(View.GONE);
        for(int i=0;i<count;i++){String e=explanation(q,mode,i);if(e.isEmpty())continue;h.addView(text((i+1)+". "+e,13.7f,ink(),false));}
        parent.addView(h);more.setOnClickListener(v->toggleInline(h,more,"Почему другие варианты почти правильные?   ↓","Скрыть разбор вариантов   ↑"));
    }

    private void renderMulti(JSONObject q,String mode,int idx,int total){
        LinearLayout qc=card(panel());qc.addView(text(qText(q,mode),20.5f,ink(),false));qc.addView(text("Выберите все верные ответы. Нажатый вариант заметно выделяется рамкой и номером.",13.5f,muted(),false));JSONArray opts=optionsAsArray(q,mode);ArrayList<ChoiceView> choices=new ArrayList<>();boolean[] selected=new boolean[opts.length()];
        for(int i=0;i<opts.length();i++){ChoiceView cv=choice(i,opts.optString(i),true);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(6),0,dp(6));qc.addView(cv.root,lp);choices.add(cv);final int ix=i;cv.root.setOnClickListener(v->{selected[ix]=!selected[ix];renderChoice(cv,selected[ix],false,false);});}
        Button check=action("Проверить выбранные ответы",C_SAGE);qc.addView(check);bookmarkButton(q,mode,qc);qc.addView(shareRow(questionText(q,mode)));qc.addView(contentActions("",quizQuestionShareText(q,mode),false));
        check.setOnClickListener(v->{JSONArray cor=q.optJSONArray("correct");HashSet<Integer> cs=new HashSet<>();if(cor!=null)for(int i=0;i<cor.length();i++){Object x=cor.opt(i);if(x instanceof Number)cs.add(((Number)x).intValue());else{String z=String.valueOf(x);try{cs.add(z.length()==1&&Character.isLetter(z.charAt(0))?Character.toUpperCase(z.charAt(0))-'A':Integer.parseInt(z));}catch(Exception ignored){}}}HashSet<Integer> us=new HashSet<>();for(int i=0;i<selected.length;i++)if(selected[i])us.add(i);if(us.size()!=cs.size()){toast("Нужно выбрать "+cs.size()+" варианта");return;}boolean ok=us.equals(cs);for(ChoiceView x:choices){renderChoice(x,selected[x.index],cs.contains(x.index),selected[x.index]&&!cs.contains(x.index));x.root.setOnClickListener(null);}record(mode,q,ok);LinearLayout r=card(ok?(dark?Color.rgb(36,60,48):C_GOOD_BG):(dark?Color.rgb(64,42,41):C_BAD_BG));r.addView(text(ok?"✓ Верно":"✕ Есть неточность. Правильные: "+numbers(cs),18,ok?C_GOOD:C_BAD,true));String n=q.optString("note");if(!n.isEmpty())addParagraphs(r,n,14.5f);String src=sources(q);if(!src.isEmpty())r.addView(text("Источник: "+src,12.7f,muted(),false));r.addView(contentActions("",quizMultiResultShareText(q,mode,cs),false));Button next=action(idx+1<total?"Следующее":"Завершить",C_BLUE);next.setOnClickListener(x->{if(idx+1<total)renderNativeQuiz(mode,idx+1,true);else renderQuizHub(true);});r.addView(next);markDone(check,"Ответ проверен");});
    }

    private String numbers(Set<Integer>s){ArrayList<Integer>a=new ArrayList<>(s);Collections.sort(a);StringBuilder b=new StringBuilder();for(int x:a){if(b.length()>0)b.append(", ");b.append(x+1);}return b.toString();}

    private void renderMatch(JSONObject q,String mode,int idx,int total){
        LinearLayout qc=card(panel());qc.addView(kicker("СОПОСТАВЛЕНИЕ",C_BLUE));qc.addView(text(q.optString("title"),20,ink(),true));JSONArray left=q.optJSONArray("left"),right=q.optJSONArray("right"),ans=q.optJSONArray("answer");ArrayList<Spinner> spins=new ArrayList<>();ArrayList<String> values=new ArrayList<>();values.add("— Выберите —");for(int j=0;j<right.length();j++)values.add((j+1)+". "+right.optString(j));
        for(int i=0;i<left.length();i++){LinearLayout item=newSurface(choiceTone(i),18,12,1);item.addView(text((i+1)+". "+left.optString(i),14.5f,ink(),true));Spinner sp=new Spinner(this);ArrayAdapter<String> ad=themedSpinnerAdapter(values);sp.setAdapter(ad);sp.setPopupBackgroundDrawable(solidBg(panel(),14,line()));item.addView(sp);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(5),0,dp(5));qc.addView(item,lp);spins.add(sp);}
        Button check=action("Проверить соответствие",C_SAGE);qc.addView(check);qc.addView(shareRow(q.optString("title")));qc.addView(contentActions("",quizQuestionShareText(q,mode),false));check.setOnClickListener(v->{boolean ok=true;for(int i=0;i<spins.size();i++){int sel=spins.get(i).getSelectedItemPosition()-1;if(sel<0){toast("Заполните все соответствия");return;}int correct=ans.optInt(i,-1);if(sel!=correct&&sel+1!=correct)ok=false;}record(mode,q,ok);LinearLayout r=card(ok?(dark?Color.rgb(36,60,48):C_GOOD_BG):(dark?Color.rgb(64,42,41):C_BAD_BG));r.addView(text(ok?"✓ Всё верно":"✕ Есть неточности",18,ok?C_GOOD:C_BAD,true));JSONArray ex=q.optJSONArray("explanations");if(ex!=null)for(int i=0;i<ex.length();i++)r.addView(text("• "+ex.optString(i),14,ink(),false));String src=sources(q);if(!src.isEmpty())r.addView(text("Источник: "+src,12.7f,muted(),false));r.addView(contentActions("",quizMatchResultShareText(q),false));Button next=action(idx+1<total?"Следующее":"Завершить",C_BLUE);next.setOnClickListener(x->{if(idx+1<total)renderNativeQuiz(mode,idx+1,true);else renderQuizHub(true);});r.addView(next);markDone(check,"Ответ проверен");});
    }

    private void renderFree(JSONObject q,String mode,int idx,int total){
        LinearLayout qc=card(panel());String question=isMindMode(mode)?q.optString("q"):q.optString("question");qc.addView(text(question,20.5f,ink(),false));EditText ed=new EditText(this);ed.setHint("Введите ответ своими словами");ed.setTextColor(ink());ed.setHintTextColor(muted());ed.setTextSize(sz(15));ed.setMinLines(4);ed.setGravity(Gravity.TOP);ed.setPadding(dp(14),dp(14),dp(14),dp(14));ed.setBackground(surfaceBg(dark?Color.rgb(43,50,46):Color.rgb(250,249,245),dark?Color.rgb(40,47,43):Color.rgb(247,244,237),16,line()));qc.addView(ed,new LinearLayout.LayoutParams(-1,dp(150)));qc.addView(contentActions("",quizQuestionShareText(q,mode),false));Button show=action("Показать эталон и проверить себя",C_SAGE);qc.addView(show);show.setOnClickListener(v->{hideKeyboard(ed);String model=q.optString("model");String key=q.optString("key");LinearLayout r=card(blueSoft());r.addView(kicker("ЭТАЛОН ОТВЕТА",C_BLUE));addParagraphs(r,model,14.8f);if(!key.isEmpty())r.addView(text("Ключ: "+key,13,muted(),false));r.addView(contentActions("",freeResultShareText(q,mode),false));r.addView(text("Оцените себя:",14.5f,ink(),true));LinearLayout row=new LinearLayout(this);Button knew=outline("Знал");Button no=outline("Нужно повторить");row.addView(knew,new LinearLayout.LayoutParams(0,dp(54),1));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(54),1);lp.setMargins(dp(8),0,0,0);row.addView(no,lp);r.addView(row);knew.setOnClickListener(x->{record(mode,q,true);nextFree(mode,idx,total);});no.setOnClickListener(x->{record(mode,q,false);nextFree(mode,idx,total);});markDone(show,"Эталон показан");});
    }

    private void nextFree(String mode,int idx,int total){if(idx+1<total)renderNativeQuiz(mode,idx+1,true);else if(isMindMode(mode))renderMindHub(true);else renderQuizHub(true);}
    private void hideKeyboard(View v){InputMethodManager im=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);if(im!=null)im.hideSoftInputFromWindow(v.getWindowToken(),0);}

    private void bookmarkButton(JSONObject q,String mode,LinearLayout parent){Button b=outline(isBookmarked(mode,q)?"★ В закладках":"☆ В закладки");b.setOnClickListener(v->{toggleBookmark(mode,q);b.setText(isBookmarked(mode,q)?"★ В закладках":"☆ В закладки");});parent.addView(b,new LinearLayout.LayoutParams(-1,dp(50)));}
    private String qid(String mode,JSONObject q){String id=q.optString("num");if(id.isEmpty())id=q.optString("id");if(id.isEmpty())id=String.valueOf(q.optString("question",q.optString("q")).hashCode());return mode+":"+id;}
    private boolean isBookmarked(String m,JSONObject q){return prefs.getStringSet("bookmarks",new HashSet<>()).contains(qid(m,q));}
    private void toggleBookmark(String m,JSONObject q){HashSet<String>s=new HashSet<>(prefs.getStringSet("bookmarks",new HashSet<>()));String id=qid(m,q);if(!s.add(id))s.remove(id);prefs.edit().putStringSet("bookmarks",s).apply();}

    private void record(String mode,JSONObject q,boolean ok){
        String id=qid(mode,q);HashSet<String> answered=new HashSet<>(prefs.getStringSet("answered_ids",new HashSet<>()));HashSet<String> wrong=new HashSet<>(prefs.getStringSet("wrong_ids",new HashSet<>()));boolean fresh=answered.add(id);
        SharedPreferences.Editor e=prefs.edit().putStringSet("answered_ids",answered);
        if(ok)wrong.remove(id);else wrong.add(id);e.putStringSet("wrong_ids",wrong);
        if(fresh){e.putInt("answered_total",prefs.getInt("answered_total",0)+1).putInt("correct_total",prefs.getInt("correct_total",0)+(ok?1:0)).putInt("answered_"+mode,prefs.getInt("answered_"+mode,0)+1).putInt("correct_"+mode,prefs.getInt("correct_"+mode,0)+(ok?1:0));}
        e.apply();
    }

    private void renderRepeatHub(boolean push){
        clear("repeat","",push);currentSection="quiz";appTop();header("Повторение","Ошибки и сохранённые задания остаются на устройстве. Откройте нужный режим — приложение продолжит с сохранённого места.");
        LinearLayout c=card(sandSoft());c.addView(text("На повторение: "+repeatCount(),22,ink(),true));c.addView(text("Закладки викторины: "+prefs.getStringSet("bookmarks",new HashSet<>()).size()+"   •   Материалы: "+materialSavedCount(),15,muted(),false));
        Button classic=outline("Продолжить классическую викторину");classic.setOnClickListener(v->renderNativeQuiz("classic",prefs.getInt("idx_classic",0),true));c.addView(classic,new LinearLayout.LayoutParams(-1,dp(52)));
        Button expert=outline("Открыть экспертный режим");expert.setOnClickListener(v->renderNativeQuiz("expert",prefs.getInt("idx_expert",0),true));c.addView(expert,new LinearLayout.LayoutParams(-1,dp(52)));
        Button hub=action("Все режимы",C_BLUE);hub.setOnClickListener(v->renderQuizHub(true));c.addView(hub);
    }

    private void renderProfile(boolean push){
        clear("profile","",push);currentSection="profile";appTop();header("Профиль знаний","Прогресс хранится локально на устройстве.");
        int a=prefs.getInt("answered_total",0),c=prefs.getInt("correct_total",0),acc=a==0?0:c*100/a;
        LinearLayout stats=new LinearLayout(this);stats.setOrientation(LinearLayout.HORIZONTAL);stats.addView(statCard(acc+"%","Точность"),new LinearLayout.LayoutParams(0,dp(100),1));LinearLayout.LayoutParams p2=new LinearLayout.LayoutParams(0,dp(100),1);p2.setMargins(dp(8),0,0,0);stats.addView(statCard(String.valueOf(a),"Пройдено"),p2);LinearLayout.LayoutParams p3=new LinearLayout.LayoutParams(0,dp(100),1);p3.setMargins(dp(8),0,0,0);stats.addView(statCard(String.valueOf(repeatCount()),"Повторить"),p3);page.addView(stats);
        LinearLayout ccard=card(panel());ccard.addView(text("По режимам",20,ink(),true));for(String m:new String[]{"classic","multi","match","hadith","free","expert","mind_quick","mind_exam"}){int ma=prefs.getInt("answered_"+m,0),k=prefs.getInt("correct_"+m,0);LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.addView(text(modeTitle(m),14,ink(),false),new LinearLayout.LayoutParams(0,-2,1));row.addView(text(ma+" · "+(ma==0?0:k*100/ma)+"%",13,muted(),true));ccard.addView(row);}
        LinearLayout saved=card(sageSoft());saved.addView(text("Сохранённое",19,ink(),true));saved.addView(text("Закладки викторины: "+prefs.getStringSet("bookmarks",new HashSet<>()).size(),14,muted(),false));saved.addView(text("Сохранённые материалы: "+materialSavedCount()+"   •   Уроков открыто: "+seenMindCount()+" из 8",14,muted(),false));
        Button s=outline("Настройки");s.setOnClickListener(v->renderSettings(true));saved.addView(s,new LinearLayout.LayoutParams(-1,dp(52)));
        Button reset=outline("Сбросить прогресс");reset.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Сбросить прогресс?").setMessage("Закладки, статистика и история изучения будут удалены.").setNegativeButton("Отмена",null).setPositiveButton("Сбросить",(d,w)->{String fm=fontMode;float fs=fontScale;boolean dk=dark;prefs.edit().clear().putString("fontMode",fm).putFloat("fontScale",fs).putBoolean("dark",dk).apply();history.clear();renderProfile(false);}).show());saved.addView(reset,new LinearLayout.LayoutParams(-1,dp(52)));
    }

    private void renderSettings(boolean push){
        clear("settings","",push);currentSection="settings";appTop();header("Настройки","Изменения применяются сразу в этом окне.");
        LinearLayout f=card(panel());f.addView(text("Размер текста",18,ink(),true));LinearLayout row=new LinearLayout(this);
        float[] zs={.9f,1f,1.15f,1.28f};String[] zn={"S","M","L","XL"};for(int i=0;i<zs.length;i++){float z=zs[i];Button b=outline(zn[i]);if(Math.abs(fontScale-z)<.02f)b.setBackground(surfaceBg(sageSoft(),sageSoft(),16,C_SAGE));b.setOnClickListener(v->{fontScale=z;prefs.edit().putFloat("fontScale",z).apply();renderSettings(false);});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(52),1);if(i>0)lp.setMargins(dp(6),0,0,0);row.addView(b,lp);}f.addView(row);
        LinearLayout font=card(blueSoft());font.addView(text("Стиль шрифта",18,ink(),true));String[][] modes={{"modern","Современный"},{"classic","Классический"},{"compact","Компактный"}};for(String[] m:modes){Button b=outline((fontMode.equals(m[0])?"✓  ":"")+m[1]);b.setOnClickListener(v->{fontMode=m[0];prefs.edit().putString("fontMode",fontMode).apply();renderSettings(false);});font.addView(b,new LinearLayout.LayoutParams(-1,dp(50)));}
        LinearLayout t=card(sageSoft());t.addView(text("Оформление",18,ink(),true));t.addView(text(dark?"Сейчас включена тёмная тема":"Сейчас включена светлая матовая тема",14,muted(),false));Button theme=action(dark?"Переключить на светлую":"Переключить на тёмную",dark?C_BLUE:C_SAGE);theme.setOnClickListener(v->{dark=!dark;prefs.edit().putBoolean("dark",dark).apply();buildShell();renderSettings(false);});t.addView(theme);
        LinearLayout preview=card(panel());preview.addView(kicker("ПРЕДПРОСМОТР",C_BLUE));preview.addView(text("Аль-Фатиха — смысл, который должен дойти до сердца.",17,ink(),true));preview.addView(text("Размер, стиль и тема применяются сразу ко всему нативному приложению.",14,muted(),false));
    }

    private void renderMenu(boolean push){
        clear("menu","",push);currentSection="menu";appTop();header("Меню","");menuBtn("Учиться","Осознанное чтение Аль-Фатихи",C_SAGE,()->renderMindHub(true));menuBtn("Повторение","Ошибки, закладки и продолжение режимов",Color.rgb(145,104,42),()->renderRepeatHub(true));menuBtn("Экзамен","Экзамен по осознанному чтению",C_BLUE,()->renderNativeQuiz("mind_exam",0,true));menuBtn("Профиль","Статистика и настройки",Color.rgb(112,96,134),()->renderProfile(true));
    }

    private void menuBtn(String title,String sub,int color,Runnable r){
        LinearLayout c=card(panel());LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);LinearLayout tx=new LinearLayout(this);tx.setOrientation(LinearLayout.VERTICAL);tx.addView(text(title,19,ink(),true));tx.addView(text(sub,13.5f,muted(),false));row.addView(tx,new LinearLayout.LayoutParams(0,-2,1));TextView a=arrowChip();row.addView(a,new LinearLayout.LayoutParams(dp(48),dp(48)));c.addView(row);c.setOnClickListener(v->r.run());
    }

    private void openSection(){if(currentSection.equals("mind"))renderMindHub(true);else if(currentSection.equals("quiz"))renderQuizHub(true);else if(currentSection.equals("profile")||currentSection.equals("settings"))renderProfile(true);else renderHome(true);}
    private void goBack(){if(!history.isEmpty()){Screen s=history.pop();restore(s);}else renderHome(false);}

    private void restore(Screen s){switch(s.type){case"home":renderHome(false);break;case"mindHub":renderMindHub(false);break;case"intro":renderIntro(false);break;case"mindLesson":renderMindLesson(Integer.parseInt(s.arg),false);break;case"mindSlow":renderMindSlow(Integer.parseInt(s.arg),false);break;case"mindFocus":renderMindFocus(Integer.parseInt(s.arg),false);break;case"mindStages":String[]m=s.arg.split(":");renderMindStages(Integer.parseInt(m[0]),Integer.parseInt(m[1]),false);break;case"quizHub":renderQuizHub(false);break;case"quiz":String[]q=s.arg.split(":");renderNativeQuiz(q[0],Integer.parseInt(q[1]),false);break;case"repeat":renderRepeatHub(false);break;case"profile":renderProfile(false);break;case"settings":renderSettings(false);break;case"menu":renderMenu(false);break;default:renderHome(false);}}

    @Override public void onBackPressed(){goBack();}

    private JSONArray arr(String name){
        if(cache.containsKey(name))return cache.get(name);
        try(InputStream in=getAssets().open(name);ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[]buf=new byte[8192];int n;while((n=in.read(buf))>0)out.write(buf,0,n);JSONArray a=new JSONArray(out.toString("UTF-8"));cache.put(name,a);return a;
        }catch(Exception e){e.printStackTrace();return new JSONArray();}
    }

    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
