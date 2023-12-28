package net.engawapg.app.zoomable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.PagerScope
import com.google.accompanist.pager.VerticalPager
import com.google.accompanist.pager.VerticalPagerIndicator
import com.google.accompanist.pager.calculateCurrentOffsetForPage
import com.google.accompanist.pager.rememberPagerState
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

/**
 * Sample that shows a zoomable images on [HorizontalPager].
 *
 * We call reset() to reset scale and offset when an image is moved out of the windows.
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun AccompanistHorizontalPagerSample(onTap: (Offset) -> Unit) {
    val resources = listOf(R.drawable.bird1, R.drawable.bird2, R.drawable.bird3)
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val pagerState = rememberPagerState()

        HorizontalPager(
            count = resources.size,
            state = pagerState,
        ) {page ->
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
            val isVisible by remember {
                derivedStateOf {
                    isVisibleForPage(page)
                }
            }
            LaunchedEffect(isVisible) {
                if (!isVisible) {
                    zoomState.reset()
                }
            }
        }

        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
        )
    }
}

/**
 * Sample that shows a zoomable images on [VerticalPager].
 *
 * We call reset() to reset scale and offset when an image is moved out of the windows.
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun AccompanistVerticalPagerSample(onTap: (Offset) -> Unit) {
    val resources = listOf(R.drawable.shoebill1, R.drawable.shoebill2, R.drawable.shoebill3)
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val pagerState = rememberPagerState()

        VerticalPager(
            count = resources.size,
            state = pagerState,
        ) {page ->
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
            val isVisible by remember {
                derivedStateOf {
                    isVisibleForPage(page)
                }
            }
            LaunchedEffect(isVisible) {
                if (!isVisible) {
                    zoomState.reset()
                }
            }
        }

        VerticalPagerIndicator(
            pagerState = pagerState,
            activeColor = Color(0x77ffffff),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp),
        )
    }
}

/**
 * Determine if the page is visible.
 *
 * @param page Page index to be determined.
 * @return true if the page is visible.
 */
@OptIn(ExperimentalPagerApi::class)
fun PagerScope.isVisibleForPage(page: Int): Boolean {
    val offset = calculateCurrentOffsetForPage(page)
    return (-1.0f < offset) and (offset < 1.0f)
}
