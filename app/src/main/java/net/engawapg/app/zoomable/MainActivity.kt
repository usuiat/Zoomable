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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset

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
    val tabTitles = listOf(
        "Sync Image",
        "Async Image",
        "Text",
        "HorizontalPager\n(Androidx)",
        "VerticalPager\n(Androidx)",
        "HorizontalPager\n(Accompanist)",
        "VerticalPager\n(Accompanist)",
    )

    Column {
        var onTapMessage by remember { mutableStateOf("")}
        val onTap = { point: Offset ->
            onTapMessage = "Tapped @(${point.x}, ${point.y})"
        }
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
                "Sync Image" -> SyncImageSample(onTap)
                "Async Image" -> AsyncImageSample(onTap)
                "Text" -> TextSample(onTap)
                "HorizontalPager\n(Accompanist)" -> AccompanistHorizontalPagerSample(onTap)
                "VerticalPager\n(Accompanist)" -> AccompanistVerticalPagerSample(onTap)
                "HorizontalPager\n(Androidx)" -> AndroidxHorizontalPagerSample(onTap)
                "VerticalPager\n(Androidx)" -> AndroidxVerticalPagerSample(onTap)
            }
        }
        Text(
            text = onTapMessage
        )
    }
}
