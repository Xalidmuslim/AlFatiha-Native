package app.alfatiha.tafsir;

final class ContentSearchEntry {
    final String title;
    final String subtitle;
    final String body;
    final String type;
    final String arg;

    ContentSearchEntry(String title, String subtitle, String body, String type, String arg) {
        this.title = title == null ? "" : title;
        this.subtitle = subtitle == null ? "" : subtitle;
        this.body = body == null ? "" : body;
        this.type = type == null ? "" : type;
        this.arg = arg == null ? "" : arg;
    }

    boolean matches(String query) {
        if (query == null) return false;
        String q = normalize(query);
        if (q.length() < 2) return false;
        return normalize(title + " " + subtitle + " " + body).contains(q);
    }

    private static String normalize(String s) {
        return s == null ? "" : s.toLowerCase(java.util.Locale.ROOT)
                .replace('ё','е')
                .replace('‘','\'')
                .replace('’','\'')
                .trim();
    }
}
