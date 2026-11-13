package app.alfatiha.tafsir;

/**
 * Small, runnable regression suite for in-page disclosure collapses.
 * Executes without Android runtime: javac ... && java -ea ...
 */
public final class InlineDisclosureScrollTargetTest {
    private static void check(int expected, int currentY, int toggleY,
            int pageHeight, int removedHeight, int viewportHeight, int spacing) {
        int actual = InlineDisclosureScrollTarget.whenCollapsing(
                currentY, toggleY, pageHeight, removedHeight, viewportHeight, spacing);
        if (actual != expected) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }

    public static void main(String[] args) {
        // The bottom of a long "Актуальные примеры" disclosure is scrolled into view.
        // Closing should smoothly return near its toggle, not jump to top.
        check(444, 1600, 460, 3200, 1600, 900, 16);

        // If the remaining document is shorter, settle at its last legal scroll
        // offset before removing content, otherwise Android clamps abruptly.
        check(500, 2200, 850, 3000, 1600, 900, 16);

        // No downward jump if the heading is below the current viewport.
        check(300, 300, 800, 3600, 400, 900, 16);

        // Short page must reach zero, but the caller ANIMATES to it.
        check(0, 1200, 720, 1800, 1500, 900, 16);

        // Other expanded sections and footer remain scrollable.
        check(780, 1480, 796, 4000, 500, 1000, 16);

        // Prevent invalid negative offsets from pathological layout values.
        check(0, -30, -5, 100, 800, 900, 16);

        System.out.println("Inline disclosure collapse target tests: OK");
    }
}
