package net.engawapg.app.zoomable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import net.engawapg.app.zoomable.theme.ZoomableTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

@Preview
@Composable
fun SettingsBottomSheetPreview() {
    ZoomableTheme {
        Column {
            SettingsContent(
                settings = Settings(),
                onSettingsChange = {},
                onDoneClick = {},
            )
        }
    }
}
