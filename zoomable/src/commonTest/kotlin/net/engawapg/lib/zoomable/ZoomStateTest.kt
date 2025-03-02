package net.engawapg.lib.zoomable

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest

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
        assertFailsWith<IllegalArgumentException>(
            "maxScale must be at least 1.0."
        ) {
            ZoomState(maxScale = 0.99f)
        }
    }

    @Test
    fun zoomState_is_initialized_with_arbitrary_scale() {
        val zoomState = ZoomState(initialScale = 2.0f)
        assertEquals(zoomState.scale, 2.0f)
    }

    @Test
    fun applyGesture_enlargeToGreaterThanMax_enlargesToMax() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset.Zero, 6f, Offset(50f, 50f), 0)

        assertEquals(zoomState.scale, 5f)
    }

    @Test
    fun applyGesture_enlargeToMax_enlargesAsSpecified() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset.Zero, 5f, Offset(50f, 50f), 0)

        assertEquals(zoomState.scale, 5f)
    }

    @Test
    fun applyGesture_enlarge_enlargesAsSpecified() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset.Zero, 2f, Offset(50f, 50f), 0)

        assertEquals(zoomState.scale, 2f)
    }

    @Test
    fun applyGesture_shrink_shrinksAsSpecified() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset.Zero, 2f, Offset(50f, 50f), 0)
        assertEquals(zoomState.scale, 2f)
        zoomState.applyGesture(Offset.Zero, 0.5f, Offset(50f, 50f), 0)
        assertEquals(zoomState.scale, 1f)
    }

    @Test
    fun applyGesture_shrinkToMin_shrinksToMin() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset.Zero, 0.9f, Offset(50f, 50f), 0)
        assertEquals(zoomState.scale, 0.9f)
    }

    @Test
    fun applyGesture_shrinkToLessThanMin_shrinksToMin() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset.Zero, 0.5f, Offset(50f, 50f), 0)
        assertEquals(zoomState.scale, 0.9f)
    }

    @Test
    fun applyGesture_zoomZero_shrinksToMin() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset.Zero, 0f, Offset(50f, 50f), 0)
        assertEquals(zoomState.scale, 0.9f)
    }

    @Test
    fun applyGesture_zoomNegative_shrinksToMin() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset.Zero, -1f, Offset(50f, 50f), 0)
        assertEquals(zoomState.scale, 0.9f)
    }

    @Test
    fun applyGesture_pan_pansAsExpected() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset(30f, 30f), 2f, Offset(50f, 50f), 0)
        assertEquals(zoomState.offsetX, 30f)
        assertEquals(zoomState.offsetY, 30f)
    }

    @Test
    fun applyGesture_panBeyondLimits_pansToTopLeftEdge() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset(100f, 100f), 2f, Offset(50f, 50f), 0)
        assertEquals(zoomState.offsetX, 50f)
        assertEquals(zoomState.offsetY, 50f)
    }

    @Test
    fun applyGesture_panBeyondLimits_pansToBottomRightEdge() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset(-100f, -100f), 2f, Offset(50f, 50f), 0)
        assertEquals(zoomState.offsetX, -50f)
        assertEquals(zoomState.offsetY, -50f)
    }

    @Test
    fun applyGesture_enlargeAtTopLeftCorner_enlargesAndShifts() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset.Zero, 2f, Offset(0f, 0f), 0)
        assertEquals(zoomState.scale, 2f)
        assertEquals(zoomState.offsetX, 50f)
        assertEquals(zoomState.offsetY, 50f)
    }

    @Test
    fun applyGesture_enlargeAtBottomRightCorner_enlargesAndShifts() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset.Zero, 2f, Offset(100f, 100f), 0)
        assertEquals(zoomState.scale, 2f)
        assertEquals(zoomState.offsetX, -50f)
        assertEquals(zoomState.offsetY, -50f)
    }

    @Test
    fun applyGesture_contentHeightIsLessThanLayoutHeight_notShiftsVertically() = runTest {
        val zoomState = ZoomState(contentSize = Size(100f, 30f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset(50f, 50f), 2f, Offset.Zero, 0)
        assertEquals(zoomState.offsetX, 50f)
        assertEquals(zoomState.offsetY, 0f)
    }

    @Test
    fun applyGesture_contentWidthIsLessThanLayoutWidth_notShiftsHorizontally() = runTest {
        val zoomState = ZoomState(contentSize = Size(30f, 100f))
        zoomState.setLayoutSize(Size(100f, 100f))

        zoomState.applyGesture(Offset(50f, 50f), 2f, Offset.Zero, 0)
        assertEquals(zoomState.offsetX, 0f)
        assertEquals(zoomState.offsetY, 50f)
    }
}

class ZoomStateTestPanWillChangeOffset {
    data class TestCase(
        val name: String,
        val zoomPosition: Offset,
        val pan: Offset,
        val expected: Boolean,
    ) {
        override fun toString() = name
    }

    companion object {
        val rect = Rect(Offset.Zero, Size(100f, 100f))
    }

    @Test
    fun pan_right_from_right_edge_will_change_offset() = runTestCase(
        TestCase(
            name = "Pan right from right edge will change offset",
            zoomPosition = rect.centerRight,
            pan = Offset(1f, 0f),
            expected = true,
        )
    )

    @Test
    fun pan_right_from_left_edge_will_not_change_offset() = runTestCase(
        TestCase(
            name = "Pan right from left edge will not change offset",
            zoomPosition = rect.centerLeft,
            pan = Offset(1f, 0f),
            expected = false,
        ),
    )

    @Test
    fun pan_left_from_left_edge_will_change_offset() = runTestCase(
        TestCase(
            name = "Pan left from left edge will change offset",
            zoomPosition = rect.centerLeft,
            pan = Offset(-1f, 0f),
            expected = true,
        )
    )

    @Test
    fun pan_left_from_right_edge_will_not_change_offset() = runTestCase(
        TestCase(
            name = "Pan left from right edge will not change offset",
            zoomPosition = rect.centerRight,
            pan = Offset(-1f, 0f),
            expected = false,
        )
    )

    @Test
    fun pan_up_from_top_edge_will_change_offset() = runTestCase(
        TestCase(
            name = "Pan up from top edge will change offset",
            zoomPosition = rect.topCenter,
            pan = Offset(0f, -1f),
            expected = true,
        )
    )

    @Test
    fun pan_up_from_bottom_edge_will_not_change_offset() = runTestCase(
        TestCase(
            name = "Pan up from bottom edge will not change offset",
            zoomPosition = rect.bottomCenter,
            pan = Offset(0f, -1f),
            expected = false,
        )
    )

    @Test
    fun pan_down_from_bottom_edge_will_change_offset() = runTestCase(
        TestCase(
            name = "Pan down from bottom edge will change offset",
            zoomPosition = rect.bottomCenter,
            pan = Offset(0f, 1f),
            expected = true,
        )
    )

    @Test
    fun pan_down_from_top_edge_will_not_change_offset() = runTestCase(
        TestCase(
            name = "Pan down from top edge will not change offset",
            zoomPosition = rect.topCenter,
            pan = Offset(0f, 1f),
            expected = false,
        )
    )

    private fun runTestCase(testCase: TestCase) = runTest {
        val zoomState = ZoomState(contentSize = rect.size)
        zoomState.setLayoutSize(rect.size)
        zoomState.applyGesture(Offset.Zero, 2f, testCase.zoomPosition, 0)

        val result = zoomState.willChangeOffset(testCase.pan)

        assertEquals(result, testCase.expected)
    }
}
