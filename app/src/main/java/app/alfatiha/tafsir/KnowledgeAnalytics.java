package app.alfatiha.tafsir;

import android.content.SharedPreferences;
import java.util.*;

/**
 * Single source of truth for knowledge statistics, spaced repetition and exam history.
 * Old answer sets remain authoritative; the new keys only add information that did not
 * exist in earlier versions (dates, repetitions and attempt history).
 */
final class KnowledgeAnalytics {
    static final int MIN_AREA_SAMPLE=5;
    static final long DAY_MS=86_400_000L;
    static final int[] REVIEW_DAYS={1,3,7,14,30};

    static final String AREA_TAFSIR="tafsir";
    static final String AREA_HADITH="hadith";
    static final String AREA_ARABIC="arabic";
    static final String AREA_METHOD="methodology";
    static final String AREA_MEANING="meaning";
    static final String AREA_CONNECTIONS="connections";
    static final String AREA_HEART="heart";
    static final String AREA_PRACTICE="practice";
    static final String AREA_ERRORS="errors";

    static final class QuestionRef {
        final String canonicalId;
        final ArrayList<String> stateIds=new ArrayList<>();
        String mode;
        int index;
        String title;
        String area;
        String errorType;

        QuestionRef(String canonicalId,String stateId,String mode,int index,
                    String title,String area,String errorType){
            this.canonicalId=canonicalId;
            this.mode=mode;
            this.index=index;
            this.title=title==null?"":title;
            this.area=area==null?AREA_MEANING:area;
            this.errorType=errorType==null?"":errorType;
            stateIds.add(stateId);
        }
    }

    static final class Catalog {
        private final LinkedHashMap<String,QuestionRef> byCanonical=new LinkedHashMap<>();
        private final HashMap<String,QuestionRef> byState=new HashMap<>();

        void add(String canonicalId,String stateId,String mode,int index,
                 String title,String area,String errorType){
            QuestionRef q=byCanonical.get(canonicalId);
            if(q==null){
                q=new QuestionRef(canonicalId,stateId,mode,index,title,area,errorType);
                byCanonical.put(canonicalId,q);
            }else if(!q.stateIds.contains(stateId)){
                q.stateIds.add(stateId);
            }
            byState.put(stateId,q);
        }

        List<QuestionRef> all(){return new ArrayList<>(byCanonical.values());}
        QuestionRef byCanonical(String id){return byCanonical.get(id);}
        QuestionRef byState(String id){return byState.get(id);}
        int size(){return byCanonical.size();}
    }

    static final class AreaStats {
        final String key;
        final String title;
        final String description;
        int total;
        int answered;
        int correct;
        int wrong;
        int corrected;

        AreaStats(String key){
            this.key=key;
            this.title=areaTitle(key);
            this.description=areaDescription(key);
        }

        int accuracy(){return answered==0?0:clamp(correct*100/answered);}
        int coverage(){return total==0?0:clamp(answered*100/total);}
        boolean enoughData(){return answered>=MIN_AREA_SAMPLE;}
    }

    static final class Summary {
        int total;
        int answered;
        int correct;
        int wrong;
        int corrected;
        int due;
        int accuracy;
        int coverage;
        int rating;
        int examAverage;
        int examCount;
        String level;
        String nextLevel;
        String nextLevelHint;
        AreaStats weakArea;
    }

    static final class AttemptResult {
        final String mode;
        final int correct;
        final int total;
        final int percent;
        final int firstPercent;
        final int previousPercent;
        final boolean repeat;

        AttemptResult(String mode,int correct,int total,int firstPercent,
                      int previousPercent,boolean repeat){
            this.mode=mode;
            this.total=Math.max(0,total);
            this.correct=Math.max(0,Math.min(correct,this.total));
            this.percent=this.total==0?0:clamp(this.correct*100/this.total);
            this.firstPercent=clamp(firstPercent);
            this.previousPercent=previousPercent<0?-1:clamp(previousPercent);
            this.repeat=repeat;
        }
    }

    static final class ExamRecord {
        final long time;
        final String type;
        final int correct;
        final int total;
        final int percent;
        final ArrayList<String> weakAreas;

        ExamRecord(long time,String type,int correct,int total,List<String> weakAreas){
            this.time=Math.max(0,time);
            this.type=type;
            this.total=Math.max(0,total);
            this.correct=Math.max(0,Math.min(correct,this.total));
            this.percent=this.total==0?0:clamp(this.correct*100/this.total);
            this.weakAreas=new ArrayList<>(weakAreas==null?Collections.emptyList():weakAreas);
        }
    }

