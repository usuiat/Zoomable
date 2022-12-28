package net.engawapg.app.zoomable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppScreen()
        }
    }
}

@Composable
fun AppScreen() {
    SingleImage(
        painter = painterResource(id = R.drawable.bird1),
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun SingleImage(
    painter: Painter,
    modifier: Modifier = Modifier,
) {
    val zoomState = rememberZoomState(maxScale = 5f)
    zoomState.setImageSize(painter.intrinsicSize)
    Image(
        painter = painter,
        contentDescription = "Zoomable image",
        contentScale = ContentScale.Fit,
        modifier = modifier.zoomable(zoomState),
    )
}