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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tokisport.app.data.MatchResult
import com.tokisport.app.data.Merger
import com.tokisport.app.data.Source
import com.tokisport.app.data.TeamRecord
import com.tokisport.app.data.Tournament

// 本轮按 TokiMusicUI 规范 + 用户 6 条重构：
// - 一级页改名：主页 →「竞技」、战绩 →「积分」；底栏只剩这两页，设置并入「我的」（顶栏头像进入）。
// - 竞技页：顶部头条只保留 焦点比赛·时间 + 标题 + 一句话介绍（点按进焦点二级页看焦点数据），
//   以下不分区，直接按联赛（赛事）分栏统一展示全部比赛成绩（赛事内按阶段分组）。
// - 球队徽标改用真实队徽（见 Crests.kt），查不到才回退首字圆 / 国旗。
// - 比赛行全局可点：点任何一场比赛进比赛详情页（见 DetailScreens.kt）。
// 设计体系：TokiMusicUI（玻璃 chrome、分组列表、形状阶梯）；分数仍走 ScoreText（Google Sans Flex）。

// ---------- 通用小组件 ----------

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

/** Sofascore 式横向赛事筛选条（积分页用）：一排可横滑的筛选芯片。 */
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

fun teamLabel(labels: Map<String, String>, teamId: String): String =
    labels[teamId] ?: teamId

/** 从「🇪🇸 西班牙」式标签里拆出纯队名。 */
fun teamNameOnly(labels: Map<String, String>, teamId: String): String {
    val label = teamLabel(labels, teamId)
    return label.substringAfter(' ', label)
}

/** 从「🇪🇸 西班牙」式标签里拆出国旗（无队徽时的兜底）。 */
fun teamFlagOnly(labels: Map<String, String>, teamId: String): String {
    val label = teamLabel(labels, teamId)
    return label.substringBefore(' ', "")
}

// ---------- Sofascore 式比赛行（竞技 / 球队详情 / 焦点页共用，全局可点） ----------

/**
 * 一行球队（队名 + 该队在本场的强弱呈现）：获胜方加粗，落败方弱化。
 * 徽标用真实队徽；只有比分标签的球队同样先查队徽，查不到才用国旗 emoji。
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
        } else {
            TeamCrestById(teamId, flagFallback = teamFlagOnly(labels, teamId), size = 22)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = record?.nameCn ?: teamNameOnly(labels, teamId),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal,
            color = if (dimmed) scheme.onSurfaceVariant else scheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Sofascore 式比赛行：左固定宽状态列（日期 / 完场），中两行球队，右两行比分对齐成列。
 * 胜负强调由真实比分推导：胜方队名+比分加粗，负方弱化为次要色；平局两边同等呈现。
 * 整行可点，进比赛详情页。
 */
