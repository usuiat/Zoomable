package net.engawapg.lib.zoomable

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.size
import androidx.compose.ui.unit.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Composable
fun ZoomableContent(
    zoomEnabled: Boolean = true,
    mouseWheelZoom: MouseWheelZoom = MouseWheelZoom.EnabledWithCtrlKey,
) {
    val icon = Icons.Default.Info
    val zoomState = rememberZoomState(contentSize = Size(icon.viewportWidth, icon.viewportHeight))
    Image(
        imageVector = icon,
        contentDescription = "image",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .zoomable(
                zoomState = zoomState,
                zoomEnabled = zoomEnabled,
                mouseWheelZoom = mouseWheelZoom,
            )
    )
}

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
        val icon = Icons.Default.Info
        val zoomState =
            rememberZoomState(contentSize = Size(icon.viewportWidth, icon.viewportHeight))
        Image(
            imageVector = icon,
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

expect open class PlatformZoomableTest()

@OptIn(ExperimentalTestApi::class)
class ZoomableTest : PlatformZoomableTest() {

    @Test
    fun pinch_gesture_works() = runComposeUiTest {
        setContent { ZoomableContent() }

        val node = onNodeWithContentDescription("image")
        val boundsBefore = node.getBoundsInRoot()
        node.performTouchInput {
            pinch(
                start0 = center + Offset(-100f, 0f),
                end0 = center + Offset(-200f, 0f),
                start1 = center + Offset(+100f, 0f),
                end1 = center + Offset(+200f, 0f),
            )
        }
        val boundsAfter = node.getBoundsInRoot()

        assertTrue(boundsAfter.width > boundsBefore.width)
        assertTrue(boundsAfter.height > boundsBefore.height)
    }

    @Test
    fun tap_and_drag_gesture_works() = runComposeUiTest {
        setContent { ZoomableContent() }

        val node = onNodeWithContentDescription("image")
        val boundsBefore = node.getBoundsInRoot()
        node.performTouchInput {
            down(center)
            advanceEventTime(100)
            up()
            advanceEventTime(100)
            swipe(start = center, end = center + Offset(0f, 100f))
        }
        val boundsAfter = node.getBoundsInRoot()

        assertTrue(boundsAfter.width > boundsBefore.width)
        assertTrue(boundsAfter.height > boundsBefore.height)
    }

    @Test
    fun double_tap_works_as_zoom() = runComposeUiTest {
        setContent { ZoomableContent() }

        val node = onNodeWithContentDescription("image")
        val bounds0 = node.getBoundsInRoot()

        node.performTouchInput {
            doubleClick(center)
        }
        val bounds1 = node.getBoundsInRoot()
        assertTrue(bounds1.width > bounds0.width)
        assertTrue(bounds1.height > bounds0.height)

        node.performTouchInput {
            doubleClick(center)
        }
        val bounds2 = node.getBoundsInRoot()
        assertEquals(bounds2.size, bounds0.size)
    }

    @Test
    fun zoom_for_composable_on_pager_is_available_after_swiping_pages() = runComposeUiTest {
        /*
        This function tests that zooming works after page swipes.
        We ran into a problem with Compose 1.5 where zooming did not work after swiping a
        HorizontalPager page and then returning to the initial page.
         */
        setContent { ZoomablePagerContent() }

        var image = onNodeWithContentDescription("image0")
        image.assertIsDisplayed()
        image.performTouchInput {
            swipeLeft()
        }

        image = onNodeWithContentDescription("image1")
        image.assertIsDisplayed()
        image.performTouchInput {
            swipeRight()
        }

        image = onNodeWithContentDescription("image0")
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
        assertTrue(bounds.left < 0.dp)
        assertTrue(bounds.top < 0.dp)
    }

    @Test
    fun tap_works() = runComposeUiTest {
        var count = 0
        var positionAtCallback: Offset = Offset.Unspecified
        var positionTapped: Offset = Offset.Zero
        mainClock.autoAdvance = false
        setContent {
            val icon = Icons.Default.Info
            val zoomState =
                rememberZoomState(contentSize = Size(icon.viewportWidth, icon.viewportHeight))
            Image(
                imageVector = icon,
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

        onNodeWithContentDescription("image").performTouchInput {
            positionTapped = center
            click(positionTapped)
        }
        // Wait manually because automatic synchronization does not work well.
        // I think the wait process to determine if it is a double-tap is judged to be idle.
        mainClock.advanceTimeBy(1000L)
        assertTrue(count == 1)
        assertEquals(positionAtCallback, positionTapped)
    }

    @Test
    fun long_press_works() = runComposeUiTest {
        var count = 0
        var positionAtCallback: Offset = Offset.Unspecified
        var positionTapped: Offset = Offset.Zero
        setContent {
            val icon = Icons.Default.Info
            val zoomState =
                rememberZoomState(contentSize = Size(icon.viewportWidth, icon.viewportHeight))
            Image(
                imageVector = icon,
                contentDescription = "image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .zoomable(
                        zoomState = zoomState,
                        onLongPress = { position ->
                            count = 1
                            positionAtCallback = position
                        },
                    )
            )
        }

        onNodeWithContentDescription("image").performTouchInput {
            positionTapped = center
            down(positionTapped)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis * 2)
            up()
        }

        assertTrue(count == 1)
        assertEquals(positionTapped, positionAtCallback)
    }

    @Test
    fun mouse_wheel_operation_works_as_zoom() = runComposeUiTest {
        setContent { ZoomableContent(mouseWheelZoom = MouseWheelZoom.Enabled) }

        val node = onNodeWithContentDescription("image")
        val boundsBefore = node.getBoundsInRoot()
        node.performMouseInput { scroll(-1f) }
        val boundsAfter = node.getBoundsInRoot()

        assertTrue(boundsAfter.width > boundsBefore.width)
        assertTrue(boundsAfter.height > boundsBefore.height)
    }

    @Test
    fun modifier_key_and_mouse_wheel_operation_works_as_zoom() = runComposeUiTest {
        setContent { ZoomableContent(mouseWheelZoom = MouseWheelZoom.EnabledWithCtrlKey) }

        val node = onNodeWithContentDescription("image")
        val boundsBefore = node.getBoundsInRoot()
        node.performKeyInput { keyDown(Key.CtrlRight) }
        node.performMouseInput { scroll(-1f) }
        node.performKeyInput { keyUp(Key.CtrlRight) }
        val boundsAfter = node.getBoundsInRoot()

        assertTrue(boundsAfter.width > boundsBefore.width)
        assertTrue(boundsAfter.height > boundsBefore.height)
    }

    @Test
    fun tap_gesture_works_even_if_parent_composable_has_clickable_modifier() = runComposeUiTest {
        var zoomableClickCount = 0
        var parentClickCount = 0
        mainClock.autoAdvance = false
        setContent {
            Box(modifier = Modifier.clickable { parentClickCount++ }) {
                val icon = Icons.Default.Info
                val zoomState =
                    rememberZoomState(contentSize = Size(icon.viewportWidth, icon.viewportHeight))
                Image(
                    imageVector = icon,
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

        onNodeWithContentDescription("image").performTouchInput {
            click(center)
        }
        // Wait manually because automatic synchronization does not work well.
        // I think the wait process to determine if it is a double-tap is judged to be idle.
        mainClock.advanceTimeBy(1000L)
        assertEquals(1, zoomableClickCount)
        assertEquals(0, parentClickCount)
    }

    @Test
    fun scroll_gesture_propagation_content_edge_enables_to_swipe_page_on_content_edge() =
        runComposeUiTest {
            setContent {
                ZoomablePagerContent(
                    scrollGesturePropagation = ScrollGesturePropagation.ContentEdge
                )
            }

            val image0 = onNodeWithContentDescription("image0")
            image0.performTouchInput { doubleClick() }
            waitForIdle()
            image0.performTouchInput { swipeLeft() }
            waitForIdle()
            image0.performTouchInput { swipeLeft() }

            val image1 = onNodeWithContentDescription("image1")
            image1.assertIsDisplayed()
        }

    @Test
    fun scroll_gesture_propagation_not_zoomed_disables_to_swipe_page_on_content_edge() =
        runComposeUiTest {
            setContent {
                ZoomablePagerContent(scrollGesturePropagation = ScrollGesturePropagation.NotZoomed)
            }

            val image0 = onNodeWithContentDescription("image0")
            image0.performTouchInput { doubleClick() }
            waitForIdle()
            image0.performTouchInput { swipeLeft() }
            waitForIdle()
            image0.performTouchInput { swipeLeft() }

            image0.assertIsDisplayed()
        }

    @Test
    fun pinch_gesture_does_not_work_when_zoom_is_disabled() = runComposeUiTest {
        setContent {
            ZoomableContent(zoomEnabled = false)
        }

        val node = onNodeWithContentDescription("image")
        val boundsBefore = node.getBoundsInRoot()
        node.performTouchInput {
            pinch(
                start0 = center + Offset(-100f, 0f),
                end0 = center + Offset(-200f, 0f),
                start1 = center + Offset(+100f, 0f),
                end1 = center + Offset(+200f, 0f),
            )
        }

        val boundsAfter = node.getBoundsInRoot()
        assertEquals(boundsAfter, boundsBefore)
    }

    @Test
    fun snapBackZoomable_can_zoom_image_during_gesture_and_snap_back_after_gesture() =
        runComposeUiTest {
            setContent {
                val icon = Icons.Default.Info
                val zoomState =
                    rememberZoomState(contentSize = Size(icon.viewportWidth, icon.viewportHeight))
                Image(
                    imageVector = icon,
                    contentDescription = "image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .snapBackZoomable(
                            zoomState = zoomState,
                        )
                )
            }

            val node = onNodeWithContentDescription("image")
            val boundsBefore = node.getBoundsInRoot()
            node.performTouchInput {
                down(0, center + Offset(-100f, 0f))
                down(1, center + Offset(+100f, 0f))
                moveTo(0, center + Offset(-200f, 0f))
                moveTo(1, center + Offset(+200f, 0f))
            }
            val boundsInGesture = node.getBoundsInRoot()
            node.performTouchInput {
                up(0)
                up(1)
            }
            val boundsAfter = node.getBoundsInRoot()
            assertTrue(boundsInGesture.width > boundsBefore.width)
            assertTrue(boundsInGesture.height > boundsBefore.height)
            assertEquals(boundsAfter.width, boundsBefore.width)
            assertEquals(boundsAfter.height, boundsBefore.height)
        }

    @Test
    fun enableOneFingerZoom_can_be_changed() = runComposeUiTest {
        var enableOneFingerZoom by mutableStateOf(true)
        setContent {
            val icon = Icons.Default.Info
            val zoomState =
                rememberZoomState(contentSize = Size(icon.viewportWidth, icon.viewportHeight))
            Image(
                imageVector = icon,
                contentDescription = "image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(300.dp)
                    .zoomable(
                        zoomState = zoomState,
                        enableOneFingerZoom = enableOneFingerZoom,
                    )
            )
        }

        val node = onNodeWithContentDescription("image")
        val boundsBefore = node.getBoundsInRoot()
        node.performTouchInput {
            tapAndDragZoom(1.5f)
        }
        val boundsResult1 = node.getBoundsInRoot()
        assertTrue(boundsResult1.width > boundsBefore.width)
        assertTrue(boundsResult1.height > boundsBefore.height)

        enableOneFingerZoom = false
        node.performTouchInput {
            tapAndDragZoom(1.5f)
        }
        val boundsResult2 = node.getBoundsInRoot()
        assertEquals(boundsResult2.width, boundsResult1.width)
        assertEquals(boundsResult2.height, boundsResult1.height)
    }
}
