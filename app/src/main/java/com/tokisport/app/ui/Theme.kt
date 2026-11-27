package com.tokisport.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tokisport.app.R

// 本轮起设计规范以 TokiMusicUI 为准（用户指令：按照 TokiMusicUI 的设计规范重构）：
// - 色板走 TokiMusicUI 的固定结构色板（深色默认语义 + 成对浅色），不用动态取色：
//   页底深色纯黑 #000000 / 浅色纯白 #FFFFFF，卡片 #1C1C1E / #F2F2F4，
//   高阶表面 #2C2C2E / #E9E9ED，次级文字 #8E8E93 / #6E6E73，错误红 #FF3B30。
//   主色保留 TokiSport 自己的种子紫 #6750A4 派生（TokiMusic 的米色是播放键品牌色，不移植）。
// - 玻璃：玻璃是唯一 chrome 外观（见 Glass.kt），加强档 32dp + 45% veil 给标题头与底栏。
// - 全局字体 Inter（可变字体，内嵌）+ Noto Sans SC 中文回退（不变）；
//   队伍分数专用字体 Google Sans Flex（ScoreFontFamily），只用于比分与积分数字（不变）。
// - 圆角阶梯与 MD3 默认 Shapes 一致：4 / 8 / 12 / 16 / 28 / 全圆。

/** 当前是否为深色（玻璃 tint / pill 取色用）。 */
val LocalDarkTheme = staticCompositionLocalOf { false }

/** 底栏选中 pill：深 #54545F / 浅 #D9D6E2（TokiMusicUI 口径，中性灰紫，不用种子色）。 */
@Composable
fun tokiPillColor(): Color =
    if (LocalDarkTheme.current) Color(0xFF54545F) else Color(0xFFD9D6E2)

/** 页面底色：深色纯黑 / 浅色纯白（TokiMusicUI tokiPageColor 口径）。 */
@Composable
fun tokiPageColor(): Color =
    if (LocalDarkTheme.current) Color(0xFF000000) else Color(0xFFFFFFFF)

private val Seed = Color(0xFF6750A4)