    private final SharedPreferences prefs;
    private final Catalog catalog;
    private final Set<String> answeredIds;
    private final Set<String> wrongIds;
    private final Set<String> correctedIds;
    private final Set<String> bookmarks;

    KnowledgeAnalytics(SharedPreferences prefs,Catalog catalog){
        this.prefs=prefs;
        this.catalog=catalog;
        answeredIds=stringSet("answered_ids");
        wrongIds=stringSet("wrong_ids");
        correctedIds=stringSet("corrected_ids");
        bookmarks=stringSet("bookmarks");
    }

    Catalog catalog(){return catalog;}

    private Set<String> stringSet(String key){
        return new HashSet<>(prefs.getStringSet(key,Collections.emptySet()));
    }

    private static boolean any(Set<String> values,List<String> aliases){
        for(String id:aliases)if(values.contains(id))return true;
        return false;
    }

    boolean isAnswered(QuestionRef q){
        return prefs.contains("ka_last_result:"+q.canonicalId)
                || any(answeredIds,q.stateIds);
    }

    boolean isWrong(QuestionRef q){
        String key="ka_last_result:"+q.canonicalId;
        if(prefs.contains(key))return !prefs.getBoolean(key,false);
        return any(wrongIds,q.stateIds);
    }

    boolean isCorrected(QuestionRef q){
        if(isWrong(q))return false;
        return prefs.getBoolean("ka_ever_wrong:"+q.canonicalId,false)
                || any(correctedIds,q.stateIds);
    }

    boolean isBookmarked(QuestionRef q){
        return any(bookmarks,q.stateIds);
    }

    int errorCount(QuestionRef q){
        int count=Math.max(0,prefs.getInt("ka_error_count:"+q.canonicalId,0));
        if(count==0&&(isWrong(q)||isCorrected(q)))count=1;
        return count;
    }

    long today(){
        long now=System.currentTimeMillis();
        return (now+TimeZone.getDefault().getOffset(now))/DAY_MS;
    }

    boolean isDue(QuestionRef q){
        String key="review_due_day:"+q.canonicalId;
        return prefs.contains(key)&&prefs.getLong(key,Long.MAX_VALUE)<=today();
    }

    boolean isStale(QuestionRef q){
        long last=prefs.getLong("review_last_day:"+q.canonicalId,-1);
        return last>=0&&today()-last>=30;
    }

    int reviewStage(QuestionRef q){
        return Math.max(0,Math.min(REVIEW_DAYS.length-1,
                prefs.getInt("review_stage:"+q.canonicalId,0)));
    }

    int daysUntilReview(QuestionRef q){
        String key="review_due_day:"+q.canonicalId;
        if(!prefs.contains(key))return -1;
        long delta=prefs.getLong(key,today())-today();
        if(delta>Integer.MAX_VALUE)return Integer.MAX_VALUE;
        return (int)Math.max(0,delta);
    }

    void ensureLegacyErrorsScheduled(){
        SharedPreferences.Editor e=prefs.edit();
        boolean changed=false;
        long now=today();
        for(QuestionRef q:catalog.all()){
            if(isWrong(q)&&!prefs.contains("review_due_day:"+q.canonicalId)){
                e.putInt("review_stage:"+q.canonicalId,0);
                e.putLong("review_due_day:"+q.canonicalId,now);
                e.putBoolean("review_last_result:"+q.canonicalId,false);
                changed=true;
            }
        }
        if(changed)e.apply();
    }

    void recordAnswer(QuestionRef q,boolean correct,boolean previouslyWrong){
        if(q==null)return;
        String id=q.canonicalId;
        long now=today();
        String stageKey="review_stage:"+id;
        int stage=prefs.getInt(stageKey,0);
        boolean hadSchedule=prefs.contains("review_due_day:"+id);
        boolean previousResult=prefs.getBoolean("review_last_result:"+id,false);

        if(correct){
            if(!hadSchedule||!previousResult)stage=0;
            else stage=Math.min(REVIEW_DAYS.length-1,stage+1);
        }else{
            stage=0;
        }

        int priorErrors=Math.max(0,prefs.getInt("ka_error_count:"+id,0));
        int errors=correct||priorErrors==Integer.MAX_VALUE?priorErrors:priorErrors+1;
        SharedPreferences.Editor e=prefs.edit()
                .putBoolean("ka_last_result:"+id,correct)
                .putLong("review_last_day:"+id,now)
                .putBoolean("review_last_result:"+id,correct)
                .putInt(stageKey,stage)
                .putLong("review_due_day:"+id,now+REVIEW_DAYS[stage]);
        if(!correct||previouslyWrong){
            e.putBoolean("ka_ever_wrong:"+id,true)
             .putInt("ka_error_count:"+id,Math.max(errors,previouslyWrong?(correct?1:2):0));
        }
        String recent=prefs.getString("ka_recent_results","")+(correct?"1":"0");
        if(recent.length()>60)recent=recent.substring(recent.length()-60);
        e.putString("ka_recent_results",recent).apply();
    }

