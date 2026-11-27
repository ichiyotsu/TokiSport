package com.tokisport.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import com.tokisport.app.data.Lineup
import com.tokisport.app.data.MatchResult
import com.tokisport.app.data.Source
import com.tokisport.app.data.TeamRecord
import com.tokisport.app.data.Tournament

// 二级页集合：
// - 焦点页：竞技页头条点按进入，承接原主页的焦点数据（用户第 1 条）。
// - 球队页：以介绍为主——简介、近五场状态、所在联赛积分、比赛、数据、阵容（用户第 5 条）。
// - 比赛详情页：全局可达（点任何一场比赛行），展示成绩、所在联赛、首发、进球记录与数据对比。
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
    val homeLineup = home?.let { lineupForMatch(it, away) }
    val awayLineup = away?.let { lineupForMatch(it, home) }
    // 数据对比之一：快照内两队的其他交锋
    val otherMeetings = tournament.matches.filter {
        it !== match &&
            ((it.homeTeamId == match.homeTeamId && it.awayTeamId == match.awayTeamId) ||
                (it.homeTeamId == match.awayTeamId && it.awayTeamId == match.homeTeamId))
    }
    LazyColumn(modifier = Modifier.padding(horizontal = 24.dp)) {
        // 头部 hero：模糊渐变背景（两队代表色）+ 大比分 + 两侧进球信息
        item {
            MatchHero(
                tournament = tournament,
                match = match,
                home = home,
                away = away,
                teamsById = teamsById,
                labels = labels,
                onOpenTeam = onOpenTeam,
            )
        }
        // 首发阵容：有名单的场次画球场图（两队各占半场）；双方都无名单时如实标注暂无数据
        item { SectionTitle("首发阵容") }
        item {
            if (homeLineup == null && awayLineup == null) {
                Column {
                    LineupBlock(
                        teamName = home?.nameCn ?: teamNameOnly(labels, match.homeTeamId),
                        lineup = null,
                        enabled = enabled,
                    )
                    Spacer(Modifier.height(8.dp))
                    LineupBlock(
                        teamName = away?.nameCn ?: teamNameOnly(labels, match.awayTeamId),
                        lineup = null,
                        enabled = enabled,
                    )
                }
            } else {
                Column {
                    LineupPitch(
                        home = home,
                        homeLineup = homeLineup,
                        away = away,
                        awayLineup = awayLineup,
                    )
                    Spacer(Modifier.height(10.dp))
                    PitchCaption(
                        teamName = home?.nameCn ?: teamNameOnly(labels, match.homeTeamId),
                        lineup = homeLineup,
                        enabled = enabled,
                    )
                    Spacer(Modifier.height(6.dp))
                    PitchCaption(
                        teamName = away?.nameCn ?: teamNameOnly(labels, match.awayTeamId),
                        lineup = awayLineup,
                        enabled = enabled,
                    )
                }
            }
        }
        // 数据对比（原「对抗数据」改名）：对比条 + 历史交锋，只用快照内真实数据
        item { SectionTitle("数据对比") }
        item {
            if (home == null || away == null) {
                Text(
                    "其中一队仅有比分收录，暂无战绩数据可对比。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
            } else {
                ComparisonBars(home = home, away = away)
            }
        }
        item {
            Spacer(Modifier.height(12.dp))
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
            Text(
                tournament.dataScope,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp),
            )
        }
    }
}

// ---------- 比赛详情 hero（模糊渐变 + 大比分 + 两侧进球） ----------

/**
 * 进球者 → 球队归属映射：只覆盖快照 note 里真实出现的进球者（决赛费兰·托雷斯属西班牙；
 * 中国队三场进球者属中国队），且均为数据内可核对的事实。note 里解析不出归属的段落
 * （加时赛 / 球场 / 观众等）只作中部背景行展示，绝不硬分到某一队名下。
 */
