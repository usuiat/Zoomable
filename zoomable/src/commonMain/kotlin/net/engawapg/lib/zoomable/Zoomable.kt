/*
 * Copyright 2022 usuiat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.engawapg.lib.zoomable

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.launch

/**
 * [ScrollGesturePropagation] defines when [Modifier.zoomable] propagates scroll gestures to the
 * parent composable element.
 */
public enum class ScrollGesturePropagation {

    /**
     * Propagates the scroll gesture to the parent composable element when the content is scrolled
     * to the edge and attempts to scroll further.
     */
    ContentEdge,

    /**
     * Propagates the scroll gesture to the parent composable element when the content is not zoomed.
     */
    NotZoomed,
}

/**
 * A modifier function that allows content to be zoomable.
 *
 * @param zoomState A [ZoomState] object.
 * @param zoomEnabled specifies if zoom behaviour is enabled or disabled. Even if this is false,
 * [onTap] and [onDoubleTap] will be called.
 * @param enableOneFingerZoom If true, enable one finger zoom gesture, double tap followed by
 * vertical scrolling.
 * @param scrollGesturePropagation specifies when scroll gestures are propagated to the parent
 * composable element.
 * @param onTap will be called when single tap is detected on the element.
 * @param onDoubleTap will be called when double tap is detected on the element. This is a suspend
 * function and called in a coroutine scope. The default is to toggle the scale between 1.0f and
 * 2.5f with animation.
 * @param onLongPress will be called when time elapses without the pointer moving
 */
public fun Modifier.zoomable(
    zoomState: ZoomState,
    zoomEnabled: Boolean = true,
    enableOneFingerZoom: Boolean = true,
    scrollGesturePropagation: ScrollGesturePropagation = ScrollGesturePropagation.ContentEdge,
    onTap: (position: Offset) -> Unit = {},
    onDoubleTap: suspend (
        position: Offset,
    ) -> Unit = { position -> if (zoomEnabled) zoomState.toggleScale(2.5f, position) },
    onLongPress: (position: Offset) -> Unit = {},
): Modifier = this then ZoomableElement(
    zoomState = zoomState,
    zoomEnabled = zoomEnabled,
    enableOneFingerZoom = enableOneFingerZoom,
    snapBackEnabled = false,
    scrollGesturePropagation = scrollGesturePropagation,
    onTap = onTap,
    onDoubleTap = onDoubleTap,
    onLongPress = onLongPress,
)

/**
 * A modifier function that allows content to be zoomable and automatically return to its original size when the finger is released.
 *
 * @param zoomState A [ZoomState] object.
 * @param zoomEnabled specifies if zoom behaviour is enabled or disabled. Even if this is false,
 * [onTap] and [onDoubleTap] will be called.
 * @param onTap will be called when single tap is detected on the element.
 * @param onDoubleTap will be called when double tap is detected on the element. This is a suspend
 * function and called in a coroutine scope. The default is to toggle the scale between 1.0f and
 * 2.5f with animation.
 * @param onLongPress will be called when time elapses without the pointer moving
 */
public fun Modifier.snapBackZoomable(
    zoomState: ZoomState,
    zoomEnabled: Boolean = true,
    onTap: (position: Offset) -> Unit = {},
    onDoubleTap: suspend (position: Offset) -> Unit = {},
    onLongPress: (position: Offset) -> Unit = {},
): Modifier = this then ZoomableElement(
    zoomState = zoomState,
    zoomEnabled = zoomEnabled,
    enableOneFingerZoom = false,
    snapBackEnabled = true,
    scrollGesturePropagation = ScrollGesturePropagation.NotZoomed,
    onTap = onTap,
    onDoubleTap = onDoubleTap,
    onLongPress = onLongPress,
)

private data class ZoomableElement(
    val zoomState: ZoomState,
    val zoomEnabled: Boolean,
    val enableOneFingerZoom: Boolean,
    val snapBackEnabled: Boolean,
    val scrollGesturePropagation: ScrollGesturePropagation,
    val onTap: (position: Offset) -> Unit,
    val onDoubleTap: suspend (position: Offset) -> Unit,
    val onLongPress: (position: Offset) -> Unit,
) : ModifierNodeElement<ZoomableNode>() {
    override fun create(): ZoomableNode = ZoomableNode(
        zoomState,
        zoomEnabled,
        enableOneFingerZoom,
        snapBackEnabled,
        scrollGesturePropagation,
        onTap,
        onDoubleTap,
        onLongPress,
    )

    override fun update(node: ZoomableNode) {
        node.update(
            zoomState,
            zoomEnabled,
            enableOneFingerZoom,
            snapBackEnabled,
            scrollGesturePropagation,
            onTap,
            onDoubleTap,
            onLongPress,
        )
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "zoomable"
        properties["zoomState"] = zoomState
        properties["zoomEnabled"] = zoomEnabled
        properties["enableOneFingerZoom"] = enableOneFingerZoom
        properties["snapBackEnabled"] = snapBackEnabled
        properties["scrollGesturePropagation"] = scrollGesturePropagation
        properties["onTap"] = onTap
        properties["onDoubleTap"] = onDoubleTap
        properties["onLongPress"] = onLongPress
    }
}

