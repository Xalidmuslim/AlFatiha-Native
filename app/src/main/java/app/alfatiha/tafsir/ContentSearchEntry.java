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

    /** A short excerpt centred on the first body match (without altering source text). */
    String snippet(String query) {
        String flat = body.replaceAll("\\s+", " ").trim();
        if (flat.isEmpty()) return subtitle;
        String q = normalize(query);
        int index = normalize(flat).indexOf(q);
        if (index < 0) return flat.substring(0, Math.min(115, flat.length()))
                + (flat.length() > 115 ? "…" : "");
        int start = Math.max(0, index - 48);
        int end = Math.min(flat.length(), index + q.length() + 75);
        return (start > 0 ? "…" : "") + flat.substring(start, end)
                + (end < flat.length() ? "…" : "");
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
