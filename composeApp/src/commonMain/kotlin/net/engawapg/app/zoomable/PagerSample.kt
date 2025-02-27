package net.engawapg.app.zoomable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import org.jetbrains.compose.resources.painterResource
import zoomable_root.composeapp.generated.resources.Res
import zoomable_root.composeapp.generated.resources.duck1
import zoomable_root.composeapp.generated.resources.duck2
import zoomable_root.composeapp.generated.resources.duck3

@Composable
fun PagerSample(settings: Settings, onTap: (Offset) -> Unit, onLongPress: (Offset) -> Unit) {
    val resources = listOf(Res.drawable.duck1, Res.drawable.duck2, Res.drawable.duck3)
    val pagerState = rememberPagerState { resources.size }
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val painter = painterResource(resource = resources[page])
        val zoomState = rememberZoomState(contentSize = painter.intrinsicSize)
        Image(
            painter = painter,
            contentDescription = "Zoomable image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .zoomable(
                    zoomState = zoomState,
                    zoomEnabled = settings.zoomEnabled,
                    enableOneFingerZoom = settings.enableOneFingerZoom,
                    scrollGesturePropagation = settings.scrollGesturePropagation,
                    onTap = onTap,
                    onLongPress = onLongPress,
                )
        )
    }
}
