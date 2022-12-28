package net.engawapg.app.zoomable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    val tabTitles = listOf("Single Image", "HorizontalPager", "Text")

    Column {
        TabRow(
            selectedTabIndex = tabIndex,
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = { Text(text = title, maxLines = 2) },
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
                "Single Image" -> SingleImage()
                "HorizontalPager" -> ImageOnHorizontalPager()
                "Text" -> ZoomableText()
            }
        }
    }
}

@Composable
fun SingleImage() {
    val painter = painterResource(id = R.drawable.bird1)
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

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ImageOnHorizontalPager() {
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

@Composable
fun ZoomableText() {
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