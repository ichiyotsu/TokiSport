package com.tokisport.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tokisport.app.data.Fixture
import com.tokisport.app.data.Lineup
import com.tokisport.app.data.MatchResult
import com.tokisport.app.data.Merger
import com.tokisport.app.data.Source
import com.tokisport.app.data.TeamRecord
import com.tokisport.app.data.Tournament

// 本轮布局参考 Sofascore 重构（设计体系仍为 TokiUI 0.2.0-b1 / MD3E）：
// - 共享比赛行：左固定宽状态列（日期 + 完场），中两行球队（徽标 + 队名），右两行比分对齐成列；
//   获胜方队名与比分加粗，落败方弱化（平局两边同等），胜负直接由真实比分算出。
// - 比赛按赛事阶段分组：一组一个容器 + 组头行 + 细分隔线（与全应用分组列表规则一致）。
// - 积分表：名次左侧色条标示出线/冠军，紧凑数字列（赛/胜/平/负/进:失/净/分），数字全走 ScoreText，积分列最强。
// - 信息密度向 Sofascore 靠拢：行内边距收紧，页边距保持 24dp。

// ---------- 通用小组件 ----------

/** 球队 logo 处理：国旗 emoji + 彩色首字圆（占位方案，不使用伪造队徽）。 */
@Composable
fun TeamBadge(team: TeamRecord, size: Int = 44) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(team.color)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = team.nameCn.take(1),
            color = Color.White,
            style = if (size >= 40) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun SourceTag(sources: Set<Source>, enabled: Set<Source>) {
    val text = Merger.attribution(sources, enabled)
    if (text.isNotEmpty()) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * 队伍分数专用文本：比分、积分、胜平负、进失球等数字统一用 Google Sans Flex
 * （ScoreFontFamily，中文标签经 Inter / Noto Sans SC 回退）。所有分数渲染必须走这里。
 */
@Composable
fun ScoreText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified,
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(fontFamily = ScoreFontFamily),
        fontWeight = fontWeight,
        color = color,
    )
}

@Composable
fun TournamentSwitcher(
    tournaments: List<Tournament>,
    selectedId: String,
    onSelect: (String) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        tournaments.forEachIndexed { index, t ->
            SegmentedButton(
                selected = t.id == selectedId,
                onClick = { onSelect(t.id) },
                shape = SegmentedButtonDefaults.itemShape(index, tournaments.size),
            ) { Text(t.shortName) }
        }
    }
}

/** Sofascore 式横向赛事筛选条（战绩页用）：一排可横滑的筛选芯片。 */
@Composable
fun TournamentChipRow(
    tournaments: List<Tournament>,
    selectedId: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tournaments.forEach { t ->
            FilterChip(
                selected = t.id == selectedId,
                onClick = { onSelect(t.id) },
                label = { Text(t.shortName) },
            )
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
    )
}

fun teamLabel(labels: Map<String, String>, teamId: String): String =
    labels[teamId] ?: teamId

/** 从「🇪🇸 西班牙」式标签里拆出纯队名（徽标圆已承担 logo 时使用）。 */
private fun teamNameOnly(labels: Map<String, String>, teamId: String): String {
    val label = teamLabel(labels, teamId)
    return label.substringAfter(' ', label)
}

/** 从「🇪🇸 西班牙」式标签里拆出国旗（无战绩记录的球队没有徽标圆时使用）。 */
private fun teamFlagOnly(labels: Map<String, String>, teamId: String): String {
    val label = teamLabel(labels, teamId)
    return label.substringBefore(' ', "")
}

/** 按阶段分组（保持数据原有顺序），整组累积到至少 [minCount] 场，避免把一组拦腰截断。 */
private fun takeWholeGroups(
    matches: List<MatchResult>,
    minCount: Int,
): List<Pair<String, List<MatchResult>>> {
    val groups = matches.groupBy { it.stage }.entries.toList()
    val result = mutableListOf<Pair<String, List<MatchResult>>>()
    var count = 0
    for ((stage, groupMatches) in groups) {
        if (count >= minCount) break
        result += stage to groupMatches
        count += groupMatches.size
    }
    return result
}

