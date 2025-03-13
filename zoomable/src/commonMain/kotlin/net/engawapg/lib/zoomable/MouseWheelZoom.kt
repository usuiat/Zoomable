package net.engawapg.lib.zoomable

import androidx.compose.ui.input.pointer.PointerKeyboardModifiers
import androidx.compose.ui.input.pointer.isAltPressed
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.input.pointer.isShiftPressed

/**
 * Specifies mouse wheel zoom behaviour.
 */
public enum class MouseWheelZoom {

    /**
     * No zooming with mouse wheel operation.
     */
    Disabled,

    /**
     * Zoom when mouse wheel is operated regardless of whether the modifier key is pressed.
     */
    Enabled,

    /**
     * Zoom when mouse wheel is operated while holding down Ctrl key.
     */
    EnabledWithCtrlKey,

    /**
     * Zoom when mouse wheel is operated while holding down Shift key.
     */
    EnabledWithShiftKey,

    /**
     * Zoom when mouse wheel is operated while holding down Alt key.
     */
    EnabledWithAltKey,

    /**
     * Zoom when mouse wheel is operated while holding down Meta key (Command key for Mac).
     */
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
