package net.engawapg.lib.zoomable

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun applyGesture_enlargeToGreaterThanMax_enlargesToMax() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset.Zero, 6f, Offset(50f, 50f), 0)

        assertEquals(zoomState.scale, 3f)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun applyGesture_enlargeToMax_enlargesAsSpecified() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset.Zero, 5f, Offset(50f, 50f), 0)

        assertEquals(zoomState.scale, 5f)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun applyGesture_enlarge_enlargesAsSpecified() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset.Zero, 2f, Offset(50f, 50f), 0)

        assertEquals(zoomState.scale, 2f)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun applyGesture_shrink_shrinksAsSpecified() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset.Zero, 2f, Offset(50f, 50f), 0)
        assertEquals(zoomState.scale, 2f)
        zoomState.applyGesture(Offset.Zero, 0.5f, Offset(50f, 50f), 0)
        assertEquals(zoomState.scale, 1f)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun applyGesture_shrinkToMin_shrinksToMin() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset.Zero, 0.9f, Offset(50f, 50f), 0)
        assertEquals(zoomState.scale, 0.9f)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun applyGesture_shrinkToLessThanMin_shrinksToMin() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset.Zero, 0.5f, Offset(50f, 50f), 0)
        assertEquals(zoomState.scale, 0.9f)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun applyGesture_zoomZero_shrinksToMin() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset.Zero, 0f, Offset(50f, 50f), 0)
        assertEquals(zoomState.scale, 0.9f)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun applyGesture_zoomNegative_shrinksToMin() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset.Zero, -1f, Offset(50f, 50f), 0)
        assertEquals(zoomState.scale, 0.9f)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun applyGesture_pan_pansAsExpected() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset(30f, 30f), 2f, Offset(50f, 50f), 0)
        assertEquals(zoomState.offsetX, 30f)
        assertEquals(zoomState.offsetY, 30f)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun applyGesture_panBeyondLimits_pansToTopLeftEdge() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset(100f, 100f), 2f, Offset(50f, 50f), 0)
        assertEquals(zoomState.offsetX, 50f)
        assertEquals(zoomState.offsetY, 50f)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun applyGesture_panBeyondLimits_pansToBottomRightEdge() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset(-100f, -100f), 2f, Offset(50f, 50f), 0)
        assertEquals(zoomState.offsetX, -50f)
        assertEquals(zoomState.offsetY, -50f)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun applyGesture_enlargeAtTopLeftCorner_enlargesAndShifts() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset.Zero, 2f, Offset(0f, 0f), 0)
        assertEquals(zoomState.scale, 2f)
        assertEquals(zoomState.offsetX, 50f)
        assertEquals(zoomState.offsetY, 50f)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun applyGesture_enlargeAtBottomRightCorner_enlargesAndShifts() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset.Zero, 2f, Offset(100f, 100f), 0)
        assertEquals(zoomState.scale, 2f)
        assertEquals(zoomState.offsetX, -50f)
        assertEquals(zoomState.offsetY, -50f)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun applyGesture_contentHeightIsLessThanLayoutHeight_notShiftsVertically() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 30f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset(50f, 50f), 2f, Offset.Zero, 0)
        assertEquals(zoomState.offsetX, 50f)
        assertEquals(zoomState.offsetY, 0f)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun applyGesture_contentWidthIsLessThanLayoutWidth_notShiftsHorizontally() = runTest {
        val zoomState = ZoomState(contentSize = Size(30f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset(50f, 50f), 2f, Offset.Zero, 0)
        assertEquals(zoomState.offsetX, 0f)
        assertEquals(zoomState.offsetY, 50f)
    }
}