package net.engawapg.app.zoomable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import net.engawapg.lib.zoomable.ExperimentalZoomableApi
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomableWithScroll

@OptIn(ExperimentalZoomableApi::class)
@Composable
fun LazyColumnSample(
    settings: Settings,
    onTap: (Offset) -> Unit,
    onLongPress: (Offset) -> Unit,
    onLongPressReleased: (Offset) -> Unit,
) {
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()
    val contentPadding = PaddingValues(
        top = systemBarsPadding.calculateTopPadding(),
        bottom = systemBarsPadding.calculateBottomPadding() + 64.dp
    )
    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .zoomableWithScroll(
                zoomState = rememberZoomState(initialScale = settings.initialScale),
                zoomEnabled = settings.zoomEnabled,
                enableOneFingerZoom = settings.enableOneFingerZoom,
                onTap = onTap,
                onLongPress = onLongPress,
                onLongPressReleased = onLongPressReleased,
            )
    ) {
        items(10) {
            ListItem(text = "Item $it")
        }
    }
}

@Composable
private fun ListItem(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 16.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.medium
            )
            .wrapContentSize()
    )
}
