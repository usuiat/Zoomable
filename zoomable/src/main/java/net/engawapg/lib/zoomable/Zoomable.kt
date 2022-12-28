package net.engawapg.lib.zoomable

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.gestures.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

private suspend fun PointerInputScope.detectTransformGestures(
    panZoomLock: Boolean = false,
    onGesture: (centroid: Offset, pan: Offset, zoom: Float, rotation: Float, timeMillis: Long) -> Boolean,
    onGestureStart: () -> Unit = {},
    onGestureEnd: () -> Unit = {},
) {
    forEachGesture {
        awaitPointerEventScope {
            var rotation = 0f
            var zoom = 1f
            var pan = Offset.Zero
            var pastTouchSlop = false
            val touchSlop = viewConfiguration.touchSlop
            var lockedToPanZoom = false

            awaitFirstDown(requireUnconsumed = false)
            onGestureStart()
            do {
                val event = awaitPointerEvent()
                val canceled = event.changes.fastAny { it.isConsumed }
                if (!canceled) {
                    val zoomChange = event.calculateZoom()
                    val rotationChange = event.calculateRotation()
                    val panChange = event.calculatePan()

                    if (!pastTouchSlop) {
                        zoom *= zoomChange
                        rotation += rotationChange
                        pan += panChange

                        val centroidSize = event.calculateCentroidSize(useCurrent = false)
                        val zoomMotion = abs(1 - zoom) * centroidSize
                        val rotationMotion = abs(rotation * kotlin.math.PI.toFloat() * centroidSize / 180f)
                        val panMotion = pan.getDistance()

                        if (zoomMotion > touchSlop ||
                            rotationMotion > touchSlop ||
                            panMotion > touchSlop
                        ) {
                            pastTouchSlop = true
                            lockedToPanZoom = panZoomLock && rotationMotion < touchSlop
                        }
                    }

                    if (pastTouchSlop) {
                        val centroid = event.calculateCentroid(useCurrent = false)
                        val effectiveRotation = if (lockedToPanZoom) 0f else rotationChange
                        if (effectiveRotation != 0f ||
                            zoomChange != 1f ||
                            panChange != Offset.Zero
                        ) {
                            val isConsumed = onGesture(
                                centroid,
                                panChange,
                                zoomChange,
                                effectiveRotation,
                                event.changes[0].uptimeMillis
                            )
                            if (isConsumed) {
                                event.changes.fastForEach {
                                    if (it.positionChanged()) {
                                        it.consume()
                                    }
                                }
                            }
                        }
                    }
                }
            } while (!canceled && event.changes.fastAny { it.pressed })
            onGestureEnd()
        }
    }
}

@Stable
class ZoomState(
    private val maxScale: Float,
    private var imageSize: Size = Size.Zero
) {
    private var _scale = Animatable(1f).apply {
        updateBounds(0.9f, maxScale)
    }
    val scale: Float
        get() = _scale.value

    private var _offsetX = Animatable(0f)
    val offsetX: Float
        get() = _offsetX.value

    private var _offsetY = Animatable(0f)
    val offsetY: Float
        get() = _offsetY.value

    private var layoutSize = Size.Zero
    fun setLayoutSize(size: Size) {
        layoutSize = size
        updateFitImageSize()
    }

    fun setImageSize(size: Size) {
        imageSize = size
        updateFitImageSize()
    }

    private var fitImageSize = Size.Zero
    private fun updateFitImageSize() {
        if (layoutSize == Size.Zero) {
            fitImageSize = Size.Zero
            return
        }

        if (imageSize == Size.Zero) {
            fitImageSize = layoutSize
            return
        }

        val imageAspectRatio = imageSize.width / imageSize.height
        val layoutAspectRatio = layoutSize.width / layoutSize.height

        fitImageSize = if (imageAspectRatio > layoutAspectRatio) {
            imageSize * (layoutSize.width / imageSize.width)
        } else {
            imageSize * (layoutSize.height / imageSize.height)
        }
    }

    suspend fun reset() = coroutineScope {
        launch { _scale.snapTo(1f) }
        _offsetX.updateBounds(0f, 0f)
        launch { _offsetX.snapTo(0f) }
        _offsetY.updateBounds(0f, 0f)
        launch { _offsetY.snapTo(0f) }
    }

    private var shouldConsumeEvent: Boolean? = null

    internal fun startGesture() {
        shouldConsumeEvent = null
    }

    internal fun canConsumeGesture(pan: Offset, zoom: Float): Boolean {
        return shouldConsumeEvent ?: run {
            var consume = true
            if (zoom == 1f) { // One finger gesture
                if (scale == 1f) {  // Not zoomed
                    consume = false
                } else {
                    val ratio = (abs(pan.x) / abs(pan.y))
                    if (ratio > 3) {   // Horizontal drag
                        if ((pan.x < 0) && (_offsetX.value == _offsetX.lowerBound)) {
                            // Drag R to L when right edge of the content is shown.
                            consume = false
                        }
                        if ((pan.x > 0) && (_offsetX.value == _offsetX.upperBound)) {
                            // Drag L to R when left edge of the content is shown.
                            consume = false
                        }
                    }
                }
            }
            shouldConsumeEvent = consume
            consume
        }
    }

    private val velocityTracker = VelocityTracker()
    private var shouldFling = true

    internal suspend fun applyGesture(
        pan: Offset,
        zoom: Float,
        position: Offset,
        timeMillis: Long
    ) = coroutineScope {
        launch {
            _scale.snapTo(_scale.value * zoom)
        }

        val boundX = java.lang.Float.max((fitImageSize.width * _scale.value - layoutSize.width), 0f) / 2f
        _offsetX.updateBounds(-boundX, boundX)
        launch {
            _offsetX.snapTo(_offsetX.value + pan.x)
        }

        val boundY = java.lang.Float.max((fitImageSize.height * _scale.value - layoutSize.height), 0f) / 2f
        _offsetY.updateBounds(-boundY, boundY)
        launch {
            _offsetY.snapTo(_offsetY.value + pan.y)
        }

        velocityTracker.addPosition(timeMillis, position)

        if (zoom != 1f) {
            shouldFling = false
        }
    }

    private val velocityDecay = exponentialDecay<Float>()

    internal suspend fun endGesture() = coroutineScope {
        if (shouldFling) {
            val velocity = velocityTracker.calculateVelocity()
            launch {
                _offsetX.animateDecay(velocity.x, velocityDecay)
            }
            launch {
                _offsetY.animateDecay(velocity.y, velocityDecay)
            }
        }
        shouldFling = true

        if (_scale.value < 1f) {
            launch {
                _scale.animateTo(1f)
            }
        }
    }
}

@Composable
fun rememberZoomState(
    maxScale: Float,
    imageSize: Size = Size.Zero
) = remember {
    ZoomState(maxScale, imageSize)
}

fun Modifier.zoomable(zoomState: ZoomState): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "zoomable"
        properties["zoomState"] = zoomState
    }
) {
    val scope = rememberCoroutineScope()
    Modifier
        .onSizeChanged { size ->
            zoomState.setLayoutSize(size.toSize())
        }
        .pointerInput(Unit) {
            detectTransformGestures(
                onGestureStart = { zoomState.startGesture() },
                onGesture = { centroid, pan, zoom, _, timeMillis ->
                    val canConsume = zoomState.canConsumeGesture(pan = pan, zoom = zoom)
                    if (canConsume) {
                        scope.launch {
                            zoomState.applyGesture(
                                pan = pan,
                                zoom = zoom,
                                position = centroid,
                                timeMillis = timeMillis,
                            )
                        }
                    }
                    canConsume
                },
                onGestureEnd = {
                    scope.launch {
                        zoomState.endGesture()
                    }
                }
            )
        }
        .graphicsLayer {
            scaleX = zoomState.scale
            scaleY = zoomState.scale
            translationX = zoomState.offsetX
            translationY = zoomState.offsetY
        }
}
