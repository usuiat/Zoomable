/*
 * Copyright 2026 usuiat
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

package net.engawapg.lib.zoomable

import androidx.annotation.FloatRange

/**
 * Specifies how far the content can be scaled beyond its normal range while a zoom gesture is in
 * progress. Once the gesture ends, the scale animates back into the normal range.
 *
 * Both values are factors applied to the boundaries of the normal scale range, not absolute scale
 * values. Note that the two are applied to different boundaries: [lower] is applied to the minimum
 * scale, which is always `1.0`, while [upper] is applied to [ZoomState.maxScale].
 *
 * For example, with a [ZoomState] whose `maxScale` is `5.0`:
 *
 * ```
 * Bounce(lower = 0.8f, upper = 1.2f) // Scale can reach 0.8 while shrinking and 6.0 while enlarging.
 * ```
 *
 * @param lower The factor applied to the minimum scale of `1.0`. Must be greater than `0.0` and at
 * most `1.0`. A value of `1.0` disables bouncing while shrinking.
 * @param upper The factor applied to [ZoomState.maxScale]. Must be at least `1.0`. A value of `1.0`
 * disables bouncing while enlarging.
 */
public data class Bounce(
    @param:FloatRange(from = 0.0, fromInclusive = false, to = 1.0) public val lower: Float,
    @param:FloatRange(from = 1.0) public val upper: Float,
) {
    init {
        require(lower > 0f && lower <= 1f) {
            "lower must be greater than 0.0 and at most 1.0."
        }
        require(upper >= 1f) { "upper must be at least 1.0." }
    }

    public companion object {
        /**
         * Bounces while shrinking but not while enlarging.
         */
        public val Default: Bounce = Bounce(lower = 0.9f, upper = 1f)

        /**
         * Does not bounce at all.
         */
        public val None: Bounce = Bounce(lower = 1f, upper = 1f)
    }
}
