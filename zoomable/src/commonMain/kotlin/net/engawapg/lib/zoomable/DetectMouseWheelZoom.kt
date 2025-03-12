package net.engawapg.lib.zoomable

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.PointerKeyboardModifiers

internal suspend fun PointerInputScope.detectMouseWheelZoom(
    canZoom: (keyboardModifiers: PointerKeyboardModifiers) -> Boolean,
    onZoom: (zoom: Float, position: Offset) -> Unit,
) = awaitPointerEventScope {
    while (true) {
        val event = awaitPointerEvent()
        if (event.type != PointerEventType.Scroll) continue

        if (!canZoom(event.keyboardModifiers)) continue

        val change = event.changes[0]
        val zoom = 1f - change.scrollDelta.y * 0.1f
        if (zoom == 1f) continue

        onZoom(zoom, change.position)
        change.consume()
    }
}
