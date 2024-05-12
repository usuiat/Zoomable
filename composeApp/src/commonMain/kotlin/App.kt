import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import net.engawapg.app.zoomable.samples
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalResourceApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        var tabIndex by remember { mutableIntStateOf(0) }
        val samples = remember { samples() }

        Column {
            var onTapMessage by remember { mutableStateOf("") }
            val onTap = { point: Offset ->
                onTapMessage = "Tapped @(${point.x}, ${point.y})"
            }
            ScrollableTabRow(
                selectedTabIndex = tabIndex,
            ) {
                samples.forEachIndexed { index, sample ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(text = sample.title) },
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds()
            ) {
                samples[tabIndex].content(onTap)
            }
            Text(
                text = onTapMessage
            )
        }
    }
}