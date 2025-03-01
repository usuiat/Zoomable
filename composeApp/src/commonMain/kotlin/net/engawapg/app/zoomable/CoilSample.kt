package net.engawapg.app.zoomable

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun CoilSample(settings: Settings, onTap: (Offset) -> Unit, onLongPress: (Offset) -> Unit) {
    val zoomState = rememberZoomState()
    AsyncImage(
        model = "https://github.com/usuiat.png",
        contentDescription = "GitHub icon",
        contentScale = ContentScale.Fit,
        onSuccess = { state ->
            zoomState.setContentSize(state.painter.intrinsicSize)
        },
        modifier = Modifier
            .fillMaxSize()
            .zoomable(
                zoomState = zoomState,
                zoomEnabled = settings.zoomEnabled,
                enableOneFingerZoom = settings.enableOneFingerZoom,
                onTap = onTap,
                onLongPress = onLongPress,
            ),
    )
}
