package com.tokisport.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tokisport.app.data.Source

/**
 * 「我的」页（TokiMusicUI《页面规范》「我的」结构口径）：头像卡 + 分组列表。
 * 原设置页的全部功能并入这里：数据源三开关（Google Sports / 直播吧 / 懂球帝）、
 * 综合结果说明、关于（版本 / 设计 / 数据）。底栏不再有设置 tab，从顶栏头像进入。
 * 头像是通用账号头像（无用户照片，用 Person 图标）。
 */
@Composable
fun ProfileScreen(
    enabled: Set<Source>,
    onToggle: (Source, Boolean) -> Unit,
) {
    LazyColumn(modifier = Modifier.padding(horizontal = 24.dp)) {
        // 头像卡
        item {
            Row(
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "TokiSport",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "本地快照数据 · 无需登录",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        // 数据源分组（原设置页开关，逻辑不变、仅搬家）
        item { SectionTitle("数据源") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Source.entries.forEachIndexed { index, source ->
                    GroupedRow(
                        position = positionOf(index, Source.entries.size),
                        title = source.label,
                        subtitle = source.role,
                        trailing = {
                            Switch(
                                checked = source in enabled,
                                onCheckedChange = { onToggle(source, it) },
                            )
                        },
                    )
                }
            }
        }
        item {
            Text(
                "页面展示已启用数据源的综合结果：同一记录只显示一次并标注来源；只被已关闭来源核实的记录会暂时隐藏。三个来源应当一致，若将来同步发现冲突，按多数决（3 中取 2）采信并标记。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        // 关于分组
        item { SectionTitle("关于") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                GroupedRow(
                    position = SegmentPosition.First,
                    title = "版本",
                    subtitle = "TokiSport 0.1.4",
                )
                GroupedRow(
                    position = SegmentPosition.Middle,
                    title = "设计",
                    subtitle = "TokiMusicUI 设计规范（MD3E）：玻璃标题头与悬浮底栏、分组列表；" +
                        "全局字体 Inter（中文回退 Noto Sans SC），队伍分数使用 Google Sans Flex",
                )
                GroupedRow(
                    position = SegmentPosition.Last,
                    title = "数据",
                    subtitle = "本地快照（2026-11-27 核对），来源 Google Sports / 直播吧 / 懂球帝；" +
                        "球队队徽来自 TheSportsDB 并内嵌于应用（离线可用）。" +
                        "Google Sports 无官方免费 API，后续可通过 SerpApi / SearchApi 接入实时同步。",
                )
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}
