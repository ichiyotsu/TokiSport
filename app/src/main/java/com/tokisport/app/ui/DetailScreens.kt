package com.tokisport.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import com.tokisport.app.data.Lineup
import com.tokisport.app.data.MatchResult
import com.tokisport.app.data.Source
import com.tokisport.app.data.TeamRecord
import com.tokisport.app.data.Tournament

// 二级页集合：
// - 焦点页：竞技页头条点按进入，承接原主页的焦点数据（用户第 1 条）。
// - 球队页：以介绍为主——简介、近五场状态、所在联赛积分、比赛、数据、阵容（用户第 5 条）。
// - 比赛详情页：全局可达（点任何一场比赛行），展示成绩、所在联赛、首发、进球记录与对抗数据。
// 数据口径：只展示快照里真实收录的数据；没有的（控球/射门/评分等）不编造，缺失处如实标注暂无数据。

// ---------- 焦点页（竞技头条的二级页） ----------

@Composable
fun FocusDetailScreen(
    tournament: Tournament,
    labels: Map<String, String>,
    enabled: Set<Source>,
    onOpenMatch: (Tournament, MatchResult) -> Unit,
) {
    val teamsById = remember(tournament) { tournament.teams.associateBy { it.id } }
    LazyColumn(modifier = Modifier.padding(horizontal = 24.dp)) {
        // 赛事焦点卡（完整版：标题 + 介绍全文 + 时间地点）
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
        // 焦点比赛（点按进比赛详情）
        val featured = tournament.matches.firstOrNull()
        if (featured != null) {
            item {
                FeaturedMatchCard(
                    tournament = tournament,
                    match = featured,
                    teamsById = teamsById,
                    labels = labels,
                    enabled = enabled,
                    onClick = { onOpenMatch(tournament, featured) },
                )
            }
        }
        // 焦点数据（从竞技页移入）
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
        // 赛程（未开赛场次，如实标注）
        if (tournament.fixtures.isNotEmpty()) {
            item { SectionTitle("赛程") }
            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        tournament.fixtures.forEachIndexed { index, fixture ->
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.width(96.dp)) {
                                    Text(
                                        fixture.stage,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        fixture.date,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(30.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant),
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(fixture.label, style = MaterialTheme.typography.bodyMedium)
                                    if (fixture.note.isNotEmpty()) {
                                        Text(
                                            fixture.note,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                            if (index < tournament.fixtures.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                )
                            }
                        }
                    }
                }
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

/** 焦点比赛卡（Sofascore 比赛页头式）：大号比分居中，两队队徽+队名分列两侧，胜方加粗。 */
@Composable
fun FeaturedMatchCard(
    tournament: Tournament,
    match: MatchResult,
    teamsById: Map<String, TeamRecord>,
    labels: Map<String, String>,
    enabled: Set<Source>,
    onClick: (() -> Unit)? = null,
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
            modifier = Modifier
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 16.dp, vertical = 14.dp),
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
                FeaturedTeamSide(
                    teamId = match.homeTeamId,
                    teamsById = teamsById,
                    labels = labels,
                    emphasized = homeWin,
                    dimmed = awayWin,
                    modifier = Modifier.weight(1f),
                )
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
                FeaturedTeamSide(
                    teamId = match.awayTeamId,
                    teamsById = teamsById,
                    labels = labels,
                    emphasized = awayWin,
                    dimmed = homeWin,
                    modifier = Modifier.weight(1f),
                )
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
private fun FeaturedTeamSide(
    teamId: String,
    teamsById: Map<String, TeamRecord>,
    labels: Map<String, String>,
    emphasized: Boolean,
    dimmed: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val record = teamsById[teamId]
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (record != null) {
            TeamBadge(record, size = 48)
        } else {
            TeamCrestById(teamId, flagFallback = teamFlagOnly(labels, teamId), size = 48)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            record?.nameCn ?: teamNameOnly(labels, teamId),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal,
            color = if (dimmed) scheme.onSurfaceVariant else scheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

// ---------- 球队详情（介绍主导） ----------

/** 某队在某场的赛果（从该队视角）：胜 / 平 / 负。 */
private enum class FormResult(val label: String) { WIN("胜"), DRAW("平"), LOSS("负") }

private fun formResultOf(teamId: String, match: MatchResult): FormResult {
    val gf = if (match.homeTeamId == teamId) match.homeScore else match.awayScore
    val ga = if (match.homeTeamId == teamId) match.awayScore else match.homeScore
    return when {
        gf > ga -> FormResult.WIN
        gf < ga -> FormResult.LOSS
        else -> FormResult.DRAW
    }
}

private fun formColor(result: FormResult): Color = when (result) {
    FormResult.WIN -> Color(0xFF2F9E44)
    FormResult.DRAW -> Color(0xFF868E96)
    FormResult.LOSS -> Color(0xFFE03131)
}

@Composable
fun TeamDetailScreen(
    tournament: Tournament,
    labels: Map<String, String>,
    team: TeamRecord,
    enabled: Set<Source>,
    onOpenTeam: (TeamRecord) -> Unit,
    onOpenMatch: (MatchResult) -> Unit,
) {
    val teamsById = remember(tournament) { tournament.teams.associateBy { it.id } }
    val matches = tournament.matches.filter { it.homeTeamId == team.id || it.awayTeamId == team.id }
    // 快照比赛按时间倒序收录（最近的在前），取前 5 场即近五场。
    val recent = matches.take(5)
    val groupKeys = tournament.teams.map { it.group }.distinct()
    val isGroupStage = groupKeys.all { it.matches(Regex("^[A-Z]组$")) }
    LazyColumn(modifier = Modifier.padding(horizontal = 24.dp)) {
        // 头部：大队徽 + 队名 + 所属赛事/小组/名次/积分摘要
        item {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TeamBadge(team, 72)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(team.nameCn, style = MaterialTheme.typography.headlineSmall,
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
        }
        // 简介（全部由快照字段拼成，不编造）
        item { SectionTitle("简介") }
        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val groupText =
                        if (team.group != "—") "分在${team.group}" else "以淘汰赛阶段成绩收录"
                    Text(
                        "${team.nameCn}（${team.nameEn}）参加${tournament.name}，$groupText，" +
                            "本届成绩为${team.rankLabel}。共出场 ${team.played} 场，" +
                            "${team.won} 胜 ${team.drawn} 平 ${team.lost} 负。",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (team.scopeNote.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            team.scopeNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    SourceTag(team.sources, enabled)
                }
            }
        }
        // 近五场状态（Sofascore 式胜/平/负圆点，点按进对应比赛详情）
        item { SectionTitle("近五场") }
        item {
            if (recent.isEmpty()) {
                Text(
                    "该队比赛记录待同步。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    recent.forEach { match ->
                        val result = formResultOf(team.id, match)
                        val opponentId =
                            if (match.homeTeamId == team.id) match.awayTeamId else match.homeTeamId
                        val opponent = teamsById[opponentId]
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(52.dp)
                                .clickable { onOpenMatch(match) },
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(formColor(result)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    result.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                opponent?.nameCn ?: teamNameOnly(labels, opponentId),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            ScoreText(
                                if (match.homeTeamId == team.id) {
                                    "${match.homeScore}-${match.awayScore}"
                                } else {
                                    "${match.awayScore}-${match.homeScore}"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
        // 所在联赛 · 积分（小组制显示本组表，否则显示全表）
        item { SectionTitle("所在联赛 · 积分") }
        item {
            val contextTeams = if (isGroupStage) {
                tournament.teams.filter { it.group == team.group }
            } else {
                tournament.teams
            }.sortedWith(standingsOrder())
            StandingsTable(
                teams = contextTeams,
                onOpenTeam = onOpenTeam,
                qualifying = { t, position ->
                    (isGroupStage && position <= 2) || rankLabelQualifies(t)
                },
            )
        }
        // 比赛（按阶段分组，点按进比赛详情）
        item { SectionTitle("比赛") }
        if (matches.isEmpty()) {
            item {
                Text(
                    "该队比赛记录待同步。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            val groups = matches.groupBy { it.stage }.entries.toList()
            items(groups) { (stage, groupMatches) ->
                MatchGroupContainer(
                    header = "🏆 ${tournament.shortName} · $stage",
                    matches = groupMatches,
                    teamsById = teamsById,
                    labels = labels,
                    enabled = enabled,
                    onMatchClick = onOpenMatch,
                )
            }
        }
        // 数据（战绩统计行）
        item { SectionTitle("数据") }
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
        // 阵容（按场次阵容优先；无完整位置数据，不按位置编造分组）
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
                            "阵容数据待 Google Sports / 直播吧 / 懂球帝同步（或其来源已在「我的」中关闭）。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                } else {
                    LineupCard(lineup, enabled)
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
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

// ---------- 比赛详情（全局可达） ----------

/**
 * 某队在本场的首发：先查按场次收录的阵容；单阵容只在其 context 明确写了本场对手时才算本场
 * （如世界杯决赛两队、中国队第 3 轮），避免把决赛阵容安到其他场次上。
 */
private fun lineupForMatch(team: TeamRecord, opponent: TeamRecord?): Lineup? {
    if (opponent != null) {
        team.matchLineups[opponent.id]?.let { return it }
    }
    val single = team.lineup ?: return null
    return if (opponent != null && single.context.contains(opponent.nameCn)) single else null
}

@Composable
fun MatchDetailScreen(
    tournament: Tournament,
    labels: Map<String, String>,
    match: MatchResult,
    enabled: Set<Source>,
    onOpenTeam: (TeamRecord) -> Unit,
    onOpenMatch: (MatchResult) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val teamsById = remember(tournament) { tournament.teams.associateBy { it.id } }
    val home = teamsById[match.homeTeamId]
    val away = teamsById[match.awayTeamId]
    val homeWin = match.homeScore > match.awayScore
    val awayWin = match.awayScore > match.homeScore
    val homeLineup = home?.let { lineupForMatch(it, away) }
    val awayLineup = away?.let { lineupForMatch(it, home) }
    // 对抗数据之一：快照内两队的其他交锋
    val otherMeetings = tournament.matches.filter {
        it !== match &&
            ((it.homeTeamId == match.homeTeamId && it.awayTeamId == match.awayTeamId) ||
                (it.homeTeamId == match.awayTeamId && it.awayTeamId == match.homeTeamId))
    }
    LazyColumn(modifier = Modifier.padding(horizontal = 24.dp)) {
        // 比分头：所在联赛 · 阶段 · 时间 + 两队队徽/队名 + 大比分（成绩）
        item {
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
                        "🏆 ${tournament.name} · ${match.stage} · ${match.date}",
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FeaturedTeamSide(
                            teamId = match.homeTeamId,
                            teamsById = teamsById,
                            labels = labels,
                            emphasized = homeWin,
                            dimmed = awayWin,
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (home != null) Modifier.clickable { onOpenTeam(home) }
                                    else Modifier,
                                ),
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ScoreText(
                                "${match.homeScore} - ${match.awayScore}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "完场",
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                        FeaturedTeamSide(
                            teamId = match.awayTeamId,
                            teamsById = teamsById,
                            labels = labels,
                            emphasized = awayWin,
                            dimmed = homeWin,
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (away != null) Modifier.clickable { onOpenTeam(away) }
                                    else Modifier,
                                ),
                        )
                    }
                    // 进球与比赛记录（快照 note 原文，如决赛进球者、赵松源梅开二度等）
                    if (match.note.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
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
        // 首发阵容（有收录才展示名单；没有如实标注暂无数据）
        item { SectionTitle("首发阵容") }
        item {
            Column {
                LineupBlock(
                    teamName = home?.nameCn ?: teamNameOnly(labels, match.homeTeamId),
                    lineup = homeLineup,
                    enabled = enabled,
                )
                Spacer(Modifier.height(8.dp))
                LineupBlock(
                    teamName = away?.nameCn ?: teamNameOnly(labels, match.awayTeamId),
                    lineup = awayLineup,
                    enabled = enabled,
                )
            }
        }
        // 对抗数据：历史交锋 + 两队本届数据对比（只用快照内真实数据）
        item { SectionTitle("对抗数据") }
        item {
            Text(
                "历史交锋（本届快照内）",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            if (otherMeetings.isEmpty()) {
                Text(
                    "快照内暂无两队其他交锋记录。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
            } else {
                MatchGroupContainer(
                    header = "🏆 ${tournament.shortName} · 历史交锋",
                    matches = otherMeetings,
                    teamsById = teamsById,
                    labels = labels,
                    enabled = enabled,
                    onMatchClick = onOpenMatch,
                )
            }
        }
        item {
            Spacer(Modifier.height(12.dp))
            Text(
                "本届数据对比",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            if (home == null || away == null) {
                Text(
                    "其中一队仅有比分收录，暂无战绩数据可对比。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
            } else {
                ComparisonTable(home = home, away = away)
            }
        }
        item {
            Text(
                tournament.dataScope,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp),
            )
        }
    }
}

@Composable
private fun LineupBlock(teamName: String, lineup: Lineup?, enabled: Set<Source>) {
    Column {
        Text(
            teamName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        if (lineup == null) {
            Text(
                "首发阵容暂无数据（快照未收录本场名单）。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LineupCard(lineup, enabled)
        }
    }
}

/** 两队本届战绩对比表：行是 积分/胜/平/负/进球/失球/净胜球，数字走 ScoreText。 */
@Composable
private fun ComparisonTable(home: TeamRecord, away: TeamRecord) {
    val scheme = MaterialTheme.colorScheme
    fun gdText(t: TeamRecord) = t.gd?.let { if (it > 0) "+$it" else it.toString() } ?: "—"
    val rows: List<Triple<String, String, String>> = listOf(
        Triple("积分", home.points.toString(), away.points.toString()),
        Triple("胜", home.won.toString(), away.won.toString()),
        Triple("平", home.drawn.toString(), away.drawn.toString()),
        Triple("负", home.lost.toString(), away.lost.toString()),
        Triple("进球", home.gf?.toString() ?: "—", away.gf?.toString() ?: "—"),
        Triple("失球", home.ga?.toString() ?: "—", away.ga?.toString() ?: "—"),
        Triple("净胜球", gdText(home), gdText(away)),
    )
    Surface(
        shape = MaterialTheme.shapes.large,
        color = scheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(home.nameCn, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(56.dp))
                Text(away.nameCn, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End)
            }
            HorizontalDivider(color = scheme.outlineVariant,
                modifier = Modifier.padding(horizontal = 16.dp))
            rows.forEachIndexed { index, (label, homeValue, awayValue) ->
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ScoreText(homeValue, style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(
                        label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(56.dp),
                    )
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                        ScoreText(awayValue, style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold)
                    }
                }
                if (index < rows.lastIndex) {
                    HorizontalDivider(
                        color = scheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}
