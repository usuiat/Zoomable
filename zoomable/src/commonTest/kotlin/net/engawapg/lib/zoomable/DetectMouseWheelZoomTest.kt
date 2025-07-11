package net.engawapg.lib.zoomable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.pointer.PointerKeyboardModifiers
import androidx.compose.ui.input.pointer.isAltPressed
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.MouseInjectionScope
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DetectMouseWheelZoomTest : PlatformZoomableTest() {

    private data class ZoomResult(var zoom: Float = 1f)

    private fun ComposeUiTest.pointerInputContentWithDetectMouseWheelZoom(
        result: ZoomResult,
        canZoom: (PointerKeyboardModifiers) -> Boolean = { true },
    ): SemanticsNodeInteraction {
        val testTag = "target"
        setContent {
            Box(
                modifier = Modifier
                    .testTag(testTag)
                    .size(300.dp)
                    .pointerInput(Unit) {
                        detectMouseWheelZoom(
                            canZoom = canZoom,
                            onZoom = { zoom, _ ->
                                result.zoom *= zoom
                            }
                        )
                    }
            )
        }
        return onNodeWithTag(testTag)
    }

    private fun MouseInjectionScope.wheelZoom(zoom: Float) {
        val delta = (zoom - 1f) / SCROLL_TO_ZOOM_RATE
        scroll(delta)
    }

    @Test
    fun scroll_should_be_judged_as_zoom() = runComposeUiTest {
        val result = ZoomResult()
        val target = pointerInputContentWithDetectMouseWheelZoom(result)

        target.performMouseInput {
            wheelZoom(1.1f)
        }

        assertTrue(result.zoom == 1.1f)
    }

    @Test
    fun modifier_keys_are_detected_being_pressed() = runComposeUiTest {
        val result = ZoomResult()
        var keyboardModifiers: PointerKeyboardModifiers? = null
        val target = pointerInputContentWithDetectMouseWheelZoom(
            result = result,
            canZoom = {
                keyboardModifiers = it
                true
            }
        )

        target.performKeyInput { keyDown(Key.CtrlLeft) }
        target.performMouseInput { wheelZoom(1.1f) }
        target.performKeyInput { keyUp(Key.CtrlLeft) }
        assertEquals(true, keyboardModifiers?.isCtrlPressed)

        target.performKeyInput { keyDown(Key.ShiftLeft) }
        target.performMouseInput { wheelZoom(1.1f) }
        target.performKeyInput { keyUp(Key.ShiftLeft) }
        assertEquals(true, keyboardModifiers?.isShiftPressed)

        target.performKeyInput { keyDown(Key.AltLeft) }
        target.performMouseInput { wheelZoom(1.1f) }
        target.performKeyInput { keyUp(Key.AltLeft) }
        assertEquals(true, keyboardModifiers?.isAltPressed)

        target.performKeyInput { keyDown(Key.MetaLeft) }
        target.performMouseInput { wheelZoom(1.1f) }
        target.performKeyInput { keyUp(Key.MetaLeft) }
        assertEquals(true, keyboardModifiers?.isMetaPressed)
    }

    @Test
    fun scroll_should_be_ignored_if_not_allowed() = runComposeUiTest {
        val result = ZoomResult()
        val target = pointerInputContentWithDetectMouseWheelZoom(
            result = result,
            canZoom = { false }
        )

        target.performMouseInput {
            wheelZoom(1.1f)
        }

        assertTrue(result.zoom == 1.0f)
    }
}
