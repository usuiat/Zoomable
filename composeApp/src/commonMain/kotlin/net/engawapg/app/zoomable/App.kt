package net.engawapg.app.zoomable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import net.engawapg.app.zoomable.theme.ZoomableTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

sealed interface SampleType {
    val title: String
    data class Basic(override val title: String = "Basic image sample") : SampleType
    data class Coil(override val title: String = "Coil image sample") : SampleType
}

val sampleTypes = listOf(
    SampleType.Basic(),
    SampleType.Coil(),
)

@Composable
@Preview
fun App() {
    ZoomableTheme {
        Scaffold { innerPadding ->
            var tabIndex by remember { mutableIntStateOf(0) }
            val samples = remember { samples() }
            var sampleType by remember { mutableStateOf(sampleTypes[0]) }

            Column(modifier = Modifier.padding(innerPadding)) {
                SampleTypeSelectionMenu(
                    sampleTypeList = sampleTypes,
                    sampleType = sampleType,
                    onSampleTypeChange = { sampleType = it },
                )
                var message by remember { mutableStateOf("") }
                val onTap = { point: Offset ->
                    message = "Tapped @(${point.x}, ${point.y})"
                }
                val onLongPress = { point: Offset ->
                    message = "Long pressed @(${point.x}, ${point.y})"
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
                    samples[tabIndex].content(onTap, onLongPress)
                }
                Text(
                    text = message
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SampleTypeSelectionMenu(
    sampleTypeList: List<SampleType>,
    sampleType: SampleType,
    onSampleTypeChange: (SampleType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        TextField(
            value = sampleType.title,
            onValueChange = {},
            readOnly = true,
            maxLines = 1,
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            for (type in sampleTypeList) {
                DropdownMenuItem(
                    text = { Text(type.title) },
                    onClick = {
                        onSampleTypeChange(type)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
