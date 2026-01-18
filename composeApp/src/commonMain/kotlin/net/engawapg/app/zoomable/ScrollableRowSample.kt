package net.engawapg.app.zoomable

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.engawapg.app.zoomable.theme.ZoomableTheme
import net.engawapg.lib.zoomable.ExperimentalZoomableApi
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomableWithScroll

@OptIn(ExperimentalZoomableApi::class)
@Composable
fun ScrollableRowSample(
    settings: Settings,
    onTap: (Offset) -> Unit,
    onLongPress: (Offset) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxSize()
            .zoomableWithScroll(
                zoomState = rememberZoomState(initialScale = settings.initialScale),
                zoomEnabled = settings.zoomEnabled,
                enableOneFingerZoom = settings.enableOneFingerZoom,
                onTap = onTap,
                onLongPress = onLongPress,
            )
            .horizontalScroll(state = rememberScrollState())
    ) {
        repeat(10) {
            RowItem(text = "Item $it")
        }
    }
}

@Composable
private fun RowItem(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .size(200.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.medium
            )
            .wrapContentSize()
    )
}

@Preview
@Composable
fun ScrollableRowSamplePreview() {
    ZoomableTheme {
        Surface {
            ScrollableRowSample(
                settings = Settings(),
                onTap = {},
                onLongPress = {},
            )
        }
    }
}
