package com.example

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Desktop File / Shortcut representation
data class DesktopFile(
    val id: String,
    val name: String,
    val content: String,
    val isSystem: Boolean = false,
    val type: DesktopFileType
)

enum class DesktopFileType {
    THIS_PC, RECYCLE_BIN, NOTEPAD, FOLDER
}

// Window state
data class OpenWindow(
    val id: String,
    val title: String,
    val type: WindowType,
    val isMaximized: Boolean = false,
    val isMinimized: Boolean = false,
    val xDp: Dp = 40.dp,
    val yDp: Dp = 100.dp,
    val associatedFileId: String? = null,
    val draftContent: String = ""
)

enum class WindowType {
    THIS_PC, RECYCLE_BIN, NOTEPAD, CALCULATOR, SETTINGS
}

enum class WallpaperTheme {
    BLOOM_LIGHT, BLOOM_DARK, GLOW_PURPLE, WAVES_EMERALD
}

enum class IconSize(val dp: Dp, val fontSize: Float) {
    SMALL(48.dp, 11f),
    MEDIUM(64.dp, 12f),
    LARGE(80.dp, 14f)
}