// ---------- Sofascore 式比赛行（主页 / 战绩 / 球队详情共用） ----------

/**
 * 一行球队（队名 + 该队在本场的强弱呈现）：获胜方加粗，落败方弱化。
 * 有战绩记录的球队用彩色首字圆作徽标；只有比分标签的球队用国旗 emoji。
 */
@Composable
private fun MatchTeamLine(
    teamId: String,
    teamsById: Map<String, TeamRecord>,
    labels: Map<String, String>,
    emphasized: Boolean,
    dimmed: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    val record = teamsById[teamId]
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (record != null) {
            TeamBadge(record, size = 22)
            Spacer(Modifier.width(8.dp))
            Text(
                text = record.nameCn,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal,
                color = if (dimmed) scheme.onSurfaceVariant else scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Text(
                text = teamFlagOnly(labels, teamId),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = teamNameOnly(labels, teamId),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal,
                color = if (dimmed) scheme.onSurfaceVariant else scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Sofascore 式比赛行：左固定宽状态列（日期 / 完场），中两行球队，右两行比分对齐成列。
 * 胜负强调由真实比分推导：胜方队名+比分加粗，负方弱化为次要色；平局两边同等呈现。
 */
@Composable
fun MatchRow(
    match: MatchResult,
    teamsById: Map<String, TeamRecord>,
    labels: Map<String, String>,
    enabled: Set<Source>,
) {
    val scheme = MaterialTheme.colorScheme
    val homeWin = match.homeScore > match.awayScore
    val awayWin = match.awayScore > match.homeScore
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 左：固定宽状态列
            Column(modifier = Modifier.width(56.dp)) {
                Text(
                    text = "完场",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurfaceVariant,
                )
                Text(
                    text = match.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
            // 状态列与球队列之间的细竖线
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(38.dp)
                    .background(scheme.outlineVariant),
            )
            Spacer(Modifier.width(12.dp))
            // 中：两行球队
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                MatchTeamLine(match.homeTeamId, teamsById, labels,
                    emphasized = homeWin, dimmed = awayWin)
                MatchTeamLine(match.awayTeamId, teamsById, labels,
                    emphasized = awayWin, dimmed = homeWin)
            }
            // 右：两行比分，对齐成列
            Column(
                modifier = Modifier.width(30.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                ScoreText(
                    text = match.homeScore.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (homeWin) FontWeight.Bold else FontWeight.Normal,
                    color = if (awayWin) scheme.onSurfaceVariant else scheme.onSurface,
                )
                ScoreText(
                    text = match.awayScore.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (awayWin) FontWeight.Bold else FontWeight.Normal,
                    color = if (homeWin) scheme.onSurfaceVariant else scheme.onSurface,
                )
            }
        }
        if (match.note.isNotEmpty() || match.sources.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.padding(start = 69.dp)) {
                if (match.note.isNotEmpty()) {
                    Text(
                        match.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(8.dp))
                }
                SourceTag(match.sources, enabled)
            }
        }
    }
}

/**
 * 一组比赛的分组容器：组头行（🏆 + 赛事名 · 阶段 + 场数）+ 容器内比赛行 + 细分隔线。
 */
@Composable
fun MatchGroupContainer(
    header: String,
    matches: List<MatchResult>,
    teamsById: Map<String, TeamRecord>,
    labels: Map<String, String>,
    enabled: Set<Source>,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = header,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${matches.size} 场",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            matches.forEachIndexed { index, match ->
                MatchRow(match, teamsById, labels, enabled)
                if (index < matches.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }
        }
    }
}

