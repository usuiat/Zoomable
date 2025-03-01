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

    private data class ZoomableResult(
        var pan: Offset = Offset.Zero,
        var zoom: Float = 1f,
        var tap: Int = 0,
        var doubleTap: Int = 0,
        var longPress: Int = 0,
    )

    private fun ComposeUiTest.pointerInputContentWithDetectZoomableGestures(
        result: ZoomableResult,
        cancelIfZoomCanceled: Boolean = false,
        enableOneFingerZoom: Boolean = true,
        canConsumeGesture: (Offset, Float) -> Boolean = { _, _ -> true },
    ): SemanticsNodeInteraction {
        val testTag = "target"
        setContent {
            Box(
                modifier = Modifier
                    .testTag(testTag)
                    .size(300.dp)
                    .pointerInput(Unit) {
                        detectZoomableGestures(
                            cancelIfZoomCanceled = { cancelIfZoomCanceled },
                            canConsumeGesture = canConsumeGesture,
                            onGesture = { _, pan, zoom, _ ->
                                result.pan += pan
                                result.zoom *= zoom
                            },
                            onTap = { result.tap++ },
                            onDoubleTap = { result.doubleTap++ },
                            onLongPress = { result.longPress++ },
                            enableOneFingerZoom = { enableOneFingerZoom },
                        )
                    }
            )
        }
        return onNodeWithTag(testTag)
    }

    private fun ComposeUiTest.nestedPointerInputContentWithDetectZoomableGestures(
        result: ZoomableResult,
        cancelIfZoomCanceled: Boolean = false,
        enableOneFingerZoom: Boolean = true,
        canConsumeGesture: (Offset, Float) -> Boolean = { _, _ -> true },
        firstDownOnParent: (down: PointerInputChange) -> Unit = {},
        eventOnParent: (event: PointerEvent) -> Unit = {},
        eventOnChild: (event: PointerEvent) -> Unit = {},
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
                        .pointerInput(Unit) {
                            detectZoomableGestures(
                                cancelIfZoomCanceled = { cancelIfZoomCanceled },
                                canConsumeGesture = canConsumeGesture,
                                onGesture = { _, pan, zoom, _ ->
                                    result.pan += pan
                                    result.zoom *= zoom
                                },
                                onTap = { result.tap++ },
                                onDoubleTap = { result.doubleTap++ },
                                onLongPress = { result.longPress++ },
                                enableOneFingerZoom = { enableOneFingerZoom },
                            )
                        }
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
    fun tap_gesture_should_be_judged_as_tap() = runComposeUiTest {
        val result = ZoomableResult()
        val target = pointerInputContentWithDetectZoomableGestures(result)

        target.performGesture {
            click()
        }

        assertEquals(1, result.tap)
    }

    @Test
    fun pan_gesture_should_be_judged_as_pan() = runComposeUiTest {
        val result = ZoomableResult()
        val target = pointerInputContentWithDetectZoomableGestures(result)

        target.performGesture {
            pan(100f, 100f)
        }

        assertEquals(Offset(100f, 100f), result.pan)
        assertEquals(0, result.tap)
    }

    @Test
    fun pinch_gesture_should_be_judged_as_zoom() = runComposeUiTest {
        val result = ZoomableResult()
        val target = pointerInputContentWithDetectZoomableGestures(result)

        target.performGesture {
            pinchZoom(2f)
        }

        assertEquals(2f, result.zoom)
        assertEquals(0, result.tap)
    }

    @Test
    fun double_tap_gesture_should_be_judged_as_double_tap() = runComposeUiTest {
        val result = ZoomableResult()
        val target = pointerInputContentWithDetectZoomableGestures(result)

        target.performGesture {
            doubleClick()
        }

        assertEquals(1, result.doubleTap)
        assertEquals(0, result.tap)
    }

    @Test
    fun double_tap_gesture_should_be_judged_as_double_tap_if_one_finger_zoom_is_disabled() =
        runComposeUiTest {
            val result = ZoomableResult()
            val target = pointerInputContentWithDetectZoomableGestures(
                result = result,
                enableOneFingerZoom = false,
            )

            target.performGesture {
                doubleClick()
            }

            assertEquals(1, result.doubleTap)
            assertEquals(0, result.tap)
        }

    @Test
    fun tap_and_drag_gesture_should_be_judged_as_zoom() = runComposeUiTest {
        val result = ZoomableResult()
        val target = pointerInputContentWithDetectZoomableGestures(result)

        target.performGesture {
            tapAndDragZoom(1.5f)
        }

        assertEquals(1.5f, result.zoom)
        assertEquals(Offset.Zero, result.pan)
        assertEquals(0, result.tap)
        assertEquals(0, result.doubleTap)
    }

    @Test
    fun tap_and_drag_gesture_should_ignored_if_one_finger_zoom_is_disabled() = runComposeUiTest {
        val result = ZoomableResult()
        val target = pointerInputContentWithDetectZoomableGestures(
            result = result,
            enableOneFingerZoom = false,
        )

        target.performGesture {
            tapAndDragZoom(1.5f)
        }

        assertEquals(1f, result.zoom)
        assertEquals(Offset.Zero, result.pan)
        assertEquals(0, result.tap)
        assertEquals(0, result.doubleTap)
    }

    @Test
    fun long_press_gesture_should_be_judged_as_long_press() = runComposeUiTest {
        val result = ZoomableResult()
        val target = pointerInputContentWithDetectZoomableGestures(result)

        target.performTouchInput {
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis / 2)
            move() // Send 0px move event to advance the clock
        }

        assertEquals(0, result.longPress)

        target.performTouchInput {
            advanceEventTime(500L)
            move() // Send 0px move event to advance the clock
        }

        assertEquals(1, result.longPress)

        target.performGesture {
            up()
        }

        assertEquals(0, result.tap)
    }

    @Test
    fun slight_movement_on_tap_should_be_ignored() = runComposeUiTest {
        val result = ZoomableResult()
        val target = pointerInputContentWithDetectZoomableGestures(result)

        target.performGesture {
            down(center)
            moveBy(Offset(viewConfiguration.touchSlop / 2f, 0f))
            up()
        }

        assertEquals(1, result.tap)
        assertEquals(Offset.Zero, result.pan)
    }

    @Test
    fun slight_movement_on_double_tap_should_be_ignored() = runComposeUiTest {
        val result = ZoomableResult()
        val target = pointerInputContentWithDetectZoomableGestures(result)

        target.performGesture {
            down(center)
            up()
            advanceEventTime(viewConfiguration.doubleTapDelay)
            down(center)
            moveBy(Offset(0f, viewConfiguration.touchSlop / 2f))
            up()
        }

        assertEquals(1, result.doubleTap)
        assertEquals(Offset.Zero, result.pan)
        assertEquals(1f, result.zoom)
    }

    @Test
    fun slight_movement_on_long_tap_should_be_ignored() = runComposeUiTest {
        val result = ZoomableResult()
        val target = pointerInputContentWithDetectZoomableGestures(result)

        target.performGesture {
            down(center)
            moveBy(Offset(0f, viewConfiguration.touchSlop / 2f))
            advanceEventTime(viewConfiguration.longPressTimeoutMillis * 2)
            up()
        }

        assertEquals(1, result.longPress)
        assertEquals(Offset.Zero, result.pan)
    }

    @Test
    fun two_finger_tap_gesture_should_not_be_judged_as_tap() = runComposeUiTest {
        val result = ZoomableResult()
        val target = pointerInputContentWithDetectZoomableGestures(result)

        target.performGesture {
            down(0, center)
            down(1, center)
            up(0)
            up(1)
        }

        assertEquals(0, result.tap)
        assertEquals(1f, result.zoom)
    }

    @Test
    fun tap_and_two_finger_tap_gesture_should_not_be_judged_as_double_tap() = runComposeUiTest {
        val result = ZoomableResult()
        val target = pointerInputContentWithDetectZoomableGestures(result)

        target.performGesture {
            down(center)
            up()
            advanceEventTime(viewConfiguration.doubleTapDelay)
            down(0, center)
            down(1, center)
            up(0)
            up(1)
        }

        assertEquals(0, result.doubleTap)
    }

    @Test
    fun two_finger_long_tap_gesture_should_not_be_judged_as_long_press() = runComposeUiTest {
        val result = ZoomableResult()
        val target = pointerInputContentWithDetectZoomableGestures(result)

        target.performGesture {
            down(0, center)
            down(1, center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis * 2)
            up(0)
            up(1)
        }

        assertEquals(0, result.longPress)
    }

    @Test
    fun tap_and_long_press_gesture_should_not_be_judged_as_double_tap() = runComposeUiTest {
        val result = ZoomableResult()
        val target = pointerInputContentWithDetectZoomableGestures(result)

        target.performGesture {
            down(center)
            up()
            advanceEventTime(viewConfiguration.doubleTapDelay)
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis * 2)
            up()
        }

        assertEquals(0, result.doubleTap)
    }

    @Test
    fun gesture_should_continue_if_number_of_fingers_changes() = runComposeUiTest {
        val result = ZoomableResult()
        val target = pointerInputContentWithDetectZoomableGestures(result)

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

        assertEquals(4f, result.zoom)
        assertEquals(Offset(50f, 50f), result.pan)
    }

    @Test
    fun gesture_should_stop_when_number_of_fingers_become_one() = runComposeUiTest {
        val result = ZoomableResult()
        val target = pointerInputContentWithDetectZoomableGestures(
            result = result,
            cancelIfZoomCanceled = true,
        )

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

        assertEquals(2f, result.zoom)
        assertEquals(Offset(50f, 0f), result.pan)
    }

    @Test
    fun pan_and_zoom_after_long_press_should_be_ignored() = runComposeUiTest {
        val result = ZoomableResult()
        val target = pointerInputContentWithDetectZoomableGestures(result)

        target.performGesture {
            // long press
            down(0, center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis * 2)
            // pan
            moveBy(Offset(50f, 0f))
            // zoom
            down(1, center + Offset(-50f, 0f))
            moveBy(0, Offset(50f, 0f))
            moveBy(1, Offset(-50f, 0f))
            up(0)
            up(1)
        }

        assertEquals(Offset.Zero, result.pan)
        assertEquals(1f, result.zoom)
        assertEquals(1, result.longPress)
    }

    @Test
    fun long_press_after_pan_should_be_ignored() = runComposeUiTest {
        val result = ZoomableResult()
        val target = pointerInputContentWithDetectZoomableGestures(result)

        target.performGesture {
            down(center)
            moveBy(Offset(50f, 0f))
            advanceEventTime(viewConfiguration.longPressTimeoutMillis * 2)
            up()
        }

        assertEquals(0, result.longPress)
        assertEquals(Offset(50f, 0f), result.pan)
    }

    @Test
    fun first_down_event_should_be_consumed() = runComposeUiTest {
        val downEvents = mutableListOf<PointerInputChange>()
        val target = nestedPointerInputContentWithDetectZoomableGestures(
            result = ZoomableResult(),
            firstDownOnParent = { if (!it.isConsumed) downEvents.add(it) },
        )

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
        val result = ZoomableResult()
        val target = nestedPointerInputContentWithDetectZoomableGestures(
            result = result,
            eventOnChild = { event ->
                event.changes.filter { it.positionChanged() }.forEach { it.consume() }
            }
        )

        target.performGesture {
            pinchZoom(2f)
            pan(50f)

            down(center)
            moveBy(Offset(0f, viewConfiguration.touchSlop / 2f))
            advanceEventTime(viewConfiguration.longPressTimeoutMillis * 2)
            up()

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

        assertEquals(Offset.Zero, result.pan)
        assertEquals(1f, result.zoom)
        assertEquals(0, result.longPress, "long press should not be called")
        assertEquals(0, result.doubleTap, "double tap should not be called")
        assertEquals(0, result.tap, "tap should not be called")
    }

    @Test
    fun gesture_should_be_canceled_if_event_is_consumed_by_parent_before_touch_slop_is_past() =
        runComposeUiTest {
            val result = ZoomableResult()
            val target = nestedPointerInputContentWithDetectZoomableGestures(
                result = result,
                eventOnParent = { event ->
                    event.changes.filter { it.positionChanged() }.forEach { it.consume() }
                }
            )

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

            assertEquals(Offset.Zero, result.pan)
            assertEquals(1f, result.zoom)
        }

    @Test
    fun event_should_be_consumed_if_allowed() = runComposeUiTest {
        val moveChanges = mutableListOf<PointerInputChange>()
        val result = ZoomableResult()
        val target = nestedPointerInputContentWithDetectZoomableGestures(
            result = result,
            eventOnParent = { event ->
                moveChanges.addAll(event.changes.filter { it.positionChanged() })
            }
        )

        target.performGesture {
            pan(50f)
            pinchZoom(2f)
            tapAndDragZoom(1.5f)
        }

        assertTrue(moveChanges.all { it.isConsumed })
        assertEquals(Offset(50f, 0f), result.pan)
        assertEquals(3f, result.zoom)
    }

    @Test
    fun event_should_not_be_consumed_if_not_allowed() = runComposeUiTest {
        val moveChanges = mutableListOf<PointerInputChange>()
        val result = ZoomableResult()
        val target = nestedPointerInputContentWithDetectZoomableGestures(
            result = result,
            canConsumeGesture = { _, _ -> false },
            eventOnParent = { event ->
                moveChanges.addAll(event.changes.filter { it.positionChanged() })
            }
        )

        target.performGesture {
            pan(50f)
            pinchZoom(2f)
            tapAndDragZoom(1.5f)
        }

        assertTrue(moveChanges.none { it.isConsumed })
        assertEquals(Offset.Zero, result.pan)
        assertEquals(1f, result.zoom)
    }

    @Test
    fun event_after_long_press_should_be_consumed() = runComposeUiTest {
        val changes = mutableListOf<PointerInputChange>()
        var longPressed = false
        val result = ZoomableResult()
        val target = nestedPointerInputContentWithDetectZoomableGestures(
            result = result,
            eventOnParent = { event ->
                if (longPressed) changes.addAll(event.changes)
            }
        )

        target.performTouchInput {
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis * 2)
            move() // send 0px move event to advance the clock
        }
        longPressed = true
        target.performTouchInput {
            moveBy(Offset(50f, 0f))
            up()
        }

        assertTrue(changes.all { it.isConsumed })
    }
}