private class ZoomableNode(
    var zoomState: ZoomState,
    var zoomEnabled: Boolean,
    var enableOneFingerZoom: Boolean,
    var snapBackEnabled: Boolean,
    var scrollGesturePropagation: ScrollGesturePropagation,
    var onTap: (position: Offset) -> Unit,
    var onDoubleTap: suspend (position: Offset) -> Unit,
    var onLongPress: (position: Offset) -> Unit,
) : PointerInputModifierNode, LayoutModifierNode, DelegatingNode() {
    var measuredSize = Size.Zero

    fun update(
        zoomState: ZoomState,
        zoomEnabled: Boolean,
        enableOneFingerZoom: Boolean,
        snapBackEnabled: Boolean,
        scrollGesturePropagation: ScrollGesturePropagation,
        onTap: (position: Offset) -> Unit,
        onDoubleTap: suspend (position: Offset) -> Unit,
        onLongPress: (position: Offset) -> Unit,
    ) {
        if (this.zoomState != zoomState) {
            zoomState.setLayoutSize(measuredSize)
            this.zoomState = zoomState
        }
        this.zoomEnabled = zoomEnabled
        this.enableOneFingerZoom = enableOneFingerZoom
        this.scrollGesturePropagation = scrollGesturePropagation
        this.snapBackEnabled = snapBackEnabled
        this.onTap = onTap
        this.onDoubleTap = onDoubleTap
        this.onLongPress = onLongPress
    }

    val pointerInputNode = delegate(
        SuspendingPointerInputModifierNode {
            detectZoomableGestures(
                cancelIfZoomCanceled = { snapBackEnabled },
                onGestureStart = {
                    resetConsumeGesture()
                    zoomState.startGesture()
                },
                canConsumeGesture = { pan, zoom ->
                    zoomEnabled && canConsumeGesture(pan, zoom)
                },
                onGesture = { centroid, pan, zoom, timeMillis ->
                    if (zoomEnabled) {
                        coroutineScope.launch {
                            zoomState.applyGesture(
                                pan = pan,
                                zoom = zoom,
                                position = centroid,
                                timeMillis = timeMillis,
                            )
                        }
                    }
                },
                onGestureEnd = {
                    coroutineScope.launch {
                        if (snapBackEnabled || zoomState.scale < 1f) {
                            zoomState.changeScale(1f, Offset.Zero)
                        } else {
                            zoomState.startFling()
                        }
                    }
                },
                onTap = { onTap(it) },
                onDoubleTap = { position ->
                    coroutineScope.launch {
                        onDoubleTap(position)
                    }
                },
                onLongPress = { onLongPress(it) },
                enableOneFingerZoom = { enableOneFingerZoom },
            )
        }
    )

    private var consumeGesture: Boolean? = null

    private fun resetConsumeGesture() {
        consumeGesture = null
    }

    private fun canConsumeGesture(pan: Offset, zoom: Float): Boolean {
        val currentValue = consumeGesture
        if (currentValue != null) {
            return currentValue
        }

        val newValue = when {
            zoom != 1f -> true
            zoomState.scale == 1f -> false
            scrollGesturePropagation == ScrollGesturePropagation.NotZoomed -> true
            else -> zoomState.willChangeOffset(pan)
        }
        consumeGesture = newValue
        return newValue
    }

    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize,
    ) {
        pointerInputNode.onPointerEvent(pointerEvent, pass, bounds)
    }

    override fun onCancelPointerInput() {
        pointerInputNode.onCancelPointerInput()
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        measuredSize = IntSize(placeable.measuredWidth, placeable.measuredHeight).toSize()
        zoomState.setLayoutSize(measuredSize)
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(x = 0, y = 0) {
                scaleX = zoomState.scale
                scaleY = zoomState.scale
                translationX = zoomState.offsetX
                translationY = zoomState.offsetY
            }
        }
    }
}

/**
 * Toggle the scale between [targetScale] and 1.0f.
 *
 * @param targetScale Scale to be set if this function is called when the scale is 1.0f.
 * @param position Zoom around this point.
 * @param animationSpec The animation configuration.
 */
public suspend fun ZoomState.toggleScale(
    targetScale: Float,
    position: Offset,
    animationSpec: AnimationSpec<Float> = spring(),
) {
    val newScale = if (scale == 1f) targetScale else 1f
    changeScale(newScale, position, animationSpec)
}