@Composable
fun MatchRow(
    match: MatchResult,
    teamsById: Map<String, TeamRecord>,
    labels: Map<String, String>,
    enabled: Set<Source>,
    onClick: (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val homeWin = match.homeScore > match.awayScore
    val awayWin = match.awayScore > match.homeScore
    Column(
        modifier = Modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
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
    onMatchClick: (MatchResult) -> Unit,
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
                MatchRow(match, teamsById, labels, enabled, onClick = { onMatchClick(match) })
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

@Composable
fun NoSourceState(onOpenProfile: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("暂无可展示的数据", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "三个数据源已全部关闭。请在「我的」中至少开启一个数据源，页面会展示已启用来源的综合结果。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onOpenProfile) { Text("去开启") }
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
 * Sofascore 式积分表：名次 + 左侧色条（出线/冠军标示）、队徽+队名、紧凑数字列，积分列最强。
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
                Spacer(Modifier.width(30.dp)) // 队徽 + 间隔
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
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
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
                        color = if (isChampion) scheme.onPrimaryContainer else scheme.onSurface,
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

/** 积分排序比较器：积分 → 净胜球 → 进球。 */
fun standingsOrder(): Comparator<TeamRecord> =
    compareByDescending<TeamRecord> { it.points }
        .thenByDescending { it.gd ?: Int.MIN_VALUE }
        .thenByDescending { it.gf ?: Int.MIN_VALUE }

/** 该队是否处于出线标示状态：以数据里的 rankLabel 为准（晋级 / 已出线）。 */
fun rankLabelQualifies(team: TeamRecord): Boolean =
    team.rankLabel.contains("晋级") || team.rankLabel.contains("已出线")

// ---------- 竞技页 ----------

/**
 * 竞技页（原主页）：
 * - 顶部头条只保留：焦点比赛（赛事 · 阶段 · 时间）+ 标题 + 一句话介绍；点按进焦点二级页（焦点数据在那边）。
 * - 以下不设其他分区：全部赛事的比赛成绩统一展示，按联赛（赛事）分栏，赛事内按阶段分组。
 */
@Composable
fun ArenaScreen(
    tournaments: List<Tournament>,
    labelsByTournament: Map<String, Map<String, String>>,
    enabled: Set<Source>,
    onOpenProfile: () -> Unit,
    onOpenFocus: (Tournament) -> Unit,
    onOpenMatch: (Tournament, MatchResult) -> Unit,
) {
    if (enabled.isEmpty()) {
        NoSourceState(onOpenProfile)
        return
    }
    val headline = tournaments.firstOrNull()
    LazyColumn(modifier = Modifier.padding(horizontal = 24.dp)) {
        // 顶部头条
        if (headline != null) {
            item {
                HeadlineCard(
                    tournament = headline,
                    labels = labelsByTournament[headline.id] ?: emptyMap(),
                    onClick = { onOpenFocus(headline) },
                )
            }
        }
        // 按联赛分栏：每项赛事一栏，栏内按阶段分组统一展示全部比赛成绩
        tournaments.forEach { tournament ->
            item { SectionTitle(tournament.name) }
            val teamsById = tournament.teams.associateBy { it.id }
            val labels = labelsByTournament[tournament.id] ?: emptyMap()
            val groups = tournament.matches.groupBy { it.stage }.entries.toList()
            items(groups) { (stage, groupMatches) ->
                MatchGroupContainer(
                    header = "🏆 ${tournament.shortName} · $stage",
                    matches = groupMatches,
                    teamsById = teamsById,
                    labels = labels,
                    enabled = enabled,
                    onMatchClick = { match -> onOpenMatch(tournament, match) },
                )
            }
            item {
                Text(
                    tournament.dataScope,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

/**
 * 顶部头条卡：仅保留 比赛·时间（焦点比赛的赛事 · 阶段 · 日期 + 紧凑比分行）、
 * 标题（heroTitle）与一句话介绍（heroBody 单行截断）。点按进焦点二级页。
 */
@Composable
private fun HeadlineCard(
    tournament: Tournament,
    labels: Map<String, String>,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val teamsById = remember(tournament) { tournament.teams.associateBy { it.id } }
    val featured = tournament.matches.firstOrNull()
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = scheme.primaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (featured != null) {
                Text(
                    "🏆 ${tournament.name} · ${featured.stage} · ${featured.date}",
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val home = teamsById[featured.homeTeamId]
                    if (home != null) TeamBadge(home, size = 28)
                    else TeamCrestById(featured.homeTeamId, teamFlagOnly(labels, featured.homeTeamId), 28)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        home?.nameCn ?: teamNameOnly(labels, featured.homeTeamId),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.width(10.dp))
                    ScoreText(
                        "${featured.homeScore} - ${featured.awayScore}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        teamsById[featured.awayTeamId]?.nameCn
                            ?: teamNameOnly(labels, featured.awayTeamId),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.width(8.dp))
                    val away = teamsById[featured.awayTeamId]
                    if (away != null) TeamBadge(away, size = 28)
                    else TeamCrestById(featured.awayTeamId, teamFlagOnly(labels, featured.awayTeamId), 28)
                }
                Spacer(Modifier.height(10.dp))
            }
            Text(
                tournament.heroTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                tournament.heroBody,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ---------- 积分页（原战绩页） ----------

@Composable
fun StandingsScreen(
    tournament: Tournament,
    enabled: Set<Source>,
    onOpenProfile: () -> Unit,
    onOpenTeam: (TeamRecord) -> Unit,
) {
    if (enabled.isEmpty()) {
        NoSourceState(onOpenProfile)
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
                    "按小组积分排序 · 左侧色条为出线区（组内前二或已标注晋级）· 点球队查看介绍与战绩"
                } else {
                    "按综合积分排序 · 点球队查看介绍与战绩"
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