    LinkedHashMap<String,AreaStats> areaStats(){
        LinkedHashMap<String,AreaStats> result=new LinkedHashMap<>();
        for(String key:areaOrder())result.put(key,new AreaStats(key));
        for(QuestionRef q:catalog.all()){
            AreaStats a=result.get(q.area);
            if(a==null){a=new AreaStats(q.area);result.put(q.area,a);}
            a.total++;
            if(isAnswered(q)){
                a.answered++;
                if(isWrong(q))a.wrong++;else a.correct++;
                if(isCorrected(q))a.corrected++;
            }
        }
        result.entrySet().removeIf(x->x.getValue().total==0);
        return result;
    }

    AreaStats weakArea(){
        AreaStats weak=null;
        double worst=14.99;
        for(AreaStats a:areaStats().values()){
            if(!a.enoughData())continue;
            // One isolated miss is not enough to label a whole area as weak.
            if(!hasWeakEvidence(a))continue;
            double reliability=Math.min(1d,a.answered/10d);
            double risk=(100-a.accuracy())*(.60+.40*reliability)
                    +Math.min(15,a.wrong*3);
            if(risk>worst){worst=risk;weak=a;}
        }
        return weak;
    }

    private static boolean hasWeakEvidence(AreaStats a){
        return a.wrong>=2
                ||(a.answered>=8&&a.accuracy()<75)
                ||(a.answered>=12&&a.accuracy()<85);
    }

    Summary summary(){
        ensureLegacyErrorsScheduled();
        Summary s=new Summary();
        s.total=catalog.size();
        for(QuestionRef q:catalog.all()){
            if(isAnswered(q)){
                s.answered++;
                if(isWrong(q))s.wrong++;else s.correct++;
                if(isCorrected(q))s.corrected++;
            }
            if(isDue(q))s.due++;
        }
        s.accuracy=s.answered==0?0:clamp(s.correct*100/s.answered);
        s.coverage=s.total==0?0:clamp(s.answered*100/s.total);
        List<ExamRecord> history=examHistory();
        int legacy=legacyExamPercent();
        s.examCount=history.isEmpty()&&legacy>=0?1:history.size();
        if(!history.isEmpty()){
            int count=Math.min(5,history.size()),sum=0;
            for(int i=0;i<count;i++)sum+=history.get(i).percent;
            s.examAverage=sum/count;
        }else{
            if(legacy>=0)s.examAverage=legacy;
        }
        double confidence=Math.min(1d,s.answered/50d);
        int errorBase=s.wrong+s.corrected;
        int resolution=errorBase==0?s.coverage:clamp(s.corrected*100/errorBase);
        double score=s.accuracy*.45*confidence+s.coverage*.35
                +resolution*.10*confidence+(s.examCount>0?s.examAverage*.10:0);
        s.rating=clamp((int)Math.round(score));
        assignLevel(s);
        s.weakArea=weakArea();
        return s;
    }

    private int legacyExamPercent(){
        int total=0,correct=0;
        for(QuestionRef q:catalog.all()){
            String examId=null;
            for(String id:q.stateIds)if(id.startsWith("mind_exam:")){examId=id;break;}
            if(examId==null)continue;
            total++;
            // Migration evidence must come from the old exam itself, not from an
            // identical question answered in the shorter understanding check.
            if(!answeredIds.contains(examId))return -1;
            if(!wrongIds.contains(examId))correct++;
        }
        return total==0?-1:clamp(correct*100/total);
    }

