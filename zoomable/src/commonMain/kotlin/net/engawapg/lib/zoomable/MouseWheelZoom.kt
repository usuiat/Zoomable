package net.engawapg.lib.zoomable

import androidx.compose.ui.input.pointer.PointerKeyboardModifiers
import androidx.compose.ui.input.pointer.isAltPressed
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.input.pointer.isShiftPressed

public enum class MouseWheelZoom {
    Disabled,
    Enabled,
    EnabledWithCtrlKey,
    EnabledWithShiftKey,
    EnabledWithAltKey,
    EnabledWithMetaKey,
    ;

    internal fun matchKeyboardModifiers(keyboardModifiers: PointerKeyboardModifiers): Boolean {
        return when (this) {
            Disabled -> false
            Enabled -> true
            EnabledWithCtrlKey -> keyboardModifiers.isCtrlPressed
            EnabledWithShiftKey -> keyboardModifiers.isShiftPressed
            EnabledWithAltKey -> keyboardModifiers.isAltPressed
            EnabledWithMetaKey -> keyboardModifiers.isMetaPressed
        }
    }
}
