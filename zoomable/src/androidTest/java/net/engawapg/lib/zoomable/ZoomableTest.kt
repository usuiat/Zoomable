package net.engawapg.lib.zoomable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
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
    fun zoomable_doubleTap_zoomed() {
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
                    )
            )
        }

        val node = composeTestRule.onNodeWithContentDescription("image")
        val bounds0 = node.fetchSemanticsNode().boundsInRoot

        node.performTouchInput {
            doubleClick(center)
        }
        val bounds1 = node.fetchSemanticsNode().boundsInRoot
        assert((bounds1.width / bounds0.width) == 2.5f)
        assert((bounds1.height / bounds0.height) == 2.5f)

        node.performTouchInput {
            doubleClick(center)
        }
        val bounds2 = node.fetchSemanticsNode().boundsInRoot
        assert(bounds2.size == bounds0.size)
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Test
    fun zoomable_on_pager_zoomAfterSwipePage_zoomed() {
        /*
        This function tests that zooming works after page swipes.
        We ran into a problem with Compose 1.5 where zooming did not work after swiping a
        HorizontalPager page and then returning to the initial page.
         */
        composeTestRule.setContent {
            val pagerState = rememberPagerState { 2 }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().semantics { testTag = "pager" }
            ) { page ->
                val painter = painterResource(id = android.R.drawable.ic_dialog_info)
                val zoomState = rememberZoomState(contentSize = painter.intrinsicSize)
                Image(
                    painter = painter,
                    contentDescription = "image$page",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .zoomable(
                            zoomState = zoomState,
                        )
                )
            }
        }

        var image= composeTestRule.onNodeWithContentDescription("image0")
        image.assertIsDisplayed()
        image.performTouchInput {
            swipeLeft()
        }

        image = composeTestRule.onNodeWithContentDescription("image1")
        image.assertIsDisplayed()
        image.performTouchInput {
            swipeRight()
        }

        image = composeTestRule.onNodeWithContentDescription("image0")
        image.assertIsDisplayed()
        image.performTouchInput {
            pinch(
                start0 = center + Offset(-100f, 0f),
                end0 = center + Offset(-200f, 0f),
                start1 = center + Offset(+100f, 0f),
                end1 = center + Offset(+200f, 0f),
            )
        }

        /*
        We really want to check the size of the image, but on Pager we cannot get the size right,
        so instead we check that Top and Right are negative numbers.
         */
        val bounds = image.getUnclippedBoundsInRoot()
        assert(bounds.left < 0.dp && bounds.top < 0.dp)
    }

    @Test
    fun zoomable_tap_calledOnTap() {
        var count = 0
        composeTestRule.mainClock.autoAdvance = false
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
                        onTap = {
                            count = 1
                        },
                    )
            )
        }

        composeTestRule.onNodeWithContentDescription("image").performClick()
        // Wait manually because automatic synchronization does not work well.
        // I think the wait process to determine if it is a double-tap is judged to be idle.
        composeTestRule.mainClock.advanceTimeBy(1000L)
        assert(count == 1)
    }
}