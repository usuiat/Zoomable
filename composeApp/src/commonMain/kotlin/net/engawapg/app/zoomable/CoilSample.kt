package net.engawapg.app.zoomable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import coil3.compose.rememberAsyncImagePainter
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun CoilSample(
    settings: Settings, onTap: (Offset) -> Unit, onLongPress: (Offset) -> Unit,
    onLongPressReleased: (Offset) -> Unit,
) {
    val painter = rememberAsyncImagePainter(model = "https://github.com/usuiat.png")
    val zoomState = rememberZoomState(
        contentSize = painter.intrinsicSize,
        initialScale = settings.initialScale,
    )
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
                onTap = onTap,
                onLongPress = onLongPress,
                onLongPressReleased = onLongPressReleased,
                mouseWheelZoom = settings.mouseWheelZoom,
            ),
    )
}
