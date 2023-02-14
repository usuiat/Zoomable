package net.engawapg.lib.zoomable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.swipe
import org.junit.Rule
import org.junit.Test

class ZoomableTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun zoomable_pinch_zoomed() {
        composeTestRule.setContent {
            val painter = painterResource(id = android.R.drawable.ic_dialog_info)
            val zoomState = rememberZoomState(contentSize = painter.intrinsicSize)
            Image(
                painter = painter,
                contentDescription = "image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .zoomable(zoomState)
            )
        }

        val node = composeTestRule.onNodeWithContentDescription("image")
        val boundsBefore = node.fetchSemanticsNode().boundsInRoot
        node.performTouchInput {
                pinch(
                    start0 = center + Offset(-100f, 0f),
                    end0 = center + Offset(-200f, 0f),
                    start1 = center + Offset(+100f, 0f),
                    end1 = center + Offset(+200f, 0f),
                )
            }
        val boundsAfter = node.fetchSemanticsNode().boundsInRoot
        assert(boundsAfter.width > boundsBefore.width && boundsAfter.height > boundsBefore.height)
    }

    @Test
    fun zoomable_tapAndSwipe_zoomed() {
        composeTestRule.setContent {
            val painter = painterResource(id = android.R.drawable.ic_dialog_info)
            val zoomState = rememberZoomState(contentSize = painter.intrinsicSize)
            Image(
                painter = painter,
                contentDescription = "image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .zoomable(zoomState)
            )
        }

        val node = composeTestRule.onNodeWithContentDescription("image")
        val boundsBefore = node.fetchSemanticsNode().boundsInRoot
        println("bounds=$boundsBefore")
        node.performTouchInput {
            down(center)
            advanceEventTime(100)
            up()
            advanceEventTime(100)
            swipe(start = center, end = center + Offset(0f, 100f))
        }
        val boundsAfter = node.fetchSemanticsNode().boundsInRoot
        println("bounds=$boundsAfter")
        assert(boundsAfter.width > boundsBefore.width && boundsAfter.height > boundsBefore.height)
    }
}