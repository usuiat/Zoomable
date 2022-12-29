/*
 * Copyright 2022 usuiat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.engawapg.app.zoomable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.accompanist.pager.*
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppScreen()
        }
    }
}

@Composable
fun AppScreen() {
    var tabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Sync Image", "Async Image", "Text", "HorizontalPager", "VerticalPager")

    Column {
        ScrollableTabRow(
            selectedTabIndex = tabIndex,
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = { Text(text = title) },
                )
            }
        }
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
        ) {
            when (tabTitles[tabIndex]) {
                "Sync Image" -> SyncImageSample()
                "Async Image" -> AsyncImageSample()
                "Text" -> TextSample()
                "HorizontalPager" -> HorizontalPagerSample()
                "VerticalPager" -> VerticalPagerSample()
            }
        }
    }
}

/**
 * Sample that shows a zoomable image synchronously.
 *
 * [Modifier.zoomable] modifies an [Image] composable which shows a resource image.
 */
@Composable
fun SyncImageSample() {
    val painter = painterResource(id = R.drawable.penguin)
    val zoomState = rememberZoomState(
        contentSize = painter.intrinsicSize,
    )
    Image(
        painter = painter,
        contentDescription = "Zoomable image",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .zoomable(zoomState),
    )
}

/**
 * Sample that shows a zoomable image asynchronously.
 *
 * [Modifier.zoomable] modifies Coil library's [AsyncImage] composable.
 * setContentSize() will be called when the image data is loaded.
 */
@Composable
fun AsyncImageSample() {
    val zoomState = rememberZoomState()
    AsyncImage(
        model = "https://github.com/usuiat.png",
        contentDescription = "GitHub icon",
        contentScale = ContentScale.Fit,
        onSuccess = { state ->
            zoomState.setContentSize(state.painter.intrinsicSize)
        },
        modifier = Modifier
            .fillMaxSize()
            .zoomable(zoomState)
    )
}

/**
 * Sample that shows a zoomable text.
 *
 * [Modifier.zoomable] modifies [Text] composable.
 */
@Composable
fun TextSample() {
    val zoomState = rememberZoomState()
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .zoomable(zoomState)
    ) {
        Text(
            text = "This is zoomable text.",
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Sample that shows a zoomable images on [HorizontalPager].
 *
 * We call reset() to reset scale and offset when an image is moved out of the windows.
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun HorizontalPagerSample() {
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
                    .zoomable(zoomState)
            )
            // Reset zoom state when the page is moved out of the window.
            val isVisible by remember {
                derivedStateOf {
                    val offset = calculateCurrentOffsetForPage(page)
                    (-1.0f < offset) and (offset < 1.0f)
                }
            }
            LaunchedEffect(isVisible) {
                zoomState.reset()
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
fun VerticalPagerSample() {
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
                    .zoomable(zoomState)
            )
            // Reset zoom state when the page is moved out of the window.
            val isVisible by remember {
                derivedStateOf {
                    val offset = calculateCurrentOffsetForPage(page)
                    (-1.0f < offset) and (offset < 1.0f)
                }
            }
            LaunchedEffect(isVisible) {
                zoomState.reset()
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