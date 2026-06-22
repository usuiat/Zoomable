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

@file:OptIn(ExperimentalZoomableApi::class)

package net.engawapg.lib.zoomable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
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
 */
@Stable
internal class ZoomablePopupState {
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
internal fun rememberZoomablePopupState(): ZoomablePopupState = remember { ZoomablePopupState() }

/**
 * Controller that lets [SnapBackZoomableBox] instances render their zoomed
 * content inside a shared overlay that lives in the same window as the host.
 *
 * Unlike the [Popup]-based overlay used when no host is present, an overlay
 * hosted inside [SnapBackZoomableOverlayHost] is free to extend across system
 * bar areas on platforms (notably Android) where the popup's native surface
 * is otherwise capped to the window's visible display frame.
 */
@Stable
internal class SnapBackZoomableOverlayController {
    internal var hostCoordinates: LayoutCoordinates? by mutableStateOf(null)
    internal val entries: SnapshotStateList<SnapBackZoomableOverlayEntry> =
        mutableStateListOf()
}

internal class SnapBackZoomableOverlayEntry(
    val zoomState: ZoomState,
    val scrim: Color,
    val content: @Composable () -> Unit,
) {
    var anchorCoordinates: LayoutCoordinates? by mutableStateOf(null)
}

internal val LocalSnapBackZoomableOverlayController =
    staticCompositionLocalOf<SnapBackZoomableOverlayController?> { null }

/**
 * Provides a shared overlay for every [SnapBackZoomableBox] nested inside
 * [content]. Place this at (or near) the root of your compose tree so the
 * overlay can extend across the entire window, including system bar areas.
 *
 * When a host is present each [SnapBackZoomableBox] renders its zoomed content
 * as a Compose overlay inside this host's [Box] instead of a platform
 * [Popup] — so the overlay and its [scrim] cover the full host area.
 *
 * Example:
 * ```
 * SnapBackZoomableOverlayHost(modifier = Modifier.fillMaxSize()) {
 *     Scaffold { padding ->
 *         LazyColumn(contentPadding = padding) {
 *             items(images) { image -> SnapBackZoomableBox { /* content */ } }
 *         }
 *     }
 * }
 * ```
 *
 * @param modifier The modifier applied to the host [Box].
 * @param content The screen content that can contain [SnapBackZoomableBox]es.
 */
@ExperimentalZoomableApi
@Composable
public fun SnapBackZoomableOverlayHost(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val controller = remember { SnapBackZoomableOverlayController() }
    CompositionLocalProvider(LocalSnapBackZoomableOverlayController provides controller) {
        Box(
            modifier = modifier.onGloballyPositioned { controller.hostCoordinates = it },
        ) {
            content()
            controller.entries.forEach { entry ->
                key(entry) {
                    HostedZoomOverlay(entry, controller.hostCoordinates)
                }
            }
        }
    }
}

/**
 * A container that makes its [content] zoomable with snap-back behavior while
 * allowing the zoomed content to escape the clipping bounds of its parent.
 *
 * Pinch zoom and pan gestures are supported; when the pointers are released the
 * content animates back to its original size and position.
 *
 * While a gesture is in progress (including the snap-back animation), [content]
 * is rendered as an overlay over a [scrim] background. If this box is nested
 * inside a [SnapBackZoomableOverlayHost], the overlay lives in that host (and
 * may extend across system bar areas); otherwise it falls back to a platform
 * [Popup].
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
 * @param scrim Color painted behind the zoomed content. Its alpha is multiplied
 * by the zoom progress so the scrim fades in as the user zooms past 1x and
 * fades out during the snap-back animation. Pass [Color.Transparent] to disable.
 * @param onTap Called when a single tap is detected.
 * @param content The zoomable content.
 */
@ExperimentalZoomableApi
@Composable
public fun SnapBackZoomableBox(
    modifier: Modifier = Modifier,
    zoomState: ZoomState = rememberZoomState(),
    scrim: Color = Color.Black.copy(alpha = 0.6f),
    onTap: ((position: Offset) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    // Compose [content] once and move the same subtree between the anchor and the
    // overlay so its state and side effects (e.g. AsyncImage requests) aren't duplicated.
    val movableContent = remember(content) { movableContentOf(content) }

    val controller = LocalSnapBackZoomableOverlayController.current
    if (controller != null) {
        HostedSnapBackZoomableBox(
            modifier = modifier,
            zoomState = zoomState,
            controller = controller,
            scrim = scrim,
            onTap = onTap,
            content = movableContent,
        )
    } else {
        PopupSnapBackZoomableBox(
            modifier = modifier,
            zoomState = zoomState,
            scrim = scrim,
            onTap = onTap,
            content = movableContent,
        )
    }
}

@Composable
private fun HostedSnapBackZoomableBox(
    modifier: Modifier,
    zoomState: ZoomState,
    controller: SnapBackZoomableOverlayController,
    scrim: Color,
    onTap: ((position: Offset) -> Unit)?,
    content: @Composable () -> Unit,
) {
    val entry = remember(zoomState, scrim, content) {
        SnapBackZoomableOverlayEntry(zoomState, scrim, content)
    }
    DisposableEffect(controller, entry) {
        controller.entries.add(entry)
        onDispose { controller.entries.remove(entry) }
    }

    var anchorSize by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = modifier
            .onGloballyPositioned { entry.anchorCoordinates = it }
            .snapBackZoomable(zoomState = zoomState, onTap = onTap)
            .onSizeChanged { anchorSize = it },
    ) {
        if (!zoomState.isActive) {
            content()
        } else {
            ReservedSpace(anchorSize)
        }
    }
}

/**
 * Returns the scrim opacity factor for the given [scale], fading the scrim in from fully
 * transparent at rest (scale 1) to its configured alpha once the content has scaled up.
 */
internal fun scrimAlphaForScale(scale: Float): Float = ((scale - 1f) * 2f).coerceIn(0f, 1f)

@Composable
private fun rememberScrimAlpha(zoomState: ZoomState): State<Float> = remember(zoomState) {
    derivedStateOf {
        scrimAlphaForScale(zoomState.scale)
    }
}

@Composable
private fun HostedZoomOverlay(
    entry: SnapBackZoomableOverlayEntry,
    hostCoords: LayoutCoordinates?,
) {
    val zoomState = entry.zoomState
    // The overlay stays visible while the gesture is active, which includes the
    // snap-back animation, so the underlying content does not flash into view
    // before the content has settled back to its resting state.
    if (!zoomState.isActive) return
    val anchorCoords = entry.anchorCoordinates
    if (hostCoords == null || anchorCoords == null) return
    if (!hostCoords.isAttached || !anchorCoords.isAttached) return

    val anchorOffset = hostCoords.localPositionOf(anchorCoords, Offset.Zero)
    val anchorSize = anchorCoords.size
    val scrimAlpha by rememberScrimAlpha(zoomState)

    // Scrim spans the whole host.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(entry.scrim.copy(alpha = entry.scrim.alpha * scrimAlpha)),
    )

    // Anchor-sized, scaled content placed at the anchor's position within the host.
    Box(
        modifier = Modifier.layout { measurable, constraints ->
            val w = anchorSize.width.coerceAtLeast(1)
            val h = anchorSize.height.coerceAtLeast(1)
            val placeable = measurable.measure(Constraints.fixed(w, h))
            val layoutW = if (constraints.hasBoundedWidth) constraints.maxWidth else w
            val layoutH = if (constraints.hasBoundedHeight) constraints.maxHeight else h
            layout(layoutW, layoutH) {
                placeable.place(
                    anchorOffset.x.roundToInt(),
                    anchorOffset.y.roundToInt(),
                )
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
            entry.content()
        }
    }
}

@Composable
private fun PopupSnapBackZoomableBox(
    modifier: Modifier,
    zoomState: ZoomState,
    scrim: Color,
    onTap: ((position: Offset) -> Unit)?,
    content: @Composable () -> Unit,
) {
    val popupState = rememberZoomablePopupState()
    var anchorSize by remember { mutableStateOf(IntSize.Zero) }
    Box {
        if (zoomState.isActive) {
            ZoomablePopup(
                popupState = popupState,
                zoomState = zoomState,
                scrim = scrim,
                content = content,
            )
        }

        Box(
            modifier = modifier
                .onGloballyPositioned { popupState.onAnchorPositioned(it) }
                .snapBackZoomable(zoomState = zoomState, onTap = onTap)
                .onSizeChanged { anchorSize = it },
        ) {
            if (!zoomState.isActive) {
                content()
            } else {
                ReservedSpace(anchorSize)
            }
        }
    }
}

/**
 * An empty layout that occupies [size] pixels, used to hold the anchor's place
 * while its content has been moved into the overlay.
 */
@Composable
private fun ReservedSpace(size: IntSize) {
    Box(
        modifier = Modifier.layout { _, _ ->
            layout(size.width, size.height) {}
        },
    )
}

@Composable
private fun ZoomablePopup(
    popupState: ZoomablePopupState,
    zoomState: ZoomState,
    scrim: Color,
    content: @Composable () -> Unit,
) {
    val scrimAlpha by rememberScrimAlpha(zoomState)
    Popup(
        popupPositionProvider = popupState.positionProvider,
        onDismissRequest = {},
        properties = PopupProperties(focusable = false, clippingEnabled = false),
    ) {
        // Fill the popup window and place the content at the anchor position so
        // that graphicsLayer transforms can extend past the anchor bounds without
        // being clipped by the popup's content measurement.
        Box(
            modifier = Modifier
                .drawBehind {
                    drawRect(scrim.copy(alpha = scrim.alpha * scrimAlpha))
                }
                .layout { measurable, _ ->
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