@OptIn(ExperimentalTextApi::class)
private fun variableFont(
    resId: Int,
    weight: FontWeight,
): Font = Font(
    resId = resId,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

/** 全局字体：Inter 在前，Noto Sans SC 回退中文。 */
val AppFontFamily = FontFamily(
    variableFont(R.font.inter, FontWeight.Normal),
    variableFont(R.font.inter, FontWeight.Medium),
    variableFont(R.font.inter, FontWeight.SemiBold),
    variableFont(R.font.inter, FontWeight.Bold),
    variableFont(R.font.noto_sans_sc, FontWeight.Normal),
    variableFont(R.font.noto_sans_sc, FontWeight.Medium),
    variableFont(R.font.noto_sans_sc, FontWeight.SemiBold),
    variableFont(R.font.noto_sans_sc, FontWeight.Bold),
)

/** 队伍分数字体：Google Sans Flex 在前，Inter / Noto Sans SC 回退（中文标签用）。 */
val ScoreFontFamily = FontFamily(
    variableFont(R.font.google_sans_flex, FontWeight.Normal),
    variableFont(R.font.google_sans_flex, FontWeight.Medium),
    variableFont(R.font.google_sans_flex, FontWeight.SemiBold),
    variableFont(R.font.google_sans_flex, FontWeight.Bold),
    variableFont(R.font.inter, FontWeight.Normal),
    variableFont(R.font.inter, FontWeight.Bold),
    variableFont(R.font.noto_sans_sc, FontWeight.Normal),
    variableFont(R.font.noto_sans_sc, FontWeight.Bold),
)

private val baseTypography = Typography()

/** 全局排版：每个字阶都绑定 Inter 字体族，不留系统默认字体。 */
val AppTypography = Typography(
    displayLarge = baseTypography.displayLarge.copy(fontFamily = AppFontFamily),
    displayMedium = baseTypography.displayMedium.copy(fontFamily = AppFontFamily),
    displaySmall = baseTypography.displaySmall.copy(fontFamily = AppFontFamily),
    headlineLarge = baseTypography.headlineLarge.copy(fontFamily = AppFontFamily),
    headlineMedium = baseTypography.headlineMedium.copy(fontFamily = AppFontFamily),
    headlineSmall = baseTypography.headlineSmall.copy(fontFamily = AppFontFamily),
    titleLarge = baseTypography.titleLarge.copy(fontFamily = AppFontFamily),
    titleMedium = baseTypography.titleMedium.copy(fontFamily = AppFontFamily),
    titleSmall = baseTypography.titleSmall.copy(fontFamily = AppFontFamily),
    bodyLarge = baseTypography.bodyLarge.copy(fontFamily = AppFontFamily),
    bodyMedium = baseTypography.bodyMedium.copy(fontFamily = AppFontFamily),
    bodySmall = baseTypography.bodySmall.copy(fontFamily = AppFontFamily),
    labelLarge = baseTypography.labelLarge.copy(fontFamily = AppFontFamily),
    labelMedium = baseTypography.labelMedium.copy(fontFamily = AppFontFamily),
    labelSmall = baseTypography.labelSmall.copy(fontFamily = AppFontFamily),
)

/** 圆角阶梯（与 TokiMusicUI / MD3 默认 Shapes 一致）：4 / 8 / 12 / 16 / 28。 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

// TokiMusicUI 深色结构色板（默认语义）+ TokiSport 种子紫主色。
private val DarkScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF3D3D4E),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF2C2C2E),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF2C2C2E),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFF000000),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF0E0E10),
    surfaceContainer = Color(0xFF1C1C1E),
    surfaceContainerHigh = Color(0xFF2C2C2E),
    surfaceContainerHighest = Color(0xFF3D3D4E),
    surfaceVariant = Color(0xFF1C1C1E),
    onSurfaceVariant = Color(0xFF8E8E93),
    outline = Color(0xFF55555E),
    outlineVariant = Color(0xFF2C2C2E),
    error = Color(0xFFFF3B30),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF5A1A18),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFFFFFFF),
    inverseOnSurface = Color(0xFF000000),
    scrim = Color(0xFF000000),
)

// TokiMusicUI 浅色结构色板 + TokiSport 种子紫主色。
private val LightScheme = lightColorScheme(
    primary = Seed,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE7E3EF),
    onPrimaryContainer = Color(0xFF1C1C1E),
    secondaryContainer = Color(0xFFE9E9ED),
    onSecondaryContainer = Color(0xFF1C1C1E),
    tertiaryContainer = Color(0xFFE9E9ED),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1C1E),
    surfaceDim = Color(0xFFDDDDE2),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F7F9),
    surfaceContainer = Color(0xFFF2F2F4),
    surfaceContainerHigh = Color(0xFFE9E9ED),
    surfaceContainerHighest = Color(0xFFDEDEE5),
    surfaceVariant = Color(0xFFF2F2F4),
    onSurfaceVariant = Color(0xFF6E6E73),
    outline = Color(0xFFC6C6C8),
    outlineVariant = Color(0xFFE3E3E8),
    error = Color(0xFFFF3B30),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF5A1A18),
    inverseSurface = Color(0xFF1C1C1E),
    inverseOnSurface = Color(0xFFFFFFFF),
    scrim = Color(0xFF000000),
)

@Composable
fun TokiSportTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val scheme = if (dark) DarkScheme else LightScheme
    MaterialTheme(
        colorScheme = scheme,
        typography = AppTypography,
        shapes = AppShapes,
    ) {
        // MD3E 主题不默认提供 LocalContentColor，根部显式给 onSurface，避免深色下黑字。
        CompositionLocalProvider(
            LocalContentColor provides scheme.onSurface,
            LocalDarkTheme provides dark,
        ) {
            content()
        }
    }
}