private val scorerTeamByName: Map<String, String> = mapOf(
    "费兰·托雷斯" to "spain",
    "赵松源" to "china",
    "邝兆镭" to "china",
    "万项" to "china",
    "张君豪" to "china",
    "梁仕宇" to "china",
)

/** note 解析结果：主 / 客队进球条目（名字 + 分钟，无分钟记录则只有名字）、附加说明、背景段落。 */
private class GoalInfo(
    val home: List<String> = emptyList(),
    val away: List<String> = emptyList(),
    val homeExtra: String? = null,
    val awayExtra: String? = null,
    val context: List<String> = emptyList(),
)

private fun parseGoalInfo(match: MatchResult): GoalInfo {
    if (match.note.isEmpty()) return GoalInfo()
    val home = mutableListOf<String>()
    val away = mutableListOf<String>()
    var homeExtra: String? = null
    var awayExtra: String? = null
    val context = mutableListOf<String>()
    fun addFor(teamId: String?, entry: String, segment: String) {
        when (teamId) {
            match.homeTeamId -> home += entry
            match.awayTeamId -> away += entry
            else -> context += segment
        }
    }
    match.note.split("·").map { it.trim() }.filter { it.isNotEmpty() }.forEach { segment ->
        val minuteMatch = Regex("""^(.+?)\s+(\d+)'$""").matchEntire(segment)
        val braceMatch = Regex("""^(.+?)梅开二度$""").matchEntire(segment)
        when {
            minuteMatch != null -> {
                val name = minuteMatch.groupValues[1]
                addFor(scorerTeamByName[name], "$name ${minuteMatch.groupValues[2]}'", segment)
            }
            braceMatch != null -> {
                val name = braceMatch.groupValues[1]
                addFor(scorerTeamByName[name], "$name（梅开二度）", segment)
            }
            segment.contains("进球") -> {
                val names = segment.substringBefore("进球")
                    .split("、").map { it.trim() }.filter { it.isNotEmpty() }
                val extra = segment.substringAfter("进球").trim()
                    .removePrefix("（").removeSuffix("）").takeIf { it.isNotEmpty() }
                val teams = names.map { scorerTeamByName[it] }.distinct()
                if (names.isNotEmpty() && teams.size == 1 && teams[0] != null) {
                    names.forEach { addFor(teams[0], it, segment) }
                    if (extra != null) {
                        if (teams[0] == match.homeTeamId) homeExtra = extra else awayExtra = extra
                    }
                } else {
                    context += segment
                }
            }
            else -> context += segment
        }
    }
    return GoalInfo(home, away, homeExtra, awayExtra, context)
}

/**
 * 比赛详情头部（Sofascore 比赛页式）：整块区域铺两队代表色（TeamRecord.color，取自队徽主色调）
 * 的横向渐变，放大后高斯模糊成柔和底；其上压一层主题 veil 保证深浅色下文字对比。
 * 区域内：赛事 · 阶段 · 日期行 → 两队队徽/队名夹大号比分（ScoreText display 档）→
 * 比分下方两侧分列进球信息（主队靠右贴中、客队靠左贴中），无记录的一侧留空。
 */
