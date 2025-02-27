package net.engawapg.app.zoomable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import net.engawapg.app.zoomable.theme.ZoomableTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

sealed interface SampleType {
    val title: String
    data class Basic(override val title: String = "Basic image sample") : SampleType
    data class Coil(override val title: String = "Coil image sample") : SampleType
    data class Pager(override val title: String = "HorizontalPager sample") : SampleType
}

val sampleTypes = listOf(
    SampleType.Basic(),
    SampleType.Coil(),
    SampleType.Pager(),
)

@Composable
@Preview
fun App() {
    ZoomableTheme {
        Scaffold { innerPadding ->
            Box {
                var sampleType by remember { mutableStateOf(sampleTypes[0]) }
                var message by remember(sampleType) { mutableStateOf("") }
                val onTap = { position: Offset -> message = "Tapped at $position" }
                val onLongPress = { position: Offset -> message = "Long pressed at $position" }
                when (sampleType) {
                    is SampleType.Basic -> BasicSample(onTap = onTap, onLongPress = onLongPress)
                    is SampleType.Coil -> CoilSample(onTap = onTap, onLongPress = onLongPress)
                    is SampleType.Pager -> PagerSample(onTap = onTap, onLongPress = onLongPress)
                }
                SampleTypeSelectionMenu(
                    sampleTypeList = sampleTypes,
                    sampleType = sampleType,
                    onSampleTypeChange = { sampleType = it },
                    modifier = Modifier.padding(innerPadding)
                )
                Text(
                    text = message,
                    modifier = Modifier.padding(innerPadding).align(Alignment.BottomCenter)
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
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
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
