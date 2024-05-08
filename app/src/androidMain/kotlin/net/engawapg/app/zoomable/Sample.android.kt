package net.engawapg.app.zoomable

internal actual fun platformSamples(): Array<Sample> = arrayOf(
    Sample("HorizontalPager\n(Accompanist)") { AccompanistHorizontalPagerSample(it) },
    Sample("VerticalPager\n(Accompanist)") { AccompanistVerticalPagerSample(it) },
)