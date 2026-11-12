package com.xalid.meditsinaproroka.nativeapp

import org.junit.Assert.assertEquals
import org.junit.Test

class CompassThemeSelectionTest {
    @Test fun automaticModeFollowsSystemInBothDirections() {
        assertEquals("dark", resolveCompassThemeSelection("system", false, true))
        assertEquals("light", resolveCompassThemeSelection("system", true, false))
    }

    @Test fun manualModeOverridesSystemAndPreviousLegacyState() {
        assertEquals("dark", resolveCompassThemeSelection("dark", false, false))
        assertEquals("light", resolveCompassThemeSelection("light", true, true))
    }

    @Test fun olderInstallsRemainCompatible() {
        assertEquals("dark", resolveCompassThemeSelection(null, true, false))
        assertEquals("light", resolveCompassThemeSelection(null, false, true))
    }

    @Test fun firstInstallAndUnknownModeUseSafeFallbacks() {
        assertEquals("dark", resolveCompassThemeSelection(null, null, true))
        assertEquals("light", resolveCompassThemeSelection("invalid", null, false))
    }
}