@Composable
private fun MatchHero(
    tournament: Tournament,
    match: MatchResult,
    home: TeamRecord?,
    away: TeamRecord?,
    teamsById: Map<String, TeamRecord>,
    labels: Map<String, String>,
    onOpenTeam: (TeamRecord) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val homeWin = match.homeScore > match.awayScore
    val awayWin = match.awayScore > match.homeScore
    val homeColor = home?.let { Color(it.color) } ?: scheme.primary
    val awayColor = away?.let { Color(it.color) } ?: scheme.primary
    val goals = remember(match) { parseGoalInfo(match) }
    val heroShape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clip(heroShape),
    ) {
        // 模糊渐变层（matchParentSize 兄弟层，不参与尺寸测量）：渐变放大 1.25 倍再模糊，避免边缘透底。
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer { scaleX = 1.25f; scaleY = 1.25f }
                .blur(28.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            homeColor.copy(alpha = 0.80f),
                            scheme.primary.copy(alpha = 0.45f),
                            awayColor.copy(alpha = 0.80f),
                        ),
                    ),
                ),
        )
        // 对比 veil：深色压黑、浅色压白，保证两套主题下文字可读。
        Box(
            Modifier
                .matchParentSize()
                .background(
                    if (dark) Color.Black.copy(alpha = 0.30f)
                    else Color.White.copy(alpha = 0.38f),
                ),
        )
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "🏆 ${tournament.name} · ${match.stage} · ${match.date}",
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurface.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(14.dp))
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
                        style = MaterialTheme.typography.displayMedium,
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
            // 进球信息：比分下方两侧分列（主队进球靠右、客队进球靠左，贴向中线），无记录一侧留空。
            if (goals.home.isNotEmpty() || goals.away.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End,
                    ) {
                        goals.home.forEach { entry ->
                            Text(
                                entry,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.End,
                            )
                        }
                        goals.homeExtra?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant,
                                textAlign = TextAlign.End,
                            )
                        }
                    }
                    Spacer(Modifier.width(56.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        goals.away.forEach { entry ->
                            Text(
                                entry,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Start,
                            )
                        }
                        goals.awayExtra?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant,
                                textAlign = TextAlign.Start,
                            )
                        }
                    }
                }
            }
            // note 的非进球段落（加时赛 / 球场 / 观众等）作中部背景行原样展示。
            if (goals.context.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    goals.context.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
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

/** 球场图下方的一队说明：阵型 + 场次语境 + 来源；无名单时如实标注暂无数据。 */
@Composable
private fun PitchCaption(teamName: String, lineup: Lineup?, enabled: Set<Source>) {
    val scheme = MaterialTheme.colorScheme
    Column {
        Text(
            teamName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        if (lineup == null) {
            Text(
                "首发阵容暂无数据（快照未收录本场名单）。",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        } else {
            Text(
                "阵型 ${lineup.formation} · ${lineup.context}",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
            Text(
                "来源：${lineup.source}",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
            SourceTag(lineup.sources, enabled)
        }
    }
}

// ---------- 首发球场图 ----------

/**
 * 阵型字符串 → 各线人数（如 "4-2-3-1" → [4,2,3,1]）。
 * 阵型缺失（"—"）或与名单人数对不齐时，回退 4-4-2 展示网格（纯排版布局，不改变名单本身）。
 */
private fun formationLines(formation: String, outfield: Int): List<Int> {
    val parsed = formation.split("-").map { it.trim().toIntOrNull() }
    if (parsed.isNotEmpty() && parsed.all { it != null } && parsed.sumOf { it ?: 0 } == outfield) {
        return parsed.filterNotNull()
    }
    return if (outfield == 10) listOf(4, 4, 2) else listOf(outfield)
}

private class PitchPlayer(val number: String?, val name: String, val x: Float, val y: Float)

/**
 * 按名单顺序（门将第一、其后由守至攻）把 11 人摆进半场：
 * [topHalf] 为真时门将在顶部、各线向中线展开；为假时整体镜像到下半场。
 * 位置只是按阵型线数做的展示排版，不声称是球员的真实站位数据。
 */
private fun pitchPositions(lineup: Lineup, topHalf: Boolean): List<PitchPlayer> {
    val players = lineup.players
    if (players.isEmpty()) return emptyList()
    val lines = formationLines(lineup.formation, players.size - 1)
    val result = mutableListOf<PitchPlayer>()
    fun add(raw: String, x: Float, y: Float) {
        val numMatch = Regex("""^(\d+)\s+(.*)$""").matchEntire(raw.trim())
        val number = numMatch?.groupValues?.get(1)
        val name = (numMatch?.groupValues?.get(2) ?: raw.trim())
            .replace("（门将）", "")
            .replace("（队长）", " ©")
            .trim()
        result += PitchPlayer(number, name, x, if (topHalf) y else 1f - y)
    }
    add(players[0], 0.5f, 0.055f)
    var idx = 1
    lines.forEachIndexed { li, count ->
        val y = if (lines.size == 1) 0.30f
        else 0.175f + li * (0.265f / (lines.size - 1))
        for (j in 0 until count) {
            if (idx >= players.size) break
            add(players[idx], (j + 1f) / (count + 1f), y)
            idx++
        }
    }
    return result
}

/**
 * 足球场图（Sofascore 阵容页式，竖向）：主队占上半场、客队占下半场，
 * 每名球员是队色圆点（内写球衣号）+ 下方姓名。球场线条用 Canvas 画
 * （边线、中线、中圈、禁区、小禁区、点球点），两队颜色取各自 TeamRecord.color。
 */
@Composable
private fun LineupPitch(
    home: TeamRecord?,
    homeLineup: Lineup?,
    away: TeamRecord?,
    awayLineup: Lineup?,
) {
    val lineColor = Color.White.copy(alpha = 0.55f)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.74f)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF3D9950), Color(0xFF2E7D3E)),
                ),
            ),
    ) {
        val w = maxWidth
        val h = maxHeight
        Canvas(Modifier.fillMaxSize()) {
            val strokeWidth = 1.5.dp.toPx()
            val stroke = Stroke(width = strokeWidth)
            val inset = 12.dp.toPx()
            drawRect(
                lineColor,
                topLeft = Offset(inset, inset),
                size = Size(size.width - 2 * inset, size.height - 2 * inset),
                style = stroke,
            )
            drawLine(
                lineColor,
                Offset(inset, size.height / 2),
                Offset(size.width - inset, size.height / 2),
                strokeWidth = strokeWidth,
            )
            drawCircle(
                lineColor,
                radius = size.width * 0.13f,
                center = center,
                style = stroke,
            )
            drawCircle(lineColor, radius = 3.dp.toPx(), center = center)
            val boxW = size.width * 0.56f
            val boxH = size.height * 0.115f
            val smallW = size.width * 0.30f
            val smallH = size.height * 0.05f
            drawRect(
                lineColor,
                topLeft = Offset((size.width - boxW) / 2, inset),
                size = Size(boxW, boxH),
                style = stroke,
            )
            drawRect(
                lineColor,
                topLeft = Offset((size.width - boxW) / 2, size.height - inset - boxH),
                size = Size(boxW, boxH),
                style = stroke,
            )
            drawRect(
                lineColor,
                topLeft = Offset((size.width - smallW) / 2, inset),
                size = Size(smallW, smallH),
                style = stroke,
            )
            drawRect(
                lineColor,
                topLeft = Offset((size.width - smallW) / 2, size.height - inset - smallH),
                size = Size(smallW, smallH),
                style = stroke,
            )
            drawCircle(
                lineColor,
                radius = 3.dp.toPx(),
                center = Offset(size.width / 2, inset + size.height * 0.082f),
            )
            drawCircle(
                lineColor,
                radius = 3.dp.toPx(),
                center = Offset(size.width / 2, size.height - inset - size.height * 0.082f),
            )
        }
        if (homeLineup != null) {
            pitchPositions(homeLineup, topHalf = true).forEach { p ->
                PitchPlayerMarker(
                    player = p,
                    color = home?.let { Color(it.color) } ?: Color(0xFF6750A4),
                    topHalf = true,
                    modifier = Modifier.offset(x = w * p.x - 30.dp, y = h * p.y - 13.dp),
                )
            }
        }
        if (awayLineup != null) {
            pitchPositions(awayLineup, topHalf = false).forEach { p ->
                PitchPlayerMarker(
                    player = p,
                    color = away?.let { Color(it.color) } ?: Color(0xFF6750A4),
                    topHalf = false,
                    modifier = Modifier.offset(x = w * p.x - 30.dp, y = h * p.y - 31.dp),
                )
            }
        }
    }
}

