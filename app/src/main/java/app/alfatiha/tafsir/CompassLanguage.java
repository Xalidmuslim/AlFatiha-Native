package app.alfatiha.tafsir;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Reviewed UI navigation vocabulary only.
 *
 * NEVER pass Quranic verses, hadith narrations, quiz answers or tafsir excerpts
 * through an automatic translator. Scholarly Arabic texts must come from
 * separately sourced, reviewable Arabic content bundles with preserved IDs.
 */
public final class CompassLanguage {
    public static final String PREF_NAME = "alfatiha_native";
    public static final String PREF_KEY = "app_language";
    public static final String RU = "ru";
    public static final String AR = "ar";
    private static final Map<String, String> UI = new HashMap<>();

    static {
        UI.put("Компас сердца", "بوصلة القلب");
        UI.put("Главная", "الرئيسية");
        UI.put("Содержание", "الفهرس");
        UI.put("Прогресс", "التقدّم");
        UI.put("Меню", "القائمة");
        UI.put("Настройки", "الإعدادات");
        UI.put("Язык приложения", "لغة التطبيق");
        UI.put("Русский", "الروسية");
        UI.put("Арабский", "العربية");
        UI.put("Выберите язык интерфейса", "اختر لغة الواجهة");
        UI.put("Аль-Фатиха", "الفاتحة");
        UI.put("Медицина Пророка ﷺ", "الطب النبوي ﷺ");
        UI.put("Медицина Пророка", "الطب النبوي");
        UI.put("Азкары", "الأذكار");
        UI.put("Хадисы-кудси", "الأحاديث القدسية");
        UI.put("Хадисы", "الأحاديث");
        UI.put("Хадис", "الحديث");
        UI.put("Тайны молитвы", "أسرار الصلاة");
        UI.put("Малый ширк", "الشرك الأصغر");
        UI.put("Защита единобожия", "حماية التوحيد");
        UI.put("Единобожие", "التوحيد");
        UI.put("Викторины", "الاختبارات");
        UI.put("Викторина", "اختبار");
        UI.put("Уроки", "الدروس");
        UI.put("Урок", "الدرس");
        UI.put("Чтение", "القراءة");
        UI.put("Книга", "الكتاب");
        UI.put("Материалы", "المواد");
        UI.put("Разделы", "الأقسام");
        UI.put("Раздел", "القسم");
        UI.put("Поиск", "البحث");
        UI.put("Поиск по приложению", "البحث في التطبيق");
        UI.put("Закладки", "العلامات المرجعية");
        UI.put("Закладка", "علامة مرجعية");
        UI.put("Заметки", "الملاحظات");
        UI.put("Заметка", "ملاحظة");
        UI.put("История", "السجل");
        UI.put("Обучение", "التعلّم");
        UI.put("Начать урок", "ابدأ الدرس");
        UI.put("Продолжить", "متابعة");
        UI.put("Продолжить обучение", "متابعة التعلّم");
        UI.put("Продолжить чтение", "متابعة القراءة");
        UI.put("Начать", "ابدأ");
        UI.put("Назад", "رجوع");
        UI.put("Далее", "التالي");
        UI.put("Следующая", "التالي");
        UI.put("Предыдущая", "السابق");
        UI.put("Сохранить", "حفظ");
        UI.put("Сохранено", "تم الحفظ");
        UI.put("Удалить", "حذف");
        UI.put("Скопировать", "نسخ");
        UI.put("Скопировано", "تم النسخ");
        UI.put("Поделиться", "مشاركة");
        UI.put("Отмена", "إلغاء");
        UI.put("Закрыть", "إغلاق");
        UI.put("Открыть", "فتح");
        UI.put("Готово", "تم");
        UI.put("Проверить", "تحقّق");
        UI.put("Ответить", "أجب");
        UI.put("Правильный ответ", "الإجابة الصحيحة");
        UI.put("Разбор", "الشرح");
        UI.put("Объяснение", "التفسير");
        UI.put("Объяснения", "الشروح");
        UI.put("Источник", "المصدر");
        UI.put("Источники", "المصادر");
        UI.put("Терминология", "المصطلحات");
        UI.put("Глоссарий", "معجم المصطلحات");
        UI.put("Тема", "الموضوع");
        UI.put("Темы", "المواضيع");
        UI.put("Тест", "اختبار");
        UI.put("Повторение", "المراجعة");
        UI.put("Экзамен", "الامتحان");
        UI.put("Экзамены", "الامتحانات");
        UI.put("Общий профиль", "الملف العام");
        UI.put("Профиль", "الملف الشخصي");
        UI.put("Подробная аналитика", "تحليل مفصّل");
        UI.put("Результаты", "النتائج");
        UI.put("Результат", "النتيجة");
        UI.put("Ошибки", "الأخطاء");
        UI.put("Повторить ошибки", "مراجعة الأخطاء");
        UI.put("Повторить", "إعادة");
        UI.put("Вопросы", "الأسئلة");
        UI.put("Вопрос", "السؤال");
        UI.put("Ответ", "الإجابة");
        UI.put("Проверено", "تم التقييم");
        UI.put("Рейтинг знаний", "تقييم المعرفة");
        UI.put("Прогресс обучения", "تقدّم التعلّم");
        UI.put("Данные и настройки", "البيانات والإعدادات");
        UI.put("Размер текста", "حجم النص");
        UI.put("Стиль шрифта", "نمط الخط");
        UI.put("Современный", "حديث");
        UI.put("Классический", "تقليدي");
        UI.put("Компактный", "مضغوط");
        UI.put("Оформление", "المظهر");
        UI.put("Сейчас включена тёмная тема", "الوضع الداكن مفعّل الآن");
        UI.put("Сейчас включена светлая матовая тема", "الوضع الفاتح مفعّل الآن");
        UI.put("Переключить на светлую", "التبديل إلى الوضع الفاتح");
        UI.put("Переключить на тёмную", "التبديل إلى الوضع الداكن");
        UI.put("ПРЕДПРОСМОТР", "معاينة");
        UI.put("Сбросить прогресс", "إعادة ضبط التقدّم");
        UI.put("Сбросить", "إعادة ضبط");
        UI.put("Предисловие", "المقدمة");
        UI.put("Важное предисловие", "مقدمة مهمة");
        UI.put("Пропустить предисловие", "تخطّي المقدمة");
        UI.put("Вывод и польза", "الخلاصة والفوائد");
        UI.put("Польза", "الفائدة");
        UI.put("Актуальные примеры", "أمثلة واقعية");
        UI.put("Практика", "التطبيق");
        UI.put("Учебные материалы", "المواد التعليمية");
        UI.put("Текущее занятие", "الدرس الحالي");
        UI.put("Далее по курсу", "التالي في الدورة");
        UI.put("Пройдено", "مكتمل");
        UI.put("Новый урок", "درس جديد");
        UI.put("Ещё", "المزيد");
        UI.put("Подробнее", "المزيد من التفاصيل");
        UI.put("Раскрыть смысл глубже", "التعمّق في المعنى");
        UI.put("Продолжить тему", "متابعة الموضوع");
        UI.put("О приложении", "حول التطبيق");
        UI.put("Инструменты", "الأدوات");
        UI.put("Точность", "الدقة");
        UI.put("Обучение и практика", "التعلّم والتطبيق");
        UI.put("Проверка понимания", "اختبار الفهم");
        UI.put("Отмеченные материалы", "المواد المحفوظة");
        UI.put("Проверка знаний", "اختبار المعرفة");
        UI.put("Вопросы и ответы", "الأسئلة والأجوبة");
        UI.put("Важное", "مهم");
        UI.put("Завершено", "مكتمل");
        UI.put("Утренние азкары", "أذكار الصباح");
        UI.put("Вечерние азкары", "أذكار المساء");
        UI.put("Утро", "الصباح");
        UI.put("Вечер", "المساء");
        UI.put("Медицина", "الطب");
        UI.put("Аллах", "الله");
        UI.put("Все", "الكل");
        UI.put("Да", "نعم");
        UI.put("Нет", "لا");
    }

    private CompassLanguage() {}

    public static String get(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String selected = prefs.getString(PREF_KEY, RU);
        return AR.equals(selected) ? AR : RU;
    }

    public static void set(Context context, String language) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putString(PREF_KEY, AR.equals(language) ? AR : RU).apply();
    }

    public static boolean isArabic(Context context) {
        return AR.equals(get(context));
    }

    public static String ui(Context context, String original) {
        if (!isArabic(context) || original == null) return original;
        String mapped = UI.get(original);
        return mapped != null ? mapped : original;
    }

    public static Map<String,String> reviewedUiVocabulary() {
        return Collections.unmodifiableMap(UI);
    }
}
