package net.engawapg.app.zoomable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset

@Immutable
data class Sample(
    val title: String,
    val content: @Composable (onTap: (Offset) -> Unit) -> Unit
)

fun samples(): Array<Sample> = arrayOf(
        Sample("Sync Image") { SyncImageSample(it) },
        Sample("Async Image") { AsyncImageSample(it) },
        Sample("Text") { TextSample(it) },
        Sample("HorizontalPager\n(Androidx)") { AndroidxHorizontalPagerSample(it) },
        Sample("VerticalPager\n(Androidx)") { AndroidxVerticalPagerSample(it) },
    )
