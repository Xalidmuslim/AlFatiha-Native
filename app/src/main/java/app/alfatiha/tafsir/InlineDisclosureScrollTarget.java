package app.alfatiha.tafsir;

/**
 * Computes a stable scroll destination before a long inline section is removed.
 *
 * ScrollView otherwise instantly clamps its scrollY to the new, shorter content
 * height. This pure function is shared by every expandable native-page panel.
 */
final class InlineDisclosureScrollTarget {
    private InlineDisclosureScrollTarget() { }

    static int whenCollapsing(int currentScrollY, int toggleTopInContent,
            int expandedPageHeight, int disappearingHeight,
            int viewportHeight, int topSpacing) {
        long remainingHeight = Math.max(0L,
                (long) expandedPageHeight - Math.max(0, disappearingHeight));
        long maximumAfterCollapse = Math.max(0L,
                remainingHeight - Math.max(0, viewportHeight));
        long headingPosition = Math.max(0L,
                (long) toggleTopInContent - Math.max(0, topSpacing));
        long current = Math.max(0L, currentScrollY);
        // Never scroll farther down just because a panel was closed.
        // Pre-scroll to a position that is still legal after collapse.
        return (int) Math.min(Math.min(maximumAfterCollapse, headingPosition), current);
    }
}
