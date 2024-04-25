package net.engawapg.app.zoomable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

/**
 * Sample that shows a zoomable images on [HorizontalPager].
 *
 * We call reset() to reset scale and offset when an image is moved out of the windows.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AndroidxHorizontalPagerSample(onTap: (Offset) -> Unit = {}) {
    val resources = listOf(R.drawable.duck1, R.drawable.duck2, R.drawable.duck3)
    val pagerState = rememberPagerState { resources.size }
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val painter = painterResource(id = resources[page])
        val zoomState = rememberZoomState(contentSize = painter.intrinsicSize)
        Image(
            painter = painter,
            contentDescription = "Zoomable image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .zoomable(
                    zoomState = zoomState,
                    onTap = onTap,
                )
        )

        // Reset zoom state when the page is moved out of the window.
        val isVisible = page == pagerState.settledPage
        LaunchedEffect(isVisible) {
            if (!isVisible) {
                zoomState.reset()
            }
        }
    }
}

/**
 * Sample that shows a zoomable images on [VerticalPager].
 *
 * We call reset() to reset scale and offset when an image is moved out of the windows.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AndroidxVerticalPagerSample(onTap: (Offset) -> Unit) {
    val resources = listOf(R.drawable.eagle1, R.drawable.eagle2, R.drawable.eagle3)
    val pagerState = rememberPagerState { resources.size }
    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val painter = painterResource(id = resources[page])
        val zoomState = rememberZoomState(contentSize = painter.intrinsicSize)
        Image(
            painter = painter,
            contentDescription = "Zoomable image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .zoomable(
                    zoomState = zoomState,
                    onTap = onTap
                ),
        )

        // Reset zoom state when the page is moved out of the window.
        val isVisible = page == pagerState.settledPage
        LaunchedEffect(isVisible) {
            if (!isVisible) {
                zoomState.reset()
            }
        }
    }
}
