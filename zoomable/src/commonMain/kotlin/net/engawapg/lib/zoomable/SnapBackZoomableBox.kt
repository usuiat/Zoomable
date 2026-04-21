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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt

/**
 * A state object that tracks the anchor position and window size required to
 * render a [SnapBackZoomableBox] in a [Popup] overlay.
 *
 * Usually you do not need to instantiate this yourself; use [rememberZoomablePopupState].
 */
@Stable
public class ZoomablePopupState {
    internal var anchorWindowBounds by mutableStateOf(IntRect(0, 0, 0, 0))
        private set

    internal var windowSize by mutableStateOf(IntSize.Zero)
        private set

    internal val positionProvider: PopupPositionProvider = object : PopupPositionProvider {
        override fun calculatePosition(
            anchorBounds: IntRect,
            windowSize: IntSize,
            layoutDirection: LayoutDirection,
            popupContentSize: IntSize,
        ): IntOffset {
            this@ZoomablePopupState.windowSize = windowSize
            return IntOffset.Zero
        }
    }

    internal fun onAnchorPositioned(coords: LayoutCoordinates) {
        val bounds = coords.boundsInWindow()
        anchorWindowBounds = IntRect(
            left = bounds.left.roundToInt(),
            top = bounds.top.roundToInt(),
            right = bounds.right.roundToInt(),
            bottom = bounds.bottom.roundToInt(),
        )
    }
}

/**
 * Creates a [ZoomablePopupState] that is remembered across compositions.
 */
@Composable
public fun rememberZoomablePopupState(): ZoomablePopupState = remember { ZoomablePopupState() }

/**
 * A container that makes its [content] zoomable with snap-back behavior while
 * allowing the zoomed content to escape the clipping bounds of its parent.
 *
 * Pinch zoom and pan gestures are supported; when the pointers are released the
 * content animates back to its original size and position.
 *
 * While a gesture is in progress (including the snap-back animation), [content]
 * is rendered in a [Popup] overlay that covers the entire window. This allows the
 * zoomed content to be displayed above clipping parents such as
 * `HorizontalPager`, `LazyColumn`, or `Card`.
 *
 * Example:
 * ```
 * SnapBackZoomableBox(modifier = Modifier.fillMaxSize()) {
 *     AsyncImage(model = url, modifier = Modifier.fillMaxSize())
 * }
 * ```
 *
 * @param modifier The modifier applied to the anchor area (the original touch target).
 * @param zoomState A [ZoomState] object. Defaults to [rememberZoomState].
 * @param popupState A [ZoomablePopupState] object. Defaults to [rememberZoomablePopupState].
 * @param onTap Called when a single tap is detected.
 * @param content The zoomable content.
 */
@Composable
public fun SnapBackZoomableBox(
    modifier: Modifier = Modifier,
    zoomState: ZoomState = rememberZoomState(),
    popupState: ZoomablePopupState = rememberZoomablePopupState(),
    onTap: ((position: Offset) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val anchorAlpha by animateFloatAsState(
        targetValue = if (zoomState.isActive) 0f else 1f,
        label = "anchorAlpha",
    )

    Box {
        if (zoomState.isActive || anchorAlpha < 1f) {
            ZoomablePopup(
                popupState = popupState,
                zoomState = zoomState,
                content = content,
            )
        }

        Box(
            modifier = modifier
                .onGloballyPositioned { popupState.onAnchorPositioned(it) }
                .snapBackZoomable(zoomState = zoomState, onTap = onTap)
                .graphicsLayer { alpha = anchorAlpha },
        ) {
            content()
        }
    }
}

@Composable
private fun ZoomablePopup(
    popupState: ZoomablePopupState,
    zoomState: ZoomState,
    content: @Composable () -> Unit,
) {
    Popup(
        popupPositionProvider = popupState.positionProvider,
        onDismissRequest = {},
        properties = PopupProperties(focusable = false, clippingEnabled = false),
    ) {
        // Fill the popup window and place the content at the anchor position so
        // that graphicsLayer transforms can extend past the anchor bounds without
        // being clipped by the popup's content measurement.
        Box(
            modifier = Modifier.layout { measurable, _ ->
                val anchor = popupState.anchorWindowBounds
                val window = popupState.windowSize
                val anchorWidth = anchor.width.coerceAtLeast(1)
                val anchorHeight = anchor.height.coerceAtLeast(1)
                val windowWidth = window.width.coerceAtLeast(anchorWidth)
                val windowHeight = window.height.coerceAtLeast(anchorHeight)
                val placeable = measurable.measure(
                    Constraints(
                        minWidth = anchorWidth,
                        maxWidth = anchorWidth,
                        minHeight = anchorHeight,
                        maxHeight = anchorHeight,
                    ),
                )
                layout(windowWidth, windowHeight) {
                    placeable.place(anchor.left, anchor.top)
                }
            },
        ) {
            Box(
                modifier = Modifier.graphicsLayer {
                    scaleX = zoomState.scale
                    scaleY = zoomState.scale
                    translationX = zoomState.offsetX
                    translationY = zoomState.offsetY
                },
            ) {
                content()
            }
        }
    }
}
