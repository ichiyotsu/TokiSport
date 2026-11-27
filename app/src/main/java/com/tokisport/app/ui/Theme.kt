package com.tokisport.app.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.unit.dp
import com.tokisport.app.R

// TokiUI 0.2.0-b1 / MD3E：
// - 品牌种子色 #6750A4；系统支持时优先动态取色（HCT），否则用种子色方案。
// - 全局字体 Inter（可变字体，内嵌）+ Noto Sans SC 中文回退，与 TokiStudio 的字体搭配一致；
//   字重只用 400 / 700（500 / 600 仅个别标题经可变字体轴取真字重）。
// - 队伍分数专用字体 Google Sans Flex（ScoreFontFamily），只用于比分与积分数字。
// - 圆角走语义阶梯 shape token：compact 4 / subtle 8 / standard 12 / emphasized 16 / large 28 / full。
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

/** TokiUI 语义圆角阶梯（shape token）：4 / 8 / 12 / 16 / 28，不对称圆角清零。 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private val LightScheme = lightColorScheme(
    primary = Seed,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiaryContainer = Color(0xFFFFD8E4),
    surface = Color(0xFFFEF7FF),
    onSurface = Color(0xFF1D1B20),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiaryContainer = Color(0xFF633B48),
    surface = Color(0xFF141218),
    onSurface = Color(0xFFE6E0E9),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
)

@Composable
fun TokiSportTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val scheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (dark) DarkScheme else LightScheme
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = AppTypography,
        shapes = AppShapes,
    ) {
        // MD3E 主题不默认提供 LocalContentColor，根部显式给 onSurface，避免深色下黑字。
        CompositionLocalProvider(LocalContentColor provides scheme.onSurface) {
            content()
        }
    }
}
