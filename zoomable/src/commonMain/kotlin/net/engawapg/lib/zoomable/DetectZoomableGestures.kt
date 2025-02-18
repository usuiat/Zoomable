package net.engawapg.lib.zoomable

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach

/**
 * Gesture detector for zoomable.
 *
 * A caller of this function can choose if the pointer events will be consumed.
 * And the caller can implement [onGestureStart] and [onGestureEnd] event.
 *
 * @param canConsumeGesture Lambda that asks the caller whether the gesture can be consumed.
 * @param onGesture This lambda is called when [canConsumeGesture] returns true.
 * @param onGestureStart This lambda is called when a gesture starts.
 * @param onGestureEnd This lambda is called when a gesture ends.
 * @param onTap will be called when single tap is detected.
 * @param onDoubleTap will be called when double tap is detected.
 * @param enableOneFingerZoom If true, enable one finger zoom gesture, double tap followed by
 * vertical scrolling.
 */
internal suspend fun PointerInputScope.detectZoomableGestures(
    cancelIfZoomCanceled: Boolean,
    canConsumeGesture: (pan: Offset, zoom: Float) -> Boolean,
    onGesture: (centroid: Offset, pan: Offset, zoom: Float, timeMillis: Long) -> Unit,
    onGestureStart: () -> Unit = {},
    onGestureEnd: () -> Unit = {},
    onTap: (position: Offset) -> Unit = {},
    onDoubleTap: (position: Offset) -> Unit = {},
    enableOneFingerZoom: Boolean = true,
) = awaitEachGesture {
    val firstDown = awaitFirstDown(requireUnconsumed = false)
    firstDown.consume()
    onGestureStart()

    var firstUp: PointerInputChange = firstDown
    var hasMoved = false
    var isMultiTouch = false
    var isLongPressed = false
    var isCanceled = false
    forEachPointerEventUntilReleased(
        onCancel = { isCanceled = true },
    ) { event, isTouchSlopPast ->
        if (isTouchSlopPast) {
            val zoomChange = event.calculateZoom()
            val panChange = event.calculatePan()
            if (zoomChange != 1f || panChange != Offset.Zero) {
                val centroid = event.calculateCentroid(useCurrent = true)
                val timeMillis = event.changes[0].uptimeMillis
                if (canConsumeGesture(panChange, zoomChange)) {
                    onGesture(centroid, panChange, zoomChange, timeMillis)
                    event.consumePositionChanges()
                }
            }
            hasMoved = true
        }
        if (event.changes.count { it.pressed } > 1) {
            isMultiTouch = true
        }
        firstUp = event.changes[0]
        val cancelGesture = cancelIfZoomCanceled && isMultiTouch && event.changes.count { it.pressed } == 1
        cancelGesture
    }

    if (firstUp.uptimeMillis - firstDown.uptimeMillis > viewConfiguration.longPressTimeoutMillis) {
        isLongPressed = true
    }

    if (hasMoved || isMultiTouch || isLongPressed || isCanceled) {
        onGestureEnd()
        return@awaitEachGesture
    }

    // Vertical scrolling following a double tap is treated as a zoom gesture.
    val secondDown = awaitSecondDown(firstUp)
    if (secondDown == null) {
        onTap(firstUp.position)
    } else {
        secondDown.consume()
        var isDoubleTap = true
        var isSecondCanceled = false
        var secondUp: PointerInputChange = secondDown
        forEachPointerEventUntilReleased(
            onCancel = { isSecondCanceled = true }
        ) { event, isTouchSlopPast ->
            if (isTouchSlopPast) {
                if (enableOneFingerZoom) {
                    val panChange = event.calculatePan()
                    val zoomChange = 1f + panChange.y * 0.004f
                    if (zoomChange != 1f) {
                        val centroid = event.calculateCentroid(useCurrent = true)
                        val timeMillis = event.changes[0].uptimeMillis
                        if (canConsumeGesture(Offset.Zero, zoomChange)) {
                            onGesture(centroid, Offset.Zero, zoomChange, timeMillis)
                            event.consumePositionChanges()
                        }
                    }
                }
                isDoubleTap = false
            }
            secondUp = event.changes[0]
            false
        }

        val secondPressedTime = secondUp.uptimeMillis - secondDown.uptimeMillis
        if (secondPressedTime > viewConfiguration.longPressTimeoutMillis) {
            isDoubleTap = false
        }

        if (isDoubleTap && !isSecondCanceled) {
            onDoubleTap(secondUp.position)
        }
    }
    onGestureEnd()
}

/**
 * Invoke action for each PointerEvent until all pointers are released.
 *
 * @param onCancel Callback function that will be called if PointerEvents is consumed by other composable.
 * @param action Callback function that will be called every PointerEvents occur.
 */
private suspend fun AwaitPointerEventScope.forEachPointerEventUntilReleased(
    onCancel: () -> Unit,
    action: (event: PointerEvent, isTouchSlopPast: Boolean) -> Boolean,
) {
    val touchSlop = TouchSlop(viewConfiguration.touchSlop)
    do {
        val mainEvent = awaitPointerEvent(pass = PointerEventPass.Main)
        if (mainEvent.changes.fastAny { it.isConsumed }) {
            onCancel()
            break
        }

        val isTouchSlopPast = touchSlop.isPast(mainEvent)
        val doBreak = action(mainEvent, isTouchSlopPast)
        if (doBreak) {
            break
        }
        if (isTouchSlopPast) {
            continue
        }

        val finalEvent = awaitPointerEvent(pass = PointerEventPass.Final)
        if (finalEvent.changes.fastAny { it.isConsumed }) {
            onCancel()
            break
        }
    } while (mainEvent.changes.fastAny { it.pressed })
}

/**
 * Await second down or timeout from first up
 *
 * @param firstUp The first up event
 * @return If the second down event comes before timeout, returns it. If not, returns null.
 */
private suspend fun AwaitPointerEventScope.awaitSecondDown(
    firstUp: PointerInputChange,
): PointerInputChange? = withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis) {
    val minUptime = firstUp.uptimeMillis + viewConfiguration.doubleTapMinTimeMillis
    var change: PointerInputChange
    // The second tap doesn't count if it happens before DoubleTapMinTime of the first tap
    do {
        change = awaitFirstDown()
    } while (change.uptimeMillis < minUptime)
    change
}

/**
 * Consume event if the position is changed.
 */
private fun PointerEvent.consumePositionChanges() {
    changes.fastForEach {
        if (it.positionChanged()) {
            it.consume()
        }
    }
}

/**
 * Touch slop detector.
 *
 * This class holds accumulated zoom and pan value to see if touch slop is past.
 *
 * @param threshold Threshold of movement of gesture after touch down. If the movement exceeds this
 * value, it is judged to be a swipe or zoom gesture.
 */
private class TouchSlop(private val threshold: Float) {
    private var pan = Offset.Zero
    private var past = false

    /**
     * Judge the touch slop is past.
     *
     * @param event Event that occurs this time.
     * @return True if the accumulated zoom or pan exceeds the threshold.
     */
    fun isPast(event: PointerEvent): Boolean {
        if (past) {
            return true
        }

        if (event.changes.size > 1) {
            // If there are two or more fingers, we determine the touch slop is past immediately.
            past = true
        } else {
            pan += event.calculatePan()
            past = pan.getDistance() > threshold
        }

        return past
    }
}
