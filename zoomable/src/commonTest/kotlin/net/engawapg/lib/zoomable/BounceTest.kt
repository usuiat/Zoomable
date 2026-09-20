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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BounceTest {

    @Test
    fun bounce_lowerIs0_throwException() {
        assertFailsWith<IllegalArgumentException> {
            Bounce(lower = 0f, upper = 1f)
        }
    }

    @Test
    fun bounce_lowerIsGreaterThan1_throwException() {
        assertFailsWith<IllegalArgumentException> {
            Bounce(lower = 1.1f, upper = 1f)
        }
    }

    @Test
    fun bounce_upperIsLessThan1_throwException() {
        assertFailsWith<IllegalArgumentException> {
            Bounce(lower = 1f, upper = 0.9f)
        }
    }

    @Test
    fun bounce_boundaryValues_instantiated() {
        val bounce = Bounce(lower = 1f, upper = 1f)
        assertEquals(1f, bounce.lower)
        assertEquals(1f, bounce.upper)
    }

    @Test
    fun default_shrinksOnlyBy10Percent() {
        assertEquals(0.9f, Bounce.Default.lower)
        assertEquals(1f, Bounce.Default.upper)
    }

    @Test
    fun none_doesNotBounceOnEitherSide() {
        assertEquals(1f, Bounce.None.lower)
        assertEquals(1f, Bounce.None.upper)
    }
}
