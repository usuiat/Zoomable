package net.engawapg.lib.zoomable

import androidx.compose.ui.input.nestedscroll.NestedScrollConnection

/**
 * Default values used by [Modifier.zoomable()]
 */
object ZoomableDefaults {

    /**
     * pageNestedScrollConnection to be set to HorizontalPager or VerticalPager.
     * If [Modifier.zoomable()] is used in Pager's contents, this should be set to Pager's
     * pageNestedScrollConnection to enable Pager scroll.
     *
     * This implements nothing so that all scroll and fling events will reach the Pager.
     */
    val pageNestedScrollConnection = object : NestedScrollConnection {}
}