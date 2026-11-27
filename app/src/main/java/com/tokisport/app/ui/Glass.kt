package com.tokisport.app.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeFeatureFlags
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur

/**
 * 玻璃与材质（TokiMusicUI《玻璃与材质》口径，Haze 实现同 TokiMusic）：
 * - 玻璃是唯一 chrome 外观：标题头与悬浮底栏走加强档（32dp 模糊 + surfaceContainer 45% veil），
 *   原 TokiUI 的实色回退预设（实色面 + 3dp primary 描边）已按 TokiMusicUI 删除。
 * - tint：深色白 30%（加强档）/ 20%（普通档）；浅色暖深 #2A2622 16% + 白 46%。
 * - 噪点 0.02 全档同值；性能模式 Quality。
 * - 尺寸会变的表面（导航胶囊、标题 chip）用内容源采样（HazeInput.Sources），避免尺寸动画黑边。
 * - 采样不可用（API < 31）时 Haze 样式自带 fallback 上色兜底：深色黑 45% / 浅色白 72%；
 *   本应用 minSdk 36，正常设备恒可采样。
 */
@OptIn(ExperimentalHazeApi::class)
object TokiBlurCapability {
    init {
        HazeFeatureFlags.isPlatformBackdropEnabled = true
    }

    val canSampleInWindow: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}

object TokiGlassParams {
    val blurRadius: Dp = 24.dp
    const val noise: Float = 0.02f

    /** 加强档：标题头与悬浮底栏同配方。 */
    val strongBlurRadius: Dp = 32.dp
    const val strongVeilAlpha: Float = 0.45f
    const val strongDarkTintAlpha: Float = 0.30f

    fun tints(dark: Boolean): List<HazeColorEffect> = if (dark) {
        listOf(HazeColorEffect.tint(Color.White.copy(alpha = 0.20f)))
    } else {
        listOf(
            HazeColorEffect.tint(Color(0xFF2A2622).copy(alpha = 0.16f)),
            HazeColorEffect.tint(Color.White.copy(alpha = 0.46f)),
        )
    }

    fun strongTints(dark: Boolean): List<HazeColorEffect> = if (dark) {
        listOf(HazeColorEffect.tint(Color.White.copy(alpha = strongDarkTintAlpha)))
    } else {
        listOf(
            HazeColorEffect.tint(Color(0xFF2A2622).copy(alpha = 0.16f)),
            HazeColorEffect.tint(Color.White.copy(alpha = 0.46f)),
        )
    }

    fun fallback(dark: Boolean): Color =
        if (dark) Color.Black.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.72f)
}

internal fun tokiBlurStyle(dark: Boolean, strong: Boolean = false): HazeBlurStyle =
    HazeBlurStyle {
        blurEnabled(true)
        blurRadius(if (strong) TokiGlassParams.strongBlurRadius else TokiGlassParams.blurRadius)
        noiseFactor(TokiGlassParams.noise)
        backgroundColor(Color.Transparent)
        colorEffects(if (strong) TokiGlassParams.strongTints(dark) else TokiGlassParams.tints(dark))
        fallbackColorEffect(HazeColorEffect.tint(TokiGlassParams.fallback(dark)))
    }

/** 普通档内容源玻璃（24dp）。 */
@Composable
fun Modifier.tokiContentGlass(state: HazeState): Modifier {
    val dark = LocalDarkTheme.current
    return hazeBlur(
        input = HazeInput.Sources(state),
        style = tokiBlurStyle(dark),
        performanceMode = HazePerformanceMode.Quality,
    )
}

/** 加强档内容源玻璃（32dp）：悬浮底栏与悬浮标题共用。 */
@Composable
fun Modifier.tokiContentGlassStrong(state: HazeState): Modifier {
    val dark = LocalDarkTheme.current
    return hazeBlur(
        input = HazeInput.Sources(state),
        style = tokiBlurStyle(dark, strong = true),
        performanceMode = HazePerformanceMode.Quality,
    )
}

/**
 * 标题 / 返回 chip 的玻璃底（TokiMusic TokiScrollChipBackdrop 同构）：
 * 内容源玻璃 + surfaceContainer veil（加强档 45% / 普通档 24%）+ 1dp outlineVariant 45% 描边。
 * 采样不可用或没有 HazeState 时退化为以 veil 透明度平涂的 surfaceContainer（兜底，不是预设外观）。
 */
@Composable
fun TokiGlassChipBackdrop(
    shape: Shape,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    strong: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val frosted = TokiBlurCapability.canSampleInWindow
    val veil = if (strong) TokiGlassParams.strongVeilAlpha else 0.24f
    Box(
        modifier
            .clip(shape)
            .then(
                if (frosted && hazeState != null) {
                    (if (strong) Modifier.tokiContentGlassStrong(hazeState)
                    else Modifier.tokiContentGlass(hazeState))
                        .background(scheme.surfaceContainer.copy(alpha = veil))
                        .border(1.dp, scheme.outlineVariant.copy(alpha = 0.45f), shape)
                } else {
                    Modifier.background(scheme.surfaceContainer.copy(alpha = veil))
                },
            ),
    )
}
