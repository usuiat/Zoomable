package net.engawapg.app.zoomable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.engawapg.app.zoomable.theme.ZoomableTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import zoomable_root.composeapp.generated.resources.Res
import zoomable_root.composeapp.generated.resources.menu_24dp
import zoomable_root.composeapp.generated.resources.settings_24dp

sealed interface SampleType {
    val title: String
    data class Basic(override val title: String = "Basic sample") : SampleType
    data class Coil(override val title: String = "Coil AsyncImage") : SampleType
    data class Pager(override val title: String = "Images on HorizontalPager") : SampleType
    data class SnapBack(override val title: String = "snapBackZoomable") : SampleType
    data class LazyColumn(override val title: String = "LazyColumn") : SampleType
    data class ScrollableRow(override val title: String = "Scrollable Row") : SampleType
}

val sampleTypes = listOf(
    SampleType.Basic(),
    SampleType.Coil(),
    SampleType.Pager(),
    SampleType.SnapBack(),
    SampleType.LazyColumn(),
    SampleType.ScrollableRow(),
)

@Composable
@Preview
fun App() {
    ZoomableTheme {
        Scaffold { innerPadding ->
            var sampleType by remember { mutableStateOf(sampleTypes[0]) }
            var showSampleSelection by remember { mutableStateOf(false) }
            var settings by remember { mutableStateOf(Settings()) }
            var showSettings by remember { mutableStateOf(false) }
            Box {
                var message by remember(sampleType) { mutableStateOf(sampleType.title) }
                val onTap = { position: Offset ->
                    message = "Tapped (${position.x.toInt()}, ${position.y.toInt()})"
                }
                val onLongPress = { position: Offset ->
                    message = "Long pressed (${position.x.toInt()}, ${position.y.toInt()})"
                }
                val onLongPressReleased = { position: Offset ->
                    message = "Long press released (${position.x.toInt()}, ${position.y.toInt()})"
                }
                when (sampleType) {
                    is SampleType.Basic -> BasicSample(
                        settings = settings,
                        onTap = onTap,
                        onLongPress = onLongPress,
                        onLongPressReleased = onLongPressReleased
                    )
                    is SampleType.Coil -> CoilSample(
                        settings = settings,
                        onTap = onTap,
                        onLongPress = onLongPress,
                        onLongPressReleased = onLongPressReleased
                    )
                    is SampleType.Pager -> PagerSample(
                        settings = settings,
                        onTap = onTap,
                        onLongPress = onLongPress,
                        onLongPressReleased = onLongPressReleased

                    )
                    is SampleType.SnapBack -> SnapBackSample(
                        settings = settings,
                        onTap = onTap,
                        onLongPress = onLongPress,
                        onLongPressReleased = onLongPressReleased

                    )
                    is SampleType.LazyColumn -> LazyColumnSample(
                        settings = settings,
                        onTap = onTap,
                        onLongPress = onLongPress,
                        onLongPressReleased = onLongPressReleased

                    )
                    is SampleType.ScrollableRow -> ScrollableRowSample(
                        settings = settings,
                        onTap = onTap,
                        onLongPress = onLongPress,
                        onLongPressReleased = onLongPressReleased
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(innerPadding).align(Alignment.BottomCenter)
                ) {
                    FilledTonalIconButton(
                        modifier = Modifier.padding(8.dp),
                        onClick = { showSampleSelection = true },
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.menu_24dp),
                            contentDescription = "Sample type selection",
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        Text(
                            text = message,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                    FilledTonalIconButton(
                        modifier = Modifier.padding(8.dp),
                        onClick = { showSettings = true },
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.settings_24dp),
                            contentDescription = "Settings",
                        )
                    }
                }
            }
            if (showSampleSelection) {
                SampleTypeSelectionBottomSheet(
                    sampleTypeList = sampleTypes,
                    sampleType = sampleType,
                    onSampleTypeChange = { sampleType = it },
                    onDismissRequest = { showSampleSelection = false },
                )
            }
            if (showSettings) {
                SettingsBottomSheet(
                    settings = settings,
                    onSettingsChange = { settings = it },
                    onDismissRequest = { showSettings = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SampleTypeSelectionBottomSheet(
    sampleTypeList: List<SampleType>,
    sampleType: SampleType,
    onSampleTypeChange: (SampleType) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = "Samples",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        for (type in sampleTypeList) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .fillMaxWidth()
                    .clickable {
                        scope
                            .launch {
                                // Delay to make it easier to check the selection result.
                                delay(150)
                                sheetState.hide()
                            }
                            .invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    onDismissRequest()
                                }
                            }
                        onSampleTypeChange(type)
                    },
            ) {
                RadioButton(
                    selected = type == sampleType,
                    onClick = null,
                    modifier = Modifier.size(48.dp)
                )
                Text(type.title)
            }
        }
    }
}
