package net.engawapg.lib.zoomable

import org.junit.Assert.*
import org.junit.Test

class ZoomStateTest {

    @Test
    fun zoomState_noArgs_instantiated() {
        val zoomState = ZoomState()
        assertNotNull(zoomState)
    }

    @Test
    fun zoomState_maxScale_1_instantiated() {
        val zoomState = ZoomState(maxScale = 1.0f)
        assertNotNull(zoomState)
    }

    @Test
    fun zoomState_maxScale_099_throwException() {
        assertThrows(
            "maxScale must be at least 1.0.",
            IllegalArgumentException::class.java
        ) {
            ZoomState(maxScale = 0.99f)
        }
    }
}