/** 焦点比赛卡（Sofascore 比赛页头式）：大号比分居中，两队徽标+队名分列两侧，胜方加粗。 */
@Composable
fun FeaturedMatchCard(
    tournament: Tournament,
    match: MatchResult,
    teamsById: Map<String, TeamRecord>,
    labels: Map<String, String>,
    enabled: Set<Source>,
) {
    val scheme = MaterialTheme.colorScheme
    val homeWin = match.homeScore > match.awayScore
    val awayWin = match.awayScore > match.homeScore
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = scheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "🏆 ${tournament.name} · ${match.stage} · ${match.date}",
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 主队
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val home = teamsById[match.homeTeamId]
                    if (home != null) {
                        TeamBadge(home, size = 48)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            home.nameCn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (homeWin) FontWeight.Bold else FontWeight.Normal,
                            color = if (awayWin) scheme.onSurfaceVariant else scheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        Text(teamFlagOnly(labels, match.homeTeamId),
                            style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            teamNameOnly(labels, match.homeTeamId),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (homeWin) FontWeight.Bold else FontWeight.Normal,
                            color = if (awayWin) scheme.onSurfaceVariant else scheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                // 大比分
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ScoreText(
                        text = "${match.homeScore} - ${match.awayScore}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "完场",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
                // 客队
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val away = teamsById[match.awayTeamId]
                    if (away != null) {
                        TeamBadge(away, size = 48)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            away.nameCn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (awayWin) FontWeight.Bold else FontWeight.Normal,
                            color = if (homeWin) scheme.onSurfaceVariant else scheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        Text(teamFlagOnly(labels, match.awayTeamId),
                            style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            teamNameOnly(labels, match.awayTeamId),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (awayWin) FontWeight.Bold else FontWeight.Normal,
                            color = if (homeWin) scheme.onSurfaceVariant else scheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            if (match.note.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    match.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            SourceTag(match.sources, enabled)
        }
    }
}

@Composable
fun NoSourceState(onGoSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("暂无可展示的数据", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "三个数据源已全部关闭。请在「设置」中至少开启一个数据源，页面会展示已启用来源的综合结果。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onGoSettings) { Text("去设置开启") }
    }
}

// ---------- 积分表（Sofascore 式） ----------

/** 积分表数字列宽（紧凑排列）：赛/胜/平/负/进:失/净/分。 */
private object TableCols {
    val pos = 24.dp
    val played = 20.dp
    val won = 20.dp
    val drawn = 20.dp
    val lost = 20.dp
    val gls = 42.dp
    val gd = 28.dp
    val pts = 28.dp
}

@Composable
private fun TableHeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(width),
    )
}

@Composable
private fun TableNumCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    bold: Boolean = false,
    color: Color = Color.Unspecified,
) {
    Box(modifier = Modifier.width(width), contentAlignment = Alignment.Center) {
        ScoreText(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = color,
        )
    }
}

/**
 * Sofascore 式积分表：名次 + 左侧色条（出线/冠军标示）、徽标+队名、紧凑数字列，积分列最强。
 * [teams] 须已按名次排好序；[qualifying] 决定某行是否显示出线色条。
 */
@Composable
fun StandingsTable(
    teams: List<TeamRecord>,
    onOpenTeam: (TeamRecord) -> Unit,
    qualifying: (team: TeamRecord, position: Int) -> Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.large,
        color = scheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            // 表头
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.width(3.dp))
                TableHeaderCell("#", TableCols.pos)
                Spacer(Modifier.width(30.dp)) // 徽标 + 间隔
                Text(
                    "球队",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                TableHeaderCell("赛", TableCols.played)
                TableHeaderCell("胜", TableCols.won)
                TableHeaderCell("平", TableCols.drawn)
                TableHeaderCell("负", TableCols.lost)
                TableHeaderCell("进:失", TableCols.gls)
                TableHeaderCell("净", TableCols.gd)
                TableHeaderCell("分", TableCols.pts)
            }
            teams.forEachIndexed { index, team ->
                val position = index + 1
                val isChampion = team.rankLabel == "冠军"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isChampion) scheme.primaryContainer else Color.Transparent,
                        )
                        .clickable { onOpenTeam(team) }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 名次左侧色条：出线区 / 冠军用主色，其余透明占位
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (isChampion || qualifying(team, position)) scheme.primary
                                else Color.Transparent,
                            ),
                    )
                    Box(
                        modifier = Modifier.width(TableCols.pos),
                        contentAlignment = Alignment.Center,
                    ) {
                        ScoreText(
                            text = position.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isChampion) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                    TeamBadge(team, size = 24)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isChampion) "${team.nameCn} 🏆" else team.nameCn,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isChampion) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    TableNumCell(team.played.toString(), TableCols.played)
                    TableNumCell(team.won.toString(), TableCols.won)
                    TableNumCell(team.drawn.toString(), TableCols.drawn)
                    TableNumCell(team.lost.toString(), TableCols.lost)
                    TableNumCell(
                        "${team.gf?.toString() ?: "—"}:${team.ga?.toString() ?: "—"}",
                        TableCols.gls,
                    )
                    TableNumCell(
                        team.gd?.let { if (it > 0) "+$it" else it.toString() } ?: "—",
                        TableCols.gd,
                    )
                    TableNumCell(
                        team.points.toString(),
                        TableCols.pts,
                        bold = true,
                        color = scheme.primary,
                    )
                }
                if (index < teams.lastIndex) {
                    HorizontalDivider(
                        color = scheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }
        }
    }
}

