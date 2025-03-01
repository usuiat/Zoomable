package net.engawapg.lib.zoomable

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.test.TouchInjectionScope

val ViewConfiguration.doubleTapDelay: Long
    get() = (doubleTapMinTimeMillis + doubleTapTimeoutMillis) / 2

fun TouchInjectionScope.pinchZoom(zoom: Float) {
    val startDistance = 100f
    down(0, center + Offset(startDistance / 2, 0f))
    down(1, center - Offset(startDistance / 2, 0f))
    val endDistance = startDistance * zoom
    moveTo(0, center + Offset(endDistance / 2, 0f))
    moveTo(1, center - Offset(endDistance / 2, 0f))
    up(0)
    up(1)
}

fun TouchInjectionScope.tapAndDragZoom(zoom: Float) {
    down(center)
    up()
    advanceEventTime(viewConfiguration.doubleTapDelay)
    down(center)
    val y = (zoom - 1f) / 0.004f
    moveBy(Offset(0f, y))
    up()
}
