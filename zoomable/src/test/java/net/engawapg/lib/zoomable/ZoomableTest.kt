package net.engawapg.lib.zoomable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Composable
fun ZoomableContent(zoomEnabled: Boolean = true) {
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
                zoomEnabled = zoomEnabled,
            )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomablePagerContent(
    scrollGesturePropagation: ScrollGesturePropagation = ScrollGesturePropagation.ContentEdge,
) {
    val pagerState = rememberPagerState { 2 }
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .semantics { testTag = "pager" }
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
                    scrollGesturePropagation = scrollGesturePropagation,
                )
        )
    }
}

@RunWith(RobolectricTestRunner::class)
class ZoomableTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun zoomable_pinch_zoomed() {
        composeTestRule.setContent { ZoomableContent() }

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
        composeTestRule.setContent { ZoomableContent() }

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
        composeTestRule.setContent { ZoomableContent() }

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

    @Test
    fun zoomable_on_pager_zoomAfterSwipePage_zoomed() {
        /*
        This function tests that zooming works after page swipes.
        We ran into a problem with Compose 1.5 where zooming did not work after swiping a
        HorizontalPager page and then returning to the initial page.
         */
        composeTestRule.setContent { ZoomablePagerContent() }

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
        var positionAtCallback: Offset = Offset.Unspecified
        var positionTapped: Offset = Offset.Zero
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
                        onTap = { position ->
                            count = 1
                            positionAtCallback = position
                        },
                    )
            )
        }

        composeTestRule.onNodeWithContentDescription("image").performTouchInput {
            positionTapped = center
            click(positionTapped)
        }
        // Wait manually because automatic synchronization does not work well.
        // I think the wait process to determine if it is a double-tap is judged to be idle.
        composeTestRule.mainClock.advanceTimeBy(1000L)
        assert(count == 1)
        assert(positionAtCallback == positionTapped)
    }

    @Test
    fun tap_gesture_works_even_if_parent_composable_has_clickable_modifier() {
        var zoomableClickCount = 0
        var parentClickCount = 0
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            Box(modifier = Modifier.clickable { parentClickCount++ }) {
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
                            onTap = { zoomableClickCount = 1 },
                        )
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("image").performTouchInput {
            click(center)
        }
        // Wait manually because automatic synchronization does not work well.
        // I think the wait process to determine if it is a double-tap is judged to be idle.
        composeTestRule.mainClock.advanceTimeBy(1000L)
        assert(zoomableClickCount == 1)
        assert(parentClickCount == 0)
    }

    @Test
    fun scroll_gesture_propagation_content_edge_enables_to_swipe_page_on_content_edge() {
        composeTestRule.setContent {
            ZoomablePagerContent(scrollGesturePropagation = ScrollGesturePropagation.ContentEdge)
        }

        val image0= composeTestRule.onNodeWithContentDescription("image0")
        image0.performTouchInput { doubleClick() }
        composeTestRule.waitForIdle()
        image0.performTouchInput { swipeLeft() }
        composeTestRule.waitForIdle()
        image0.performTouchInput { swipeLeft() }

        val image1 = composeTestRule.onNodeWithContentDescription("image1")
        image1.assertIsDisplayed()
    }

    @Test
    fun scroll_gesture_propagation_not_zoomed_disables_to_swipe_page_on_content_edge() {
        composeTestRule.setContent {
            ZoomablePagerContent(scrollGesturePropagation = ScrollGesturePropagation.NotZoomed)
        }

        val image0= composeTestRule.onNodeWithContentDescription("image0")
        image0.performTouchInput { doubleClick() }
        composeTestRule.waitForIdle()
        image0.performTouchInput { swipeLeft() }
        composeTestRule.waitForIdle()
        image0.performTouchInput { swipeLeft() }

        image0.assertIsDisplayed()
    }

    @Test
    fun pinch_gesture_does_not_work_when_zoom_is_disabled() {
        composeTestRule.setContent {
            ZoomableContent(zoomEnabled = false)
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
        assert(boundsAfter == boundsBefore)
    }
}