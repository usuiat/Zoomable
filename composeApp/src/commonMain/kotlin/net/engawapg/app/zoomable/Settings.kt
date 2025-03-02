package net.engawapg.app.zoomable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.ScrollGesturePropagation

data class Settings(
    val zoomEnabled: Boolean = true,
    val enableOneFingerZoom: Boolean = true,
    val scrollGesturePropagation: ScrollGesturePropagation = ScrollGesturePropagation.ContentEdge,
    val initialScale: Float = 1f,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    settings: Settings,
    onSettingsChange: (Settings) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
    ) {
        SettingsContent(
            settings = settings,
            onSettingsChange = onSettingsChange,
            onDoneClick = {
                scope
                    .launch { sheetState.hide() }
                    .invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            onDismissRequest()
                        }
                    }
            }
        )
    }
}

@Composable
internal fun SettingsContent(
    settings: Settings,
    onSettingsChange: (Settings) -> Unit,
    onDoneClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onDoneClick) {
            Text("Done")
        }
    }

    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

    SwitchSettingItem(
        text = "zoomEnabled",
        checked = settings.zoomEnabled,
        onCheckedChange = {
            onSettingsChange(settings.copy(zoomEnabled = it))
        },
    )
    SwitchSettingItem(
        text = "enableOneFingerZoom",
        checked = settings.enableOneFingerZoom,
        onCheckedChange = {
            onSettingsChange(settings.copy(enableOneFingerZoom = it))
        },
    )

    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

    Text(
        text = "scrollGesturePropagation",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
    RadioButtonSettingItem(
        text = "ContentEdge",
        selected = settings.scrollGesturePropagation == ScrollGesturePropagation.ContentEdge,
        onClick = {
            onSettingsChange(
                settings.copy(scrollGesturePropagation = ScrollGesturePropagation.ContentEdge)
            )
        },
    )
    RadioButtonSettingItem(
        text = "NotZoomed",
        selected = settings.scrollGesturePropagation == ScrollGesturePropagation.NotZoomed,
        onClick = {
            onSettingsChange(
                settings.copy(scrollGesturePropagation = ScrollGesturePropagation.NotZoomed)
            )
        },
    )

    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

    Text(
        text = "initialScale",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
    Text(
        text = "To reflect the changed values, switch the sample to be displayed.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
    RadioButtonSettingItem(
        text = "1.0",
        selected = settings.initialScale == 1f,
        onClick = { onSettingsChange(settings.copy(initialScale = 1f)) },
    )
    RadioButtonSettingItem(
        text = "2.0",
        selected = settings.initialScale == 2f,
        onClick = { onSettingsChange(settings.copy(initialScale = 2f)) },
    )

    Spacer(modifier = Modifier.height(24.dp))
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
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp, horizontal = 16.dp)
            .heightIn(min = 48.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = null
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
            .padding(horizontal = 12.dp)
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
