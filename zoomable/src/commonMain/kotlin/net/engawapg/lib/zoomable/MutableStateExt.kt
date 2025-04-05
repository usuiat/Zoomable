package net.engawapg.lib.zoomable

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.runtime.MutableFloatState

internal suspend fun MutableFloatState.animateTo(
    targetValue: Float,
    animationSpec: AnimationSpec<Float>,
) {
    animate(
        initialValue = value,
        targetValue = targetValue,
        animationSpec = animationSpec,
    ) { newValue, _ ->
        value = newValue
    }
}
