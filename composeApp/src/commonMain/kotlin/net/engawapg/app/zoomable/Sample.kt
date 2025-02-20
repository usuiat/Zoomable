package net.engawapg.app.zoomable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset

@Immutable
data class Sample(
    val title: String,
    val content: @Composable (onTap: (Offset) -> Unit, onLongPress: (Offset) -> Unit) -> Unit,
)

fun samples(): Array<Sample> = arrayOf(
    Sample("Sync Image") { onTap, onLongPress -> SyncImageSample(onTap, onLongPress) },
    Sample("Async Image") { onTap, onLongPress -> AsyncImageSample(onTap, onLongPress) },
    Sample("Text") { onTap, onLongPress -> TextSample(onTap, onLongPress) },
    Sample("HorizontalPager\n(Androidx)") { onTap, onLongPress ->
        AndroidxHorizontalPagerSample(onTap, onLongPress)
    },
    Sample("VerticalPager\n(Androidx)") { onTap, onLongPress ->
        AndroidxVerticalPagerSample(onTap, onLongPress)
    },
)
