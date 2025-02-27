package net.engawapg.app.zoomable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.engawapg.lib.zoomable.ScrollGesturePropagation

data class Settings(
    val zoomEnabled: Boolean = true,
    val enableOneFingerZoom: Boolean = true,
    val scrollGesturePropagation: ScrollGesturePropagation = ScrollGesturePropagation.ContentEdge,
)

@Composable
fun SettingsDialog(settings: Settings, onDone: (Settings) -> Unit) {
    var zoomEnabled by remember { mutableStateOf(settings.zoomEnabled) }
    var enableOneFingerZoom by remember { mutableStateOf(settings.enableOneFingerZoom) }
    var scrollGesturePropagation by remember { mutableStateOf(settings.scrollGesturePropagation) }
    AlertDialog(
        title = { Text("Settings") },
        onDismissRequest = {},
        confirmButton = {
            TextButton(
                onClick = {
                    val changedSettings = Settings(
                        zoomEnabled = zoomEnabled,
                        enableOneFingerZoom = enableOneFingerZoom,
                        scrollGesturePropagation = scrollGesturePropagation
                    )
                    onDone(changedSettings)
                }
            ) { Text("Done") }
        },
        text = {
            Column {
                SwitchSettingItem(
                    text = "zoomEnabled",
                    checked = zoomEnabled,
                    onCheckedChange = { zoomEnabled = it },
                )
                SwitchSettingItem(
                    text = "enableOneFingerZoom",
                    checked = enableOneFingerZoom,
                    onCheckedChange = { enableOneFingerZoom = it },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "scrollGesturePropagation",
                    style = MaterialTheme.typography.bodyLarge,
                )
                RadioButtonSettingItem(
                    text = "ContentEdge",
                    selected = scrollGesturePropagation == ScrollGesturePropagation.ContentEdge,
                    onClick = { scrollGesturePropagation = ScrollGesturePropagation.ContentEdge },
                )
                RadioButtonSettingItem(
                    text = "NotZoomed",
                    selected = scrollGesturePropagation == ScrollGesturePropagation.NotZoomed,
                    onClick = { scrollGesturePropagation = ScrollGesturePropagation.NotZoomed },
                )
            }
        }
    )
}

@Composable
private fun SwitchSettingItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp).padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun RadioButtonSettingItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            modifier = Modifier.size(48.dp)
        )
        Text(text = text)
    }
}
