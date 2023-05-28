package net.engawapg.lib.zoomable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.doubleClick
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

    @Test
    fun zoomable_doubleTapZoomScale_zoomed() {
        composeTestRule.setContent {
            val painter = painterResource(id = android.R.drawable.ic_dialog_info)
            val zoomState = rememberZoomState(contentSize = painter.intrinsicSize)
            Image(
                painter = painter,
                contentDescription = "image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .zoomable(
                        zoomState = zoomState,
                        doubleTapZoomSpec = DoubleTapZoomScale(2f),
                    )
            )
        }

        val node = composeTestRule.onNodeWithContentDescription("image")
        val bounds0 = node.fetchSemanticsNode().boundsInRoot

        node.performTouchInput {
            doubleClick(center)
        }
        val bounds1 = node.fetchSemanticsNode().boundsInRoot
        assert((bounds1.width / bounds0.width) == 2f)
        assert((bounds1.height / bounds0.height) == 2f)

        node.performTouchInput {
            doubleClick(center)
        }
        val bounds2 = node.fetchSemanticsNode().boundsInRoot
        assert(bounds2.size == bounds0.size)
    }

    @Test
    fun zoomable_doubleTapZoomScaleList_zoomed() {
        composeTestRule.setContent {
            val painter = painterResource(id = android.R.drawable.ic_dialog_info)
            val zoomState = rememberZoomState(contentSize = painter.intrinsicSize)
            Image(
                painter = painter,
                contentDescription = "image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .zoomable(
                        zoomState = zoomState,
                        doubleTapZoomSpec = DoubleTapZoomScaleList(listOf(1f, 2f, 3f))
                    )
            )
        }

        val node = composeTestRule.onNodeWithContentDescription("image")
        val bounds0 = node.fetchSemanticsNode().boundsInRoot

        node.performTouchInput {
            doubleClick(center)
        }
        val bounds1 = node.fetchSemanticsNode().boundsInRoot
        assert((bounds1.width / bounds0.width) == 2f)
        assert((bounds1.height / bounds0.height) == 2f)

        node.performTouchInput {
            doubleClick(center)
        }
        val bounds2 = node.fetchSemanticsNode().boundsInRoot
        assert((bounds2.width / bounds0.width) == 3f)
        assert((bounds2.height / bounds0.height) == 3f)

        node.performTouchInput {
            doubleClick(center)
        }
        val bounds3 = node.fetchSemanticsNode().boundsInRoot
        assert(bounds3.size == bounds0.size)
    }

}