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
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
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
 * @param cancelIfZoomCanceled If true, cancel gesture when the number of fingers become one.
 * @param enableOneFingerZoom If true, enable one finger zoom gesture, double tap followed by
 * vertical scrolling.
 * @param canConsumeGesture Lambda that asks the caller whether the gesture can be consumed.
 * @param onGesture This lambda is called when [canConsumeGesture] returns true.
 * @param onGestureStart This lambda is called when a gesture starts.
 * @param onGestureEnd This lambda is called when a gesture ends.
 * @param onTap will be called when single tap is detected.
 * @param onDoubleTap will be called when double tap is detected.
 * @param onLongPress will be called when time elapses without the pointer moving
 */
internal suspend fun PointerInputScope.detectZoomableGestures(
    cancelIfZoomCanceled: () -> Boolean,
    enableOneFingerZoom: () -> Boolean,
    canConsumeGesture: (pan: Offset, zoom: Float) -> Boolean,
    onGesture: (centroid: Offset, pan: Offset, zoom: Float, timeMillis: Long) -> Unit,
    onGestureStart: () -> Unit = {},
    onGestureEnd: () -> Unit = {},
    onTap: ((position: Offset) -> Unit)? = null,
    onDoubleTap: ((position: Offset) -> Unit)? = null,
    onLongPress: ((position: Offset) -> Unit)? = null,
) = awaitEachGesture {
    val firstDown = awaitFirstDown(requireUnconsumed = false)
    if (onTap != null || onDoubleTap != null || onLongPress != null || enableOneFingerZoom()) {
        firstDown.consume()
    }
    onGestureStart()
    detectGesture(
        cancelIfZoomCanceled = cancelIfZoomCanceled,
        canConsumeGesture = canConsumeGesture,
        onGesture = onGesture,
        onTap = onTap,
        onDoubleTap = onDoubleTap,
        onLongPress = onLongPress,
        enableOneFingerZoom = enableOneFingerZoom,
    )
    onGestureEnd()
}

private suspend fun AwaitPointerEventScope.detectGesture(
    cancelIfZoomCanceled: () -> Boolean,
    enableOneFingerZoom: () -> Boolean,
    canConsumeGesture: (pan: Offset, zoom: Float) -> Boolean,
    onGesture: (centroid: Offset, pan: Offset, zoom: Float, timeMillis: Long) -> Unit,
    onTap: ((position: Offset) -> Unit)?,
    onDoubleTap: ((position: Offset) -> Unit)?,
    onLongPress: ((position: Offset) -> Unit)?,
) {
    val startPosition = currentEvent.changes[0].position
    var event = try {
        withTimeout(viewConfiguration.longPressTimeoutMillis) {
            awaitTouchSlop()
        } ?: return
    } catch (_: PointerEventTimeoutCancellationException) {
        onLongPress?.invoke(startPosition)
        consumeAllEventsUntilReleased()
        return
    }

    var hasMoved = false
    while (event.isPressed) {
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
        if (cancelIfZoomCanceled() && event.isPointerReducedToOne) {
            break
        }
        event = awaitEvent() ?: return
    }
    if (hasMoved) {
        return
    }
    val firstUp = event.changes[0]

    if (onDoubleTap == null && !enableOneFingerZoom()) {
        onTap?.invoke(firstUp.position)
        return
    }

    val secondDown = awaitSecondDown(firstUp)
    if (secondDown == null) {
        onTap?.invoke(firstUp.position)
        return
    }
    secondDown.consume()

    event = awaitTouchSlop() ?: return
    if (!event.isPressed) {
        val pressedTime = event.changes[0].uptimeMillis - secondDown.uptimeMillis
        if (pressedTime < viewConfiguration.longPressTimeoutMillis) {
            onDoubleTap?.invoke(event.changes[0].position)
        }
        return
    }

    if (!enableOneFingerZoom()) return

    while (event.isPressed) {
        // Vertical scrolling following a double tap is treated as a zoom gesture.
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
        event = awaitEvent() ?: return
    }
}

private suspend fun AwaitPointerEventScope.awaitTouchSlop(): PointerEvent? {
    val touchSlop = TouchSlop(viewConfiguration.touchSlop)
    while (true) {
        val mainEvent = awaitPointerEvent(pass = PointerEventPass.Main)
        if (mainEvent.changes.fastAny { it.isConsumed }) {
            return null // canceled
        }

        if (mainEvent.changes.none { it.pressed }) {
            return mainEvent // all pointers are up
        }

        if (touchSlop.isPast(mainEvent)) {
            return mainEvent
        }

        val finalEvent = awaitPointerEvent(pass = PointerEventPass.Final)
        if (finalEvent.changes.fastAny { it.isConsumed }) {
            return null // canceled
        }
    }
}

private suspend fun AwaitPointerEventScope.awaitEvent(): PointerEvent? {
    val mainEvent = awaitPointerEvent(pass = PointerEventPass.Main)
    if (mainEvent.changes.fastAny { it.isConsumed }) {
        return null // canceled
    }

    return mainEvent
}

private suspend fun AwaitPointerEventScope.consumeAllEventsUntilReleased() {
    do {
        val event = awaitEvent() ?: return
        event.changes.fastForEach { it.consume() }
    } while (event.isPressed)
}

private val PointerEvent.isPressed
    get() = changes.fastAny { it.pressed }

private val PointerEvent.isPointerReducedToOne
    get() = changes.count { it.previousPressed } > 1 && changes.count { it.pressed } == 1

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
