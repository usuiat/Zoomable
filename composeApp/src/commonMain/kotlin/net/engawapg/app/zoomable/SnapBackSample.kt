package net.engawapg.app.zoomable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.snapBackZoomable
import org.jetbrains.compose.resources.painterResource
import zoomable_root.composeapp.generated.resources.Res
import zoomable_root.composeapp.generated.resources.bird1

@Composable
fun SnapBackSample(settings: Settings, onTap: (Offset) -> Unit, onLongPress: (Offset) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().systemBarsPadding(),
    ) {
        Text("text", Modifier.height(100.dp))
        Text("text", Modifier.height(100.dp))
        Text("text", Modifier.height(100.dp))
        Text("text", Modifier.height(100.dp))
        val painter = painterResource(resource = Res.drawable.bird1)
        val zoomState = rememberZoomState(
//            contentSize = painter.intrinsicSize,
        )
        Image(
            painter = painter,
            contentDescription = "Zoomable image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .snapBackZoomable(
                    zoomState = zoomState,
                    zoomEnabled = settings.zoomEnabled,
                    onTap = onTap,
                    onLongPress = onLongPress,
                ),
        )
    }
}