/** 球场上的一名球员：队色圆点（球衣号）+ 姓名小签；下半场球员姓名在圆点上方（向场内生长，避免出界）。 */
@Composable
private fun PitchPlayerMarker(
    player: PitchPlayer,
    color: Color,
    topHalf: Boolean,
    modifier: Modifier = Modifier,
) {
    @Composable
    fun dot() {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.5.dp, Color.White.copy(alpha = 0.85f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                player.number ?: "",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }

    @Composable
    fun nameTag() {
        Text(
            player.name,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.30f), RoundedCornerShape(4.dp))
                .padding(horizontal = 3.dp, vertical = 1.dp),
        )
    }
    Column(
        modifier = modifier.width(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (topHalf) {
            dot()
            Spacer(Modifier.height(2.dp))
            nameTag()
        } else {
            nameTag()
            Spacer(Modifier.height(2.dp))
            dot()
        }
    }
}

// ---------- 数据对比条 ----------

private class CompareRow(
    val label: String,
    val home: Int?,
    val away: Int?,
    val higherBetter: Boolean = true,
)

/**
 * 两队本届数据对比（Sofascore 式对比条）：每行 = 主队数值 | 主队条（向左、从中线向外长）|
 * 中部标签 | 客队条（向右长）| 客队数值；条长按该行较大值归一，占优一侧用主色、
 * 另一侧弱化（负 / 失球按「少者占优」判定）。数字走 ScoreText，缺失值显示「—」且条长为 0。
 */
