package net.engawapg.app.zoomable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import net.engawapg.lib.zoomable.SnapBackZoomableBox
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import zoomable_root.samples.shared.generated.resources.Res
import zoomable_root.samples.shared.generated.resources.bird1
import zoomable_root.samples.shared.generated.resources.bird2
import zoomable_root.samples.shared.generated.resources.bird3
import zoomable_root.samples.shared.generated.resources.duck1
import zoomable_root.samples.shared.generated.resources.duck2
import zoomable_root.samples.shared.generated.resources.duck3
import zoomable_root.samples.shared.generated.resources.eagle1
import zoomable_root.samples.shared.generated.resources.eagle2
import zoomable_root.samples.shared.generated.resources.eagle3

@Composable
fun SnapBackZoomableBoxSample(
    onTap: (Offset) -> Unit,
) {
    val images: List<DrawableResource> = listOf(
        Res.drawable.bird1,
        Res.drawable.duck1,
        Res.drawable.eagle1,
        Res.drawable.bird2,
        Res.drawable.duck2,
        Res.drawable.eagle2,
        Res.drawable.bird3,
        Res.drawable.duck3,
        Res.drawable.eagle3,
    )
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(images) { resource ->
            SnapBackZoomableBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                onTap = onTap,
            ) {
                Image(
                    painter = painterResource(resource),
                    contentDescription = "Zoomable image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                )
            }
        }
    }
}
