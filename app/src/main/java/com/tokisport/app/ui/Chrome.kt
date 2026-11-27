package com.tokisport.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState

/**
 * 按压缩放反馈（TokiMusicUI《组件》按钮族：玻璃圆钮带 pressScale，默认缩到 0.92）。
 * 与 clickable 共用同一个 [MutableInteractionSource]，按压时整钮等比缩小、松手回弹。
 */
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    scaleDown: Float = 0.92f,
): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) scaleDown else 1f,
        label = "pressScale",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * 全应用唯一的标题头（TokiMusicUI《组件》统一标题头口径，与 TokiMusic OverlayTitleHeader 同构）：
 * 标题 chip（全圆、headlineMedium Bold）+ 返回 chip（56dp 圆、24dp 图标），同磨砂、恒显示，
 * 返回与标题间距固定 12dp；横向边距 24dp、最小高 64dp（含状态栏 padding，无额外纵向 padding，
 * 56dp chip 在 64dp 内居中自然形成上下 4dp 呼吸）。
 * strong 形态与底栏同配方（32dp 模糊 + 45% veil）——本应用标题头恒用 strong。
 * 标题切换走淡入 + 上滑过渡；实测高度经 [onHeightMeasured] 回传，内容区据此留白（不硬编码）。
 * 一级页（竞技 / 积分）标题行右侧放头像钮（trailing），点按进「我的」。
 */
@Composable
fun OverlayTitleHeader(
    title: String,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onHeightMeasured: ((Dp) -> Unit)? = null,
) {
    val density = LocalDensity.current
    val titleShape = RoundedCornerShape(percent = 50)
    Row(
        modifier
            .fillMaxWidth()
            .onSizeChanged { size ->
                onHeightMeasured?.invoke(with(density) { size.height.toDp() })
            }
            .statusBarsPadding()
            .heightIn(min = 64.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                GlassChipButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "返回",
                    hazeState = hazeState,
                    onClick = onBack,
                )
                Spacer(Modifier.width(12.dp))
            }
            // 标题 chip：玻璃底只挂在 matchParentSize 兄弟层，文字是决定尺寸的常规子元素。
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .heightIn(min = 56.dp)
                    .clip(titleShape),
                contentAlignment = Alignment.CenterStart,
            ) {
                TokiGlassChipBackdrop(
                    shape = titleShape,
                    modifier = Modifier.matchParentSize(),
                    hazeState = hazeState,
                    strong = true,
                )
                // 标题切换：淡入 + 上滑（与 TokiMusic 同口径；本应用未启用 motionScheme，用 tween）。
                AnimatedContent(
                    targetState = title,
                    transitionSpec = {
                        (fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }) togetherWith
                            (fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it / 4 })
                    },
                    label = "headerTitle",
                ) { currentTitle ->
                    Text(
                        text = currentTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

/** 玻璃圆钮（TokiMusicUI 按钮族：56dp 圆、图标 24dp、与标题同磨砂、pressScale 按压）。 */
@Composable
fun GlassChipButton(
    icon: ImageVector,
    contentDescription: String?,
    hazeState: HazeState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier
            .size(56.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            .pressScale(interaction),
        contentAlignment = Alignment.Center,
    ) {
        TokiGlassChipBackdrop(
            shape = CircleShape,
            modifier = Modifier.matchParentSize(),
            hazeState = hazeState,
            strong = true,
        )
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * 顶栏右侧头像钮（竞技 / 积分页）：56dp 玻璃圆钮内嵌 36dp 通用账号头像
 * （无用户照片，用 Person 图标 + 主色容器底），点按进「我的」。
 */
@Composable
fun AvatarButton(hazeState: HazeState, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        Modifier
            .size(56.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            .pressScale(interaction),
        contentAlignment = Alignment.Center,
    ) {
        TokiGlassChipBackdrop(
            shape = CircleShape,
            modifier = Modifier.matchParentSize(),
            hazeState = hazeState,
            strong = true,
        )
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = "我的",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

/** 底栏条目（纯图标）：64×56dp、图标 24dp，选中只靠 pill 底色，图标色恒 onSurface。 */
data class NavEntry(val icon: ImageVector, val label: String)

/**
 * 悬浮底栏（TokiMusicUI 口径）：导航胶囊走加强档玻璃（32dp + 45% veil）。
 * 玻璃只挂在唯一的 matchParentSize 兄弟层上——条目行是唯一决定尺寸的常规子元素
 * （两个 matchParentSize 子元素会让胶囊塌成 0，TokiStudio 0.1.7 的坑）。
 */
@Composable
fun FloatingNavBar(
    entries: List<NavEntry>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(percent = 50)
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .matchParentSize()
                .clip(shape)
                .then(
                    if (TokiBlurCapability.canSampleInWindow) {
                        Modifier.tokiContentGlassStrong(hazeState)
                    } else {
                        Modifier
                    },
                )
                .background(scheme.surfaceContainer.copy(alpha = TokiGlassParams.strongVeilAlpha)),
        )
        Row(modifier = Modifier.padding(8.dp)) {
            entries.forEachIndexed { index, entry ->
                val selected = index == selectedIndex
                val interaction = remember { MutableInteractionSource() }
                Box(
                    Modifier
                        .size(width = 64.dp, height = 56.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(if (selected) tokiPillColor() else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable(
                            interactionSource = interaction,
                            indication = ripple(bounded = true),
                            onClick = { onSelect(index) },
                        )
                        .pressScale(interaction),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = entry.icon,
                        contentDescription = entry.label,
                        modifier = Modifier.size(24.dp),
                        tint = scheme.onSurface,
                    )
                }
            }
        }
    }
}