@Composable
private fun ComparisonBars(home: TeamRecord, away: TeamRecord) {
    val scheme = MaterialTheme.colorScheme
    val rows = listOf(
        CompareRow("积分", home.points, away.points),
        CompareRow("场次", home.played, away.played),
        CompareRow("胜", home.won, away.won),
        CompareRow("平", home.drawn, away.drawn),
        CompareRow("负", home.lost, away.lost, higherBetter = false),
        CompareRow("进球", home.gf, away.gf),
        CompareRow("失球", home.ga, away.ga, higherBetter = false),
        CompareRow("净胜球", home.gd, away.gd),
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
                TeamBadge(home, size = 24)
                Spacer(Modifier.width(6.dp))
                Text(
                    home.nameCn,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    away.nameCn,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(6.dp))
                TeamBadge(away, size = 24)
            }
            HorizontalDivider(
                color = scheme.outlineVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            rows.forEachIndexed { index, row ->
                CompareBarRow(row)
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

@Composable
private fun CompareBarRow(row: CompareRow) {
    val scheme = MaterialTheme.colorScheme
    val h = row.home
    val a = row.away
    val maxAbs = maxOf(abs(h ?: 0), abs(a ?: 0))
    fun frac(v: Int?): Float = if (v == null || maxAbs == 0) 0f else abs(v).toFloat() / maxAbs
    val homeBetter = h != null && a != null && (if (row.higherBetter) h > a else h < a)
    val awayBetter = h != null && a != null && (if (row.higherBetter) a > h else a < h)
    fun barColor(better: Boolean): Color =
        if (better) scheme.primary else scheme.onSurfaceVariant.copy(alpha = 0.40f)
    fun valueText(v: Int?): String = when {
        v == null -> "—"
        row.label == "净胜球" && v > 0 -> "+$v"
        else -> v.toString()
    }
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.CenterEnd) {
            ScoreText(
                valueText(h),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (homeBetter) FontWeight.Bold else FontWeight.Normal,
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(scheme.surfaceContainerHigh),
            )
            Box(
                Modifier
                    .fillMaxWidth(frac(h))
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(barColor(homeBetter)),
            )
        }
        Text(
            row.label,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(60.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(scheme.surfaceContainerHigh),
            )
            Box(
                Modifier
                    .fillMaxWidth(frac(a))
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(barColor(awayBetter)),
            )
        }
        Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.CenterStart) {
            ScoreText(
                valueText(a),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (awayBetter) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}
