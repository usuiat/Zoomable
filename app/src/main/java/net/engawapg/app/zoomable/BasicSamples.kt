package net.engawapg.app.zoomable

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

/**
 * Sample that shows a zoomable image synchronously.
 *
 * [Modifier.zoomable] modifies an [Image] composable which shows a resource image.
 */
@Composable
fun SyncImageSample() {
    val painter = painterResource(id = R.drawable.penguin)
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
            .zoomable(zoomState),
    )
}

/**
 * Sample that shows a zoomable image asynchronously.
 *
 * [Modifier.zoomable] modifies Coil library's [AsyncImage] composable.
 * setContentSize() will be called when the image data is loaded.
 */
@Composable
fun AsyncImageSample() {
    val zoomState = rememberZoomState()
    val coroutineScope = rememberCoroutineScope()
    AsyncImage(
        model = "https://github.com/usuiat.png",
        contentDescription = "GitHub icon",
        contentScale = ContentScale.Fit,
        onSuccess = { state ->
            zoomState.setContentSize(state.painter.intrinsicSize)
            Log.d("ContentSize", state.painter.intrinsicSize.toString())
            coroutineScope.launch {
                zoomState.zoomToOnContentCoordinate(zoomTo = Offset(x = 230f, y = 230f))
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(1) {
                detectTapGestures(
                    onDoubleTap = {
                        coroutineScope.launch {
                            if (zoomState.scale == 1f) {
                                val zoomFixed = zoomState.zoomToOnLayoutCoordinate(zoomTo = it)
                            } else {
                                zoomState.zoomToOnLayoutCoordinate(zoomTo = Offset.Zero, scale = 1f)
                            }
                        }
                    }
                )
            }
            .zoomable(zoomState)
    )
}

/**
 * Sample that shows a zoomable text.
 *
 * [Modifier.zoomable] modifies [Text] composable.
 */
@Composable
fun TextSample() {
    val zoomState = rememberZoomState()
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .zoomable(zoomState)
    ) {
        Text(
            text = "This is zoomable text.",
            textAlign = TextAlign.Center,
        )
    }
}
