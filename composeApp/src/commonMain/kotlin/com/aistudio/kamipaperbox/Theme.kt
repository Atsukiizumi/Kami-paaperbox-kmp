package com.aistudio.kamipaperbox

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 和纸 (Washi) 配色
val WashiDarkBg = Color(0xFF0E0D0C)
val WashiDarkSurface = Color(0xFF171614)
val WashiDarkElevated = Color(0xFF211F1C)
val WashiDarkFg = Color(0xFFF3EFE8)
val WashiDarkAccent = Color(0xFFE8DFD2)

val WashiLightBg = Color(0xFFF3ECE0)
val WashiLightSurface = Color(0xFFE8E0D2)
val WashiLightElevated = Color(0xFFFAF6EE)
val WashiLightFg = Color(0xFF12110F)
val WashiLightAccent = Color(0xFF1C1A17)

// 青墨 (Aosumi) 配色
val AosumiDarkBg = Color(0xFF0D1116)
val AosumiDarkSurface = Color(0xFF161C24)
val AosumiDarkAccent = Color(0xFF8AA4B8)

// 朱砂 (Shusha) 配色
val ShushaDarkBg = Color(0xFF140F0E)
val ShushaDarkSurface = Color(0xFF1D1715)
val ShushaDarkAccent = Color(0xFFC45C48)

enum class ThemePreset {
    WASHI, AOSUMI, SHUSHA
}

@Composable
fun KamiTheme(
    preset: ThemePreset = ThemePreset.WASHI,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (preset) {
        ThemePreset.WASHI -> if (darkTheme) {
            darkColorScheme(
                background = WashiDarkBg,
                surface = WashiDarkSurface,
                surfaceVariant = WashiDarkElevated,
                onBackground = WashiDarkFg,
                onSurface = WashiDarkFg,
                primary = WashiDarkAccent,
                onPrimary = Color(0xFF1A1714)
            )
        } else {
            lightColorScheme(
                background = WashiLightBg,
                surface = WashiLightSurface,
                surfaceVariant = WashiLightElevated,
                onBackground = WashiLightFg,
                onSurface = WashiLightFg,
                primary = WashiLightAccent,
                onPrimary = WashiLightBg
            )
        }
        ThemePreset.AOSUMI -> darkColorScheme(
            background = AosumiDarkBg,
            surface = AosumiDarkSurface,
            onBackground = Color(0xFFE6EDF4),
            primary = AosumiDarkAccent,
            onPrimary = Color(0xFF0D1116)
        )
        ThemePreset.SHUSHA -> darkColorScheme(
            background = ShushaDarkBg,
            surface = ShushaDarkSurface,
            onBackground = Color(0xFFF3EBE4),
            primary = ShushaDarkAccent,
            onPrimary = Color(0xFF140F0E)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
