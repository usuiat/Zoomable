package net.engawapg.lib.zoomable

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.click
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DetectZoomableGesturesTest : PlatformZoomableTest() {

    private val ViewConfiguration.tapDetermineDelay: Long
        get() = doubleTapTimeoutMillis * 2

    private val ViewConfiguration.doubleTapDelay: Long
        get() = (doubleTapMinTimeMillis + doubleTapTimeoutMillis) / 2

    private fun SemanticsNodeInteraction.performGesture(gesture: TouchInjectionScope.() -> Unit) {
        performTouchInput {
            gesture()

            // Wait for the gesture to complete
            advanceEventTime(viewConfiguration.tapDetermineDelay)
            down(center)
        }
    }

    private fun TouchInjectionScope.pan(x: Float = 0f, y: Float = 0f) {
        down(center)
        moveBy(Offset(x, y))
        up()
    }

    private fun TouchInjectionScope.pinchZoom(zoom: Float) {
        val startDistance = 100f
        down(0, center + Offset(startDistance / 2, 0f))
        down(1, center - Offset(startDistance / 2, 0f))
        val endDistance = startDistance * zoom
        moveTo(0, center + Offset(endDistance / 2, 0f))
        moveTo(1, center - Offset(endDistance / 2, 0f))
        up(0)
        up(1)
    }

    private fun TouchInjectionScope.tapAndDragZoom(zoom: Float) {
        down(center)
        up()
        advanceEventTime(viewConfiguration.doubleTapDelay)
        down(center)
        val y = (zoom - 1f) / 0.004f
        moveBy(Offset(0f, y))
        up()
    }

    private fun ComposeUiTest.pointerInputContent(
        block: suspend PointerInputScope.() -> Unit,
    ): SemanticsNodeInteraction {
        val testTag = "target"
        setContent {
            Box(
                modifier = Modifier
                    .testTag(testTag)
                    .size(300.dp)
                    .pointerInput(key1 = Unit, block = block)
            )
        }
        return onNodeWithTag(testTag)
    }

    @Test
    fun tap_gesture_should_be_judged_as_tap() = runComposeUiTest {
        var tapResult = 0
        val target = pointerInputContent {
            detectZoomableGestures(
                cancelIfZoomCanceled = false,
                canConsumeGesture = { _, _ -> true },
                onGesture = { _, _, _, _ -> },
                onTap = { tapResult++ }
            )
        }

        target.performGesture {
            click()
        }

        assertEquals(1, tapResult)
    }

    @Test
    fun pan_gesture_should_be_judged_as_pan() = runComposeUiTest {
        var panResult = Offset.Zero
        var tapResult = 0
        val target = pointerInputContent {
            detectZoomableGestures(
                cancelIfZoomCanceled = false,
                canConsumeGesture = { _, _ -> true },
                onGesture = { _, pan, _, _ -> panResult += pan },
                onTap = { tapResult++ }
            )
        }

        target.performGesture {
            pan(100f, 100f)
        }

        assertEquals(Offset(100f, 100f), panResult)
        assertEquals(0, tapResult)
    }

    @Test
    fun pinch_gesture_should_be_judged_as_zoom() = runComposeUiTest {
        var zoomResult = 1f
        var tapResult = 0
        val target = pointerInputContent {
            detectZoomableGestures(
                cancelIfZoomCanceled = false,
                canConsumeGesture = { _, _ -> true },
                onGesture = { _, _, zoom, _ -> zoomResult *= zoom },
                onTap = { tapResult++ }
            )
        }

        target.performGesture {
            pinchZoom(2f)
        }

        assertEquals(2f, zoomResult)
        assertEquals(0, tapResult)
    }

    @Test
    fun double_tap_gesture_should_be_judged_as_double_tap() = runComposeUiTest {
        var doubleTapResult = 0
        var tapResult = 0
        val target = pointerInputContent {
            detectZoomableGestures(
                cancelIfZoomCanceled = false,
                canConsumeGesture = { _, _ -> true },
                onGesture = { _, _, _, _ -> },
                onTap = { tapResult++ },
                onDoubleTap = { doubleTapResult++ }
            )
        }

        target.performGesture {
            doubleClick()
        }

        assertEquals(1, doubleTapResult)
        assertEquals(0, tapResult)
    }

    @Test
    fun double_tap_gesture_should_be_judged_as_double_tap_if_one_finger_zoom_is_disabled() = runComposeUiTest {
        var doubleTapResult = 0
        var tapResult = 0
        val target = pointerInputContent {
            detectZoomableGestures(
                cancelIfZoomCanceled = false,
                canConsumeGesture = { _, _ -> true },
                onGesture = { _, _, _, _ -> },
                onTap = { tapResult++ },
                onDoubleTap = { doubleTapResult++ },
                enableOneFingerZoom = false
            )
        }

        target.performGesture {
            doubleClick()
        }

        assertEquals(1, doubleTapResult)
        assertEquals(0, tapResult)
    }

    @Test
    fun tap_and_drag_gesture_should_be_judged_as_zoom() = runComposeUiTest {
        var zoomResult = 1f
        var panResult = Offset.Zero
        var tapResult = 0
        var doubleTapResult = 0
        val target = pointerInputContent {
            detectZoomableGestures(
                cancelIfZoomCanceled = false,
                canConsumeGesture = { _, _ -> true },
                onGesture = { _, pan, zoom, _ -> panResult += pan; zoomResult *= zoom },
                onTap = { tapResult++ },
                onDoubleTap = { doubleTapResult++ }
            )
        }

        target.performGesture {
            tapAndDragZoom(1.5f)
        }

        assertEquals(1.5f, zoomResult)
        assertEquals(Offset.Zero, panResult)
        assertEquals(0, tapResult)
        assertEquals(0, doubleTapResult)
    }

    @Test
    fun tap_and_drag_gesture_should_ignored_if_one_finger_zoom_is_disabled() = runComposeUiTest {
        var zoomResult = 1f
        var panResult = Offset.Zero
        var tapResult = 0
        var doubleTapResult = 0
        val target = pointerInputContent {
            detectZoomableGestures(
                cancelIfZoomCanceled = false,
                canConsumeGesture = { _, _ -> true },
                onGesture = { _, pan, zoom, _ -> panResult += pan; zoomResult *= zoom },
                onTap = { tapResult++ },
                onDoubleTap = { doubleTapResult++ },
                enableOneFingerZoom = false
            )
        }

        target.performGesture {
            tapAndDragZoom(1.5f)
        }

        assertEquals(1f, zoomResult)
        assertEquals(Offset.Zero, panResult)
        assertEquals(0, tapResult)
        assertEquals(0, doubleTapResult)
    }

    @Test
    fun slight_movement_on_tap_should_be_ignored() = runComposeUiTest {
        var tapResult = 0
        var panResult = Offset.Zero
        val target = pointerInputContent {
            detectZoomableGestures(
                cancelIfZoomCanceled = false,
                canConsumeGesture = { _, _ -> true },
                onGesture = { _, pan, _, _ -> panResult += pan },
                onTap = { tapResult++ }
            )
        }

        target.performGesture {
            down(center)
            moveBy(Offset(viewConfiguration.touchSlop / 2f, 0f))
            up()
        }

        assertEquals(1, tapResult)
        assertEquals(Offset.Zero, panResult)
    }

    @Test
    fun slight_movement_on_double_tap_should_be_ignored() = runComposeUiTest {
        var doubleTapResult = 0
        var panResult = Offset.Zero
        var zoomResult = 1f
        val target = pointerInputContent {
            detectZoomableGestures(
                cancelIfZoomCanceled = false,
                canConsumeGesture = { _, _ -> true },
                onGesture = { _, pan, zoom, _ -> panResult += pan; zoomResult *= zoom },
                onDoubleTap = { doubleTapResult++ }
            )
        }

        target.performGesture {
            down(center)
            up()
            advanceEventTime(viewConfiguration.doubleTapDelay)
            down(center)
            moveBy(Offset(0f, viewConfiguration.touchSlop / 2f))
            up()
        }

        assertEquals(1, doubleTapResult)
        assertEquals(Offset.Zero, panResult)
        assertEquals(1f, zoomResult)
    }

    @Test
    fun two_finger_tap_gesture_should_not_be_judged_as_tap() = runComposeUiTest {
        var tapResult = 0
        var zoomResult = 1f
        val target = pointerInputContent {
            detectZoomableGestures(
                cancelIfZoomCanceled = false,
                canConsumeGesture = { _, _ -> true },
                onGesture = { _, _, zoom, _ -> zoomResult *= zoom },
                onTap = { tapResult++ }
            )
        }

        target.performGesture {
            down(0, center)
            down(1, center)
            up(0)
            up(1)
        }

        assertEquals(0, tapResult)
        assertEquals(1f, zoomResult)
    }

    @Test
    fun tap_and_two_finger_tap_gesture_should_not_be_judged_as_double_tap() = runComposeUiTest {
        var doubleTapResult = 0
        val target = pointerInputContent {
            detectZoomableGestures(
                cancelIfZoomCanceled = false,
                canConsumeGesture = { _, _ -> true },
                onGesture = { _, _, _, _ -> },
                onDoubleTap = { doubleTapResult++ }
            )
        }

        target.performGesture {
            down(center)
            up()
            advanceEventTime(viewConfiguration.doubleTapDelay)
            down(0, center)
            down(1, center)
            up(0)
            up(1)
        }

        assertEquals(0, doubleTapResult)
    }

    @Test
    fun long_press_gesture_should_not_be_judged_as_tap() = runComposeUiTest {
        var tapResult = 0
        val target = pointerInputContent {
            detectZoomableGestures(
                cancelIfZoomCanceled = false,
                canConsumeGesture = { _, _ -> true },
                onGesture = { _, _, _, _ -> },
                onTap = { tapResult++ }
            )
        }

        target.performGesture {
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis * 2)
            up()
        }

        assertEquals(0, tapResult)
    }

    @Test
    fun tap_and_long_press_gesture_should_not_be_judged_as_double_tap() = runComposeUiTest {
        var doubleTapResult = 0
        val target = pointerInputContent {
            detectZoomableGestures(
                cancelIfZoomCanceled = false,
                canConsumeGesture = { _, _ -> true },
                onGesture = { _, _, _, _ -> },
                onDoubleTap = { doubleTapResult++ }
            )
        }

        target.performGesture {
            down(center)
            up()
            advanceEventTime(viewConfiguration.doubleTapDelay)
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis * 2)
            up()
        }

        assertEquals(0, doubleTapResult)
    }

    @Test
    fun gesture_should_continue_if_number_of_fingers_changes() = runComposeUiTest {
        var zoomResult = 1f
        var panResult = Offset.Zero
        val target = pointerInputContent {
            detectZoomableGestures(
                cancelIfZoomCanceled = false,
                canConsumeGesture = { _, _ -> true },
                onGesture = { _, pan, zoom, _ -> panResult += pan; zoomResult *= zoom },
            )
        }

        target.performGesture {
            // move right 50px
            down(0, center)
            moveBy(Offset(50f, 0f))
            // zoom 2x
            down(1, center + Offset(-50f, 0f))
            moveBy(0, Offset(50f, 0f))
            moveBy(1, Offset(-50f, 0f))
            // move down 50px
            up(0)
            moveBy(1, Offset(0f, 50f))
            // zoom 2x
            down(0, center + Offset(0f, 50f))
            moveBy(0, Offset(50f, 0f))
            moveBy(1, Offset(-50f, 0f))
            up(0)
            up(1)
        }

        assertEquals(4f, zoomResult)
        assertEquals(Offset(50f, 50f), panResult)
    }

    @Test
    fun gesture_should_stop_when_number_of_fingers_become_one() = runComposeUiTest {
        var zoomResult = 1f
        var panResult = Offset.Zero
        val target = pointerInputContent {
            detectZoomableGestures(
                cancelIfZoomCanceled = true,
                canConsumeGesture = { _, _ -> true },
                onGesture = { _, pan, zoom, _ -> panResult += pan; zoomResult *= zoom },
            )
        }

        target.performGesture {
            // move right 50px
            down(0, center)
            moveBy(Offset(50f, 0f))
            // zoom 2x
            down(1, center + Offset(-50f, 0f))
            moveBy(0, Offset(50f, 0f))
            moveBy(1, Offset(-50f, 0f))
            // move down 50px
            up(0)
            moveBy(1, Offset(0f, 50f))
            // zoom 2x
            down(0, center + Offset(0f, 50f))
            moveBy(0, Offset(50f, 0f))
            moveBy(1, Offset(-50f, 0f))
            up(0)
            up(1)
        }

        assertEquals(2f, zoomResult)
        assertEquals(Offset(50f, 0f), panResult)
    }

    private fun ComposeUiTest.nestedPointerInputContent(
        firstDownOnParent: (down: PointerInputChange) -> Unit = {},
        eventOnParent: (event: PointerEvent) -> Unit = {},
        eventOnChild: (event: PointerEvent) -> Unit = {},
        block: suspend PointerInputScope.() -> Unit,
    ): SemanticsNodeInteraction {
        val targetTag = "target"
        setContent {
            Box(
                modifier = Modifier
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            firstDownOnParent(awaitFirstDown(requireUnconsumed = false))
                            do {
                                val event = awaitPointerEvent()
                                eventOnParent(event)
                            } while (event.changes.any { it.pressed })
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .testTag(targetTag)
                        .size(300.dp)
                        .pointerInput(key1 = Unit, block = block)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    do {
                                        val event = awaitPointerEvent()
                                        eventOnChild(event)
                                    } while (event.changes.any { it.pressed })
                                }
                            }
                    )
                }
            }
        }
        return onNodeWithTag(targetTag)
    }

    @Test
    fun first_down_event_should_be_consumed() = runComposeUiTest {
        val downEvents = mutableListOf<PointerInputChange>()
        val target = nestedPointerInputContent(
            firstDownOnParent = { if (!it.isConsumed) downEvents.add(it) },
        ) {
            detectZoomableGestures(
                cancelIfZoomCanceled = false,
                canConsumeGesture = { _, _ -> true },
                onGesture = { _, _, _, _ -> },
            )
        }

        target.performGesture {
            pinchZoom(2f)
            pan(50f)
            doubleClick()
            click()
        }

        assertTrue(downEvents.isEmpty())
    }

    @Test
    fun gesture_should_be_canceled_if_event_is_consumed_by_child() = runComposeUiTest {
        var panResult = Offset.Zero
        var zoomResult = 1f
        var doubleTapResult = 0
        var tapResult = 0
        val target = nestedPointerInputContent(
            eventOnChild = { event ->
                event.changes.filter { it.positionChanged() }.forEach { it.consume() }
            }
        ) {
            detectZoomableGestures(
                cancelIfZoomCanceled = false,
                canConsumeGesture = { _, _ -> true },
                onGesture = { _, pan, zoom, _ -> panResult += pan; zoomResult *= zoom },
                onDoubleTap = { doubleTapResult++ },
                onTap = { tapResult++ }
            )
        }

        target.performGesture {
            pinchZoom(2f)
            pan(50f)

            down(center)
            up()
            advanceEventTime(viewConfiguration.doubleTapDelay)
            down(center)
            moveBy(Offset(0f, viewConfiguration.touchSlop / 2f))
            up()

            down(center)
            moveBy(Offset(0f, viewConfiguration.touchSlop / 2f))
            up()
        }

        assertEquals(Offset.Zero, panResult)
        assertEquals(1f, zoomResult)
        assertEquals(0, doubleTapResult, "double tap should not be called")
        assertEquals(0, tapResult, "tap should not be called")
    }

    @Test
    fun gesture_should_be_canceled_if_event_is_consumed_by_parent_before_touch_slop_is_past() = runComposeUiTest {
        var panResult = Offset.Zero
        var zoomResult = 1f
        val target = nestedPointerInputContent(
            eventOnParent = { event ->
                event.changes.filter { it.positionChanged() }.forEach { it.consume() }
            }
        ) {
            detectZoomableGestures(
                cancelIfZoomCanceled = false,
                canConsumeGesture = { _, _ -> true },
                onGesture = { _, pan, zoom, _ -> panResult += pan; zoomResult *= zoom },
            )
        }

        target.performGesture {
            down(center)
            moveBy(Offset(viewConfiguration.touchSlop / 2f, 0f))
            moveBy(Offset(50f, 0f))
            up()

            down(center)
            up()
            advanceEventTime(viewConfiguration.doubleTapDelay)
            down(center)
            moveBy(Offset(0f, viewConfiguration.touchSlop / 2f))
            moveBy(Offset(0f, 100f))
            up()
        }

        assertEquals(Offset.Zero, panResult)
        assertEquals(1f, zoomResult)
    }

    @Test
    fun event_should_be_consumed_if_allowed() = runComposeUiTest {
        val moveChanges = mutableListOf<PointerInputChange>()
        var panResult = Offset.Zero
        var zoomResult = 1f
        val target = nestedPointerInputContent(
            eventOnParent = { event ->
                moveChanges.addAll(event.changes.filter { it.positionChanged() })
            }
        ) {
            detectZoomableGestures(
                cancelIfZoomCanceled = false,
                canConsumeGesture = { _, _ -> true },
                onGesture = { _, pan, zoom, _ -> panResult += pan; zoomResult *= zoom },
            )
        }

        target.performGesture {
            pan(50f)
            pinchZoom(2f)
            tapAndDragZoom(1.5f)
        }

        assertTrue(moveChanges.all { it.isConsumed })
        assertEquals(Offset(50f, 0f), panResult)
        assertEquals(3f, zoomResult)
    }

    @Test
    fun event_should_not_be_consumed_if_not_allowed() = runComposeUiTest {
        val moveChanges = mutableListOf<PointerInputChange>()
        var panResult = Offset.Zero
        var zoomResult = 1f
        val target = nestedPointerInputContent(
            eventOnParent = { event ->
                moveChanges.addAll(event.changes.filter { it.positionChanged() })
            }
        ) {
            detectZoomableGestures(
                cancelIfZoomCanceled = false,
                canConsumeGesture = { _, _ -> false },
                onGesture = { _, pan, zoom, _ -> panResult += pan; zoomResult *= zoom },
            )
        }

        target.performGesture {
            pan(50f)
            pinchZoom(2f)
            tapAndDragZoom(1.5f)
        }

        assertTrue(moveChanges.none { it.isConsumed })
        assertEquals(Offset.Zero, panResult)
        assertEquals(1f, zoomResult)
    }
}
