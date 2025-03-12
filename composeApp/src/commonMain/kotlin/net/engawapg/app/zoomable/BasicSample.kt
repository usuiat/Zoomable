package net.engawapg.app.zoomable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import org.jetbrains.compose.resources.painterResource
import zoomable_root.composeapp.generated.resources.Res
import zoomable_root.composeapp.generated.resources.penguin

@Composable
fun BasicSample(settings: Settings, onTap: (Offset) -> Unit, onLongPress: (Offset) -> Unit) {
    val painter = painterResource(resource = Res.drawable.penguin)
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
                mouseWheelZoom = settings.mouseWheelZoom,
            ),
    )
}
