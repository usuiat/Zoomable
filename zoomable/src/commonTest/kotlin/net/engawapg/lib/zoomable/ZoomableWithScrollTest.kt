package net.engawapg.lib.zoomable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.unit.dp
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ZoomableWithScrollTest : PlatformZoomableTest() {
    @OptIn(ExperimentalZoomableApi::class)
    @Test
    fun zoomed_column_can_be_scroll_from_top_to_bottom() = runComposeUiTest {
        setContent {
            LazyColumn(
                modifier = Modifier
                    .size(500.dp)
                    .testTag("column")
                    .zoomableWithScroll(rememberZoomState())
            ) {
                items(10) {
                    BasicText(
                        text = "item$it",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                }
            }
        }
        val column = onNodeWithTag("column")

        // 2.5x zoom
        column.performTouchInput { doubleClick() }
        onNodeWithText("item0").assertIsNotDisplayed()

        // scroll to the top
        column.performTouchInput { swipeDown() }
        onNodeWithText("item0").assertIsDisplayed()

        // scroll to the last item
        column.performScrollToNode(hasText("item9"))
        onNodeWithText("item9").assertIsDisplayed()
    }
}
