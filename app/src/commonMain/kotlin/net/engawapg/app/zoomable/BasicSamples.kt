package net.engawapg.app.zoomable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import coil3.compose.AsyncImage
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import zoomable.app.generated.resources.Res
import zoomable.app.generated.resources.penguin

/**
 * Sample that shows a zoomable image synchronously.
 *
 * [Modifier.zoomable] modifies an [Image] composable which shows a resource image.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun SyncImageSample(onTap: (Offset) -> Unit) {
    val painter = painterResource(resource = Res.drawable.penguin)
//    val painter = painterResource(id = R.drawable.grid)   // for debug
    val zoomState = rememberZoomState(
        contentSize = painter.intrinsicSize,
    )
    Image(
        painter = painter,
        contentDescription = "Zoomable image",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .zoomable(
                zoomState = zoomState,
                onTap = onTap
            ),
    )
}

/**
 * Sample that shows a zoomable image asynchronously.
 *
 * [Modifier.zoomable] modifies Coil library's [AsyncImage] composable.
 * setContentSize() will be called when the image data is loaded.
 */
@Composable
fun AsyncImageSample(onTap: (Offset) -> Unit) {
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
                onTap = onTap
            ),
    )
}

/**
 * Sample that shows a zoomable text.
 *
 * [Modifier.zoomable] modifies [Text] composable.
 */
@Composable
fun TextSample(onTap: (Offset) -> Unit) {
    val zoomState = rememberZoomState()
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .zoomable(
                zoomState = zoomState,
                onTap = onTap
            ),
    ) {
        Text(
            text = "This is zoomable text.",
            textAlign = TextAlign.Center,
        )
    }
}
