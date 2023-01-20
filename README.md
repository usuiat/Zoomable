# Zoomable

Zoomable is an android library working with Jetpack Compose and enabling contents zooming with pinch gesture.

![](doc/penguin.gif)

Zoomable provides a simple Modifier extension function `Modifier.zoomable`.

Here is a sample code.

```Kotlin
Image(
    painter = painterResource(id = R.drawable.penguin),
    contentDescription = null,
    modifier = Modifier.zoomable(rememberZoomState()),
)
```

Zoomable can be used with
- any composable components such as `Image`, `Text`, etc.
- asynchronous image composable such as coil's `AsyncImage`.
- Accompanist's `HorizontalPager` and `VerticalPager`.

## Usage

### Download

Zoomable is available on Maven Central.

```
repositories {
    mavenCentral()
}

dependencies {
    implementation "net.engawapg.lib:zoomable:$version"
}
```

The latest version: <img alt="Maven Central" src="https://img.shields.io/maven-central/v/net.engawapg.lib/zoomable">

### Modifier function

You can use `Modifier.zoomable` to make contents such as an image zoomable.
The zoom state is managed in a `ZoomState` object that can be created via `rememberZoomState`.
If `contentSize` is set, the range of offset will be optimized for the specified size.

```Kotlin
val painter = painterResource(id = R.drawable.penguin)
val zoomState = rememberZoomState(contentSize = painter.intrinsicSize)
Image(
    painter = painter,
    contentDescription = "Zoomable image",
    contentScale = ContentScale.Fit,
    modifier = Modifier
        .fillMaxSize()
        .zoomable(zoomState),
)
```

## API Reference

[API Reference🔎](https://usuiat.github.io/Zoomable/)

## Samples

You can see some samples in [MainActivity.kt](app/src/main/java/net/engawapg/app/zoomable/MainActivity.kt)

- Standard Image composable
- Asynchronous image loading using [Coil](https://coil-kt.github.io/coil/) library
- Text
- Image on HorizontalPager and VerticalPager of [Accompanist](https://google.github.io/accompanist/pager/) library

## Lisence

Copyright 2022 usuiat

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Legal Notices

In the sample application, we use open source software:

- [Accompanist](https://google.github.io/accompanist/) (https://www.apache.org/licenses/LICENSE-2.0)
- [Coil](https://coil-kt.github.io/coil/) (https://www.apache.org/licenses/LICENSE-2.0)

To publish this library, we use open source software:

- [gradle-maven-publish-plugin](https://github.com/vanniktech/gradle-maven-publish-plugin) (https://www.apache.org/licenses/LICENSE-2.0)