    private static void assignLevel(Summary s){
        if(s.answered>=180&&s.rating>=72&&s.accuracy>=80&&s.examCount>=2){
            s.level="Продвинутый";
            s.nextLevel="Максимальный уровень";
            s.nextLevelHint="Поддерживайте результат интервальным повторением и новыми попытками экзамена.";
        }else if(s.answered>=80&&s.rating>=45&&s.examCount>=1){
            s.level="Уверенный";
            s.nextLevel="Продвинутый";
            s.nextLevelHint="Нужно проверить не менее 180 разных заданий, сохранить точность от 80% и пройти минимум два экзамена.";
        }else if(s.answered>=20&&s.rating>=15){
            s.level="Изучающий";
            s.nextLevel="Уверенный";
            s.nextLevelHint="Нужно расширить охват минимум до 80 заданий, закрепить ошибки и пройти итоговый экзамен.";
        }else{
            s.level="Новичок";
            s.nextLevel="Изучающий";
            s.nextLevelHint="Нужно проверить не менее 20 разных заданий: рейтинг не растёт только от нескольких удачных ответов.";
        }
    }

    int reviewNowCount(){
        int n=0;
        for(QuestionRef q:catalog.all())if(isWrong(q)||isDue(q))n++;
        return n;
    }

    List<QuestionRef> dueQuestions(){
        ArrayList<QuestionRef> out=new ArrayList<>();
        for(QuestionRef q:catalog.all())if(isDue(q))out.add(q);
        out.sort((a,b)->{
            long da=prefs.getLong("review_due_day:"+a.canonicalId,Long.MAX_VALUE);
            long db=prefs.getLong("review_due_day:"+b.canonicalId,Long.MAX_VALUE);
            int c=Long.compare(da,db);return c!=0?c:a.canonicalId.compareTo(b.canonicalId);
        });
        return out;
    }

    List<QuestionRef> errorQuestions(){
        ArrayList<QuestionRef> out=new ArrayList<>();
        for(QuestionRef q:catalog.all())if(isWrong(q))out.add(q);
        out.sort((a,b)->{
            int c=Integer.compare(errorCount(b),errorCount(a));
            return c!=0?c:a.canonicalId.compareTo(b.canonicalId);
        });
        return out;
    }

    List<QuestionRef> questionsForArea(String area,boolean errorsOnly){
        ArrayList<QuestionRef> out=new ArrayList<>();
        for(QuestionRef q:catalog.all())if(area.equals(q.area)&&(!errorsOnly||isWrong(q)))out.add(q);
        out.sort((a,b)->{
            int sa=(isWrong(a)?0:isAnswered(a)?2:1),sb=(isWrong(b)?0:isAnswered(b)?2:1);
            int c=Integer.compare(sa,sb);return c!=0?c:a.canonicalId.compareTo(b.canonicalId);
        });
        return out;
    }

    QuestionRef firstToContinue(String area){
        List<QuestionRef> list=questionsForArea(area,false);
        for(QuestionRef q:list)if(!isAnswered(q))return q;
        return list.isEmpty()?null:list.get(0);
    }

    List<QuestionRef> adaptiveQuestions(int limit){
        final AreaStats weak=weakArea();
        final Map<String,Integer> typeCounts=errorTypeCounts();
        final Map<String,AreaStats> areas=areaStats();
        ArrayList<QuestionRef> out=new ArrayList<>(catalog.all());
        out.sort((a,b)->{
            int sa=adaptiveScore(a,weak,typeCounts,areas),sb=adaptiveScore(b,weak,typeCounts,areas);
            int c=Integer.compare(sb,sa);return c!=0?c:a.canonicalId.compareTo(b.canonicalId);
        });
        if(out.size()>limit)return new ArrayList<>(out.subList(0,limit));
        return out;
    }

    private int adaptiveScore(QuestionRef q,AreaStats weak,Map<String,Integer> typeCounts,
                              Map<String,AreaStats> areas){
        int score=0;
        if(isWrong(q))score+=100;
        if(isDue(q))score+=70;
        if(isStale(q))score+=35;
        if(weak!=null&&weak.key.equals(q.area))score+=45;
        int errors=q.errorType.isEmpty()?0:typeCounts.getOrDefault(q.errorType,0);
        if(errors>=2)score+=errors>=8?30:errors*4;
        if(!isAnswered(q))score+=20;
        AreaStats own=areas.get(q.area);
        if(own!=null&&own.enoughData()&&own.accuracy()>=85&&!isWrong(q))score-=25;
        return score;
    }

    List<QuestionRef> weakExamQuestions(int limit){
        LinkedHashMap<String,AreaStats> areas=areaStats();
        HashSet<String> weakKeys=new HashSet<>();
        for(AreaStats a:areas.values())if(a.enoughData()&&hasWeakEvidence(a))weakKeys.add(a.key);
        if(weakKeys.isEmpty())return Collections.emptyList();
        ArrayList<QuestionRef> candidates=new ArrayList<>();
        for(QuestionRef q:catalog.all())if(weakKeys.contains(q.area))candidates.add(q);
        candidates.sort((a,b)->{
            int sa=(isWrong(a)?100:0)+(!isAnswered(a)?40:0)+errorCount(a)*5;
            int sb=(isWrong(b)?100:0)+(!isAnswered(b)?40:0)+errorCount(b)*5;
            int c=Integer.compare(sb,sa);return c!=0?c:a.canonicalId.compareTo(b.canonicalId);
        });
        if(candidates.size()>limit)return new ArrayList<>(candidates.subList(0,limit));
        return candidates;
    }

    List<QuestionRef> correctedExamQuestions(int limit){
        ArrayList<QuestionRef> out=new ArrayList<>();
        for(QuestionRef q:catalog.all())if(isCorrected(q)&&!isWrong(q))out.add(q);
        out.sort((a,b)->{
            int c=Integer.compare(errorCount(b),errorCount(a));
            return c!=0?c:a.canonicalId.compareTo(b.canonicalId);
        });
        if(out.size()>limit)return new ArrayList<>(out.subList(0,limit));
        return out;
    }

    String reason(QuestionRef q,String queue){
        if("errors".equals(queue)||isWrong(q))return "Допущена ошибка";
        if("due".equals(queue)||isDue(q))return "Подошёл срок интервального повторения";
        AreaStats weak=weakArea();
        if(weak!=null&&weak.key.equals(q.area))return "Слабая тема: "+weak.title;
        if(isStale(q))return "Материал давно не проверялся";
        if(!isAnswered(q))return "Материал ещё не проверялся";
        return "Закрепление освоенной темы";
    }

    Map<String,Integer> errorTypeCounts(){
        LinkedHashMap<String,Integer> out=new LinkedHashMap<>();
        for(QuestionRef q:catalog.all()){
            if(q.errorType.isEmpty())continue;
            int count=errorCount(q);
            if(count>0){int old=out.getOrDefault(q.errorType,0);out.put(q.errorType,count>Integer.MAX_VALUE-old?Integer.MAX_VALUE:old+count);}
        }
        return out;
    }

    void trackAttempt(String mode,QuestionRef q,boolean correct){
        if(q==null)return;
        String idsKey="attempt_ids:"+mode,correctKey="attempt_correct:"+mode;
        HashSet<String> ids=new HashSet<>(prefs.getStringSet(idsKey,Collections.emptySet()));
        HashSet<String> good=new HashSet<>(prefs.getStringSet(correctKey,Collections.emptySet()));
        ids.add(q.canonicalId);
        if(correct)good.add(q.canonicalId);else good.remove(q.canonicalId);
        SharedPreferences.Editor e=prefs.edit().putStringSet(idsKey,ids).putStringSet(correctKey,good);
        if(!prefs.contains("attempt_started:"+mode))e.putLong("attempt_started:"+mode,System.currentTimeMillis());
        e.apply();
    }

    AttemptResult finishAttempt(String mode){
        String idsKey="attempt_ids:"+mode,correctKey="attempt_correct:"+mode;
        Set<String> ids=stringSet(idsKey),good=stringSet(correctKey);
        int correct=0;for(String id:ids)if(good.contains(id))correct++;
        int total=ids.size(),pct=total==0?0:clamp(correct*100/total);
        String firstKey="first_attempt_percent:"+mode,lastKey="last_attempt_percent:"+mode;
        boolean repeat=prefs.contains(firstKey);
        int first=repeat?prefs.getInt(firstKey,pct):pct;
        int previous=prefs.contains(lastKey)?prefs.getInt(lastKey,-1):-1;
        SharedPreferences.Editor e=prefs.edit().putInt(lastKey,pct)
                .remove(idsKey).remove(correctKey).remove("attempt_started:"+mode);
        if(!repeat)e.putInt(firstKey,pct);
        e.apply();
        return new AttemptResult(mode,correct,total,first,previous,repeat);
    }

    void saveExam(String type,int correct,int total,List<String> weakAreas){
        HashSet<String> raw=new HashSet<>(prefs.getStringSet("exam_history_v1",Collections.emptySet()));
        ExamRecord record=new ExamRecord(System.currentTimeMillis(),type,correct,total,weakAreas);
        raw.add(encodeExam(record));
        ArrayList<ExamRecord> parsed=parseExams(raw);
        while(parsed.size()>30)parsed.remove(parsed.size()-1);
        HashSet<String> trimmed=new HashSet<>();for(ExamRecord x:parsed)trimmed.add(encodeExam(x));
        prefs.edit().putStringSet("exam_history_v1",trimmed)
                .putInt("last_exam_percent",record.percent).apply();
    }

    List<ExamRecord> examHistory(){
        return parseExams(prefs.getStringSet("exam_history_v1",Collections.emptySet()));
    }

    int lastExamPercent(){
        List<ExamRecord> h=examHistory();
        if(!h.isEmpty())return h.get(0).percent;
        return legacyExamPercent();
    }

    private static String encodeExam(ExamRecord r){
        StringBuilder weak=new StringBuilder();
        for(String x:r.weakAreas){if(weak.length()>0)weak.append(',');weak.append(x);}
        return r.time+"|"+r.type+"|"+r.correct+"|"+r.total+"|"+weak;
    }

    private static ArrayList<ExamRecord> parseExams(Collection<String> raw){
        ArrayList<ExamRecord> out=new ArrayList<>();
        for(String value:raw){
            try{
                String[] p=value.split("\\|",-1);
                if(p.length<4)continue;
                ArrayList<String> weak=new ArrayList<>();
                if(p.length>4&&!p[4].isEmpty())weak.addAll(Arrays.asList(p[4].split(",")));
                out.add(new ExamRecord(Long.parseLong(p[0]),p[1],Integer.parseInt(p[2]),Integer.parseInt(p[3]),weak));
            }catch(Exception ignored){}
        }
        out.sort((a,b)->Long.compare(b.time,a.time));
        return out;
    }

    int recentResultCount(){return prefs.getString("ka_recent_results","").length();}

    Integer recentImprovement(){
        String s=prefs.getString("ka_recent_results","");
        if(s.length()<20)return null;
        String last=s.substring(s.length()-20);
        int first=0,second=0;
        for(int i=0;i<10;i++)if(last.charAt(i)=='1')first++;
        for(int i=10;i<20;i++)if(last.charAt(i)=='1')second++;
        return (second-first)*10;
    }

    static String areaTitle(String key){
        switch(key){
            case AREA_TAFSIR:return "Тафсир";
            case AREA_HADITH:return "Хадисы и Сунна";
            case AREA_ARABIC:return "Арабский язык";
            case AREA_METHOD:return "Методология тафсира";
            case AREA_CONNECTIONS:return "Связи аятов";
            case AREA_HEART:return "Состояние сердца";
            case AREA_PRACTICE:return "Практическое применение";
            case AREA_ERRORS:return "Распознавание ошибок";
            default:return "Смыслы Аль-Фатихи";
        }
    }

    static String areaDescription(String key){
        switch(key){
            case AREA_TAFSIR:return "Переданные толкования, названия суры и смысл аятов.";
            case AREA_HADITH:return "Достоверные хадисы, молитва, «Амин» и применение Сунны.";
            case AREA_ARABIC:return "Термины, лексические оттенки, чтения и риторика.";
            case AREA_METHOD:return "Границы доказательства и корректная работа с толкованиями.";
            case AREA_CONNECTIONS:return "Порядок смыслов и внутренняя архитектура суры.";
            case AREA_HEART:return "Присутствие сердца, надежда, страх, искренность и нужда.";
            case AREA_PRACTICE:return "Применение смыслов в намазе и жизненных ситуациях.";
            case AREA_ERRORS:return "Тонкие искажения смысла и практические ошибки чтения.";
            default:return "Хвала, поклонение, помощь, наставление и прямой путь.";
        }
    }

    static String errorTypeTitle(String key){
        switch(key){
            case "close_meanings":return "Смешение близких смыслов";
            case "positions":return "Недостаточное различение позиций";
            case "evidence_strength":return "Завышение силы довода";
            case "terminology":return "Ошибка в терминологии";
            case "verse_links":return "Ошибка в связи аятов";
            case "heart_state":return "Ошибка в состоянии сердца";
            case "practice":return "Неправильное практическое применение";
            case "without_knowledge":return "Действие без достаточного знания";
            case "surface_memory":return "Запоминание без понимания";
            default:return key;
        }
    }

    static List<String> areaOrder(){
        return Arrays.asList(AREA_TAFSIR,AREA_HADITH,AREA_ARABIC,AREA_METHOD,
                AREA_MEANING,AREA_CONNECTIONS,AREA_HEART,AREA_PRACTICE,AREA_ERRORS);
    }

    private static int clamp(int value){return Math.max(0,Math.min(100,value));}
}
