package net.engawapg.app.zoomable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.snapBackZoomable
import org.jetbrains.compose.resources.painterResource
import zoomable_root.composeapp.generated.resources.Res
import zoomable_root.composeapp.generated.resources.bird1

@Composable
fun SnapBackSample(settings: Settings, onTap: (Offset) -> Unit, onLongPress: (Offset) -> Unit) {
    val painter = painterResource(resource = Res.drawable.bird1)
    val zoomState = rememberZoomState(
        contentSize = painter.intrinsicSize,
    )
    Image(
        painter = painter,
        contentDescription = "Zoomable image",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .snapBackZoomable(
                zoomState = zoomState,
                zoomEnabled = settings.zoomEnabled,
                onTap = onTap,
                onLongPress = onLongPress,
            ),
    )
}
