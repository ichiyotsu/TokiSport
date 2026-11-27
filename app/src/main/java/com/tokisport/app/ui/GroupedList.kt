package com.tokisport.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 分组列表（TokiMusicUI《组件》分组列表 + 《形状与间距》分段圆角口径）：
 * 一组一个容器序列、组内相邻卡间距 4dp、组间 12dp；分段圆角大 28dp / 小 4dp
 * （首项只圆上、末项只圆下、中间全小角、单项全大角）；行最小高 64dp，
 * 图标 24dp，标题 titleMedium，说明 bodySmall（onSurfaceVariant，最多 2 行）。
 */
enum class SegmentPosition { First, Middle, Last, Single }

fun positionOf(index: Int, count: Int): SegmentPosition = when {
    count <= 1 -> SegmentPosition.Single
    index == 0 -> SegmentPosition.First
    index == count - 1 -> SegmentPosition.Last
    else -> SegmentPosition.Middle
}

fun segmentedCorners(
    position: SegmentPosition,
    big: Dp = 28.dp,
    small: Dp = 4.dp,
): RoundedCornerShape = when (position) {
    SegmentPosition.Single -> RoundedCornerShape(big)
    SegmentPosition.First -> RoundedCornerShape(topStart = big, topEnd = big, bottomStart = small, bottomEnd = small)
    SegmentPosition.Middle -> RoundedCornerShape(small)
    SegmentPosition.Last -> RoundedCornerShape(topStart = small, topEnd = small, bottomStart = big, bottomEnd = big)
}

/** 分组标题（TokiMusicUI SectionTitle：titleLarge，上 24dp / 下 8dp）。 */
@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
    )
}

/** 设置系分组行（「我的」页与各分组列表共用）。 */
@Composable
fun GroupedRow(
    position: SegmentPosition,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Surface(
        shape = segmentedCorners(position),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .heightIn(min = 64.dp)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(12.dp))
                trailing()
            }
        }
    }
}