/** 积分排序比较器（与原战绩页一致：积分 → 净胜球 → 进球）。 */
private fun standingsOrder(): Comparator<TeamRecord> =
    compareByDescending<TeamRecord> { it.points }
        .thenByDescending { it.gd ?: Int.MIN_VALUE }
        .thenByDescending { it.gf ?: Int.MIN_VALUE }

/** 该队是否处于出线标示状态：以数据里的 rankLabel 为准（晋级 / 已出线）。 */
private fun rankLabelQualifies(team: TeamRecord): Boolean =
    team.rankLabel.contains("晋级") || team.rankLabel.contains("已出线")

// ---------- 主页概览 ----------

@Composable
fun HomeScreen(
    tournament: Tournament,
    labels: Map<String, String>,
    enabled: Set<Source>,
    onGoSettings: () -> Unit,
    onOpenRecords: () -> Unit,
    onOpenTeam: (TeamRecord) -> Unit,
) {
    if (enabled.isEmpty()) {
        NoSourceState(onGoSettings)
        return
    }
    val teamsById = remember(tournament) { tournament.teams.associateBy { it.id } }
    LazyColumn(modifier = Modifier.padding(horizontal = 24.dp)) {
        // 赛事焦点卡（冠军 / 中国队焦点事实保留在此）
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "${tournament.name} · ${tournament.status}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        tournament.heroTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        tournament.heroBody,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${tournament.dates} · ${tournament.venue}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
        // 焦点比赛（列表首场：世界杯为决赛，U17 为中国队最近一场）
        val featured = tournament.matches.firstOrNull()
        if (featured != null) {
            item {
                FeaturedMatchCard(tournament, featured, teamsById, labels, enabled)
            }
        }
        item { SectionTitle("焦点数据") }
        item {
            Column {
                tournament.focusStats.chunked(3).forEach { rowStats ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowStats.forEach { (label, value) ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                ),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    ScoreText(value, style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold)
                                    Text(label, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        repeat(3 - rowStats.size) { Spacer(Modifier.weight(1f).padding(4.dp)) }
                    }
                }
            }
        }
        // 比赛：按阶段分组的 Sofascore 式分组列表
        item { SectionTitle("比赛") }
        val homeGroups = takeWholeGroups(tournament.matches, minCount = 6)
        items(homeGroups) { (stage, groupMatches) ->
            MatchGroupContainer(
                header = "🏆 ${tournament.shortName} · $stage",
                matches = groupMatches,
                teamsById = teamsById,
                labels = labels,
                enabled = enabled,
            )
        }
        // 战绩预览：紧凑积分表（前 4 名）
        item { SectionTitle("战绩预览") }
        item {
            val preview = tournament.teams.sortedWith(standingsOrder()).take(4)
            StandingsTable(
                teams = preview,
                onOpenTeam = onOpenTeam,
                qualifying = { team, position -> position <= 2 || rankLabelQualifies(team) },
            )
        }
        item {
            TextButton(onClick = onOpenRecords) { Text("查看完整战绩 →") }
        }
        // 即将进行：分组容器内的赛程行
        item { SectionTitle("即将进行") }
        if (tournament.fixtures.isEmpty()) {
            item {
                Text(
                    "赛事已结束，暂无后续赛程。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        } else {
            item { FixtureGroup(fixtures = tournament.fixtures) }
        }
        item {
            Text(
                tournament.dataScope,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp),
            )
        }
    }
}

/** 赛程分组容器：左列阶段+日期，中列对阵说明，与比赛行同一视觉语言。 */
@Composable
private fun FixtureGroup(fixtures: List<Fixture>) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.large,
        color = scheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Column {
            fixtures.forEachIndexed { index, fixture ->
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.width(96.dp)) {
                        Text(
                            fixture.stage,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurfaceVariant,
                        )
                        Text(
                            fixture.date,
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(scheme.outlineVariant),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(fixture.label, style = MaterialTheme.typography.bodyMedium)
                        if (fixture.note.isNotEmpty()) {
                            Text(
                                fixture.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                if (index < fixtures.lastIndex) {
                    HorizontalDivider(
                        color = scheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }
        }
    }
}

// ---------- 战绩页 ----------

@Composable
fun RecordsScreen(
    tournament: Tournament,
    enabled: Set<Source>,
    onGoSettings: () -> Unit,
    onOpenTeam: (TeamRecord) -> Unit,
) {
    if (enabled.isEmpty()) {
        NoSourceState(onGoSettings)
        return
    }
    // 小组制赛事（group 形如「A组」）：按组出表，组内排序，名次为组内名次；
    // 淘汰赛汇总（世界杯）：单表呈现，冠军行高亮。
    val groupKeys = tournament.teams.map { it.group }.distinct()
    val isGroupStage = groupKeys.all { it.matches(Regex("^[A-Z]组$")) }
    LazyColumn(modifier = Modifier.padding(horizontal = 24.dp)) {
        item {
            Text(
                if (isGroupStage) {
                    "按小组积分排序 · 左侧色条为出线区（组内前二或已标注晋级）· 点球队查看比分与阵容"
                } else {
                    "按综合积分排序 · 点球队查看比分与阵容"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
            )
        }
        if (isGroupStage) {
            items(groupKeys) { group ->
                Column {
                    SectionTitle(group)
                    val groupTeams = tournament.teams
                        .filter { it.group == group }
                        .sortedWith(standingsOrder())
                    StandingsTable(
                        teams = groupTeams,
                        onOpenTeam = onOpenTeam,
                        qualifying = { team, position ->
                            position <= 2 || rankLabelQualifies(team)
                        },
                    )
                }
            }
        } else {
            item { Spacer(Modifier.height(8.dp)) }
            item {
                val sorted = tournament.teams.sortedWith(standingsOrder())
                StandingsTable(
                    teams = sorted,
                    onOpenTeam = onOpenTeam,
                    qualifying = { team, _ -> rankLabelQualifies(team) },
                )
            }
        }
        item {
            Text(
                tournament.dataScope,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp),
            )
        }
    }
}

// ---------- 球队详情 ----------

@Composable
fun TeamDetailScreen(
    tournament: Tournament,
    labels: Map<String, String>,
    team: TeamRecord,
    enabled: Set<Source>,
    onBack: () -> Unit,
) {
    val teamsById = remember(tournament) { tournament.teams.associateBy { it.id } }
    val matches = tournament.matches.filter { it.homeTeamId == team.id || it.awayTeamId == team.id }
    var detailTab by remember { mutableStateOf(0) }
    val tabNames = listOf("比赛", "数据", "阵容")
    LazyColumn(modifier = Modifier.padding(horizontal = 24.dp)) {
        // 头部：大徽标 + 队名 + 赛事/小组/名次/积分摘要（Sofascore 球队页头式）
        item {
            TextButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) { Text("← 返回") }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TeamBadge(team, 64)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("${team.flag} ${team.nameCn}", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Text(team.nameEn, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        tournament.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            ScoreText(
                "${team.group} · ${team.rankLabel} · 积分 ${team.points}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            TabRow(selectedTabIndex = detailTab) {
                tabNames.forEachIndexed { index, name ->
                    Tab(
                        selected = detailTab == index,
                        onClick = { detailTab = index },
                        text = { Text(name) },
                    )
                }
            }
        }
        when (detailTab) {
            0 -> {
                // 比赛：按阶段分组的共享比赛行
                if (matches.isEmpty()) {
                    item {
                        Text(
                            "该队比赛记录待同步。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    }
                } else {
                    val groups = matches.groupBy { it.stage }.entries.toList()
                    items(groups) { (stage, groupMatches) ->
                        Column {
                            SectionTitle(stage)
                            MatchGroupContainer(
                                header = "🏆 ${tournament.shortName} · $stage",
                                matches = groupMatches,
                                teamsById = teamsById,
                                labels = labels,
                                enabled = enabled,
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
            1 -> {
                // 数据：战绩统计行（标签 + ScoreText 数值）
                item { SectionTitle("战绩数据") }
                item {
                    val gdText = team.gd?.let { if (it > 0) "+$it" else it.toString() } ?: "—"
                    val statRows: List<Pair<String, String>> = listOf(
                        "积分" to team.points.toString(),
                        "场次" to team.played.toString(),
                        "胜" to team.won.toString(),
                        "平" to team.drawn.toString(),
                        "负" to team.lost.toString(),
                        "进球" to (team.gf?.toString() ?: "—"),
                        "失球" to (team.ga?.toString() ?: "—"),
                        "净胜球" to gdText,
                    )
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            statRows.forEachIndexed { index, (label, value) ->
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f),
                                    )
                                    ScoreText(
                                        value,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                if (index < statRows.lastIndex) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Text(
                            "${team.group} · ${team.rankLabel}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        if (team.scopeNote.isNotEmpty()) {
                            Text(
                                team.scopeNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        SourceTag(team.sources, enabled)
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
            else -> {
                // 阵容：按场次阵容优先逐场展示；否则回退到单阵容/待同步。
                // 快照名单只标注门将/队长，无完整位置信息，故不按位置编造分组，名单按原顺序呈现。
                item { SectionTitle("阵容") }
                val perMatchLineups = matches.mapNotNull { match ->
                    val opponentId =
                        if (match.homeTeamId == team.id) match.awayTeamId else match.homeTeamId
                    team.matchLineups[opponentId]?.let { lineup -> match to lineup }
                }
                if (perMatchLineups.isNotEmpty()) {
                    items(perMatchLineups) { (_, lineup) ->
                        LineupCard(lineup, enabled)
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                } else {
                    item {
                        val lineup = team.lineup
                        if (lineup == null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                ),
                            ) {
                                Text(
                                    "阵容数据待 Google Sports / 直播吧 / 懂球帝同步（或其来源已在设置中关闭）。",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp),
                                )
                            }
                        } else {
                            LineupCard(lineup, enabled)
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LineupCard(lineup: Lineup, enabled: Set<Source>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "${lineup.context} · 阵型 ${lineup.formation}",
                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            lineup.players.forEach { player ->
                Text(player, style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text("来源：${lineup.source}", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            SourceTag(lineup.sources, enabled)
        }
    }
}

// ---------- 设置页 ----------

@Composable
fun SettingsScreen(enabled: Set<Source>, onToggle: (Source, Boolean) -> Unit) {
    LazyColumn(modifier = Modifier.padding(horizontal = 24.dp)) {
        item { SectionTitle("数据源") }
        item {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Source.entries.forEachIndexed { index, source ->
                        ListItem(
                            headlineContent = { Text(source.label) },
                            supportingContent = { Text(source.role) },
                            trailingContent = {
                                Switch(
                                    checked = source in enabled,
                                    onCheckedChange = { onToggle(source, it) },
                                )
                            },
                        )
                        if (index < Source.entries.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
        item {
            Text(
                "页面展示已启用数据源的综合结果：同一记录只显示一次并标注来源；只被已关闭来源核实的记录会暂时隐藏。三个来源应当一致，若将来同步发现冲突，按多数决（3 中取 2）采信并标记。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp),
            )
        }
        item { SectionTitle("关于") }
        item {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("TokiSport 0.1.1", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "设计：MD3E + TokiUI 0.2.0-b1；全局字体 Inter（中文回退 Noto Sans SC），队伍分数使用 Google Sans Flex。数据为本地快照（2026-11-27 核对），" +
                            "Google Sports 无官方免费 API，后续可通过 SerpApi / SearchApi 接入实时同步。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
