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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tokisport.app.data.MatchResult
import com.tokisport.app.data.Merger
import com.tokisport.app.data.Source
import com.tokisport.app.data.TeamRecord
import com.tokisport.app.data.Tournament

// ---------- 通用小组件 ----------

/** 球队 logo 处理：国旗 emoji + 彩色首字圆（0.1.0 占位方案，不使用伪造队徽）。 */
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
            style = MaterialTheme.typography.titleMedium,
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

@Composable
fun MatchRow(labels: Map<String, String>, match: MatchResult, enabled: Set<Source>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "${match.stage} · ${match.date}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SourceTag(match.sources, enabled)
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${teamLabel(labels, match.homeTeamId)}  ${match.homeScore} - ${match.awayScore}  ${teamLabel(labels, match.awayTeamId)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (match.note.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(match.note, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
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

// ---------- 主页概览 ----------

@Composable
fun HomeScreen(
    tournament: Tournament,
    labels: Map<String, String>,
    enabled: Set<Source>,
    onGoSettings: () -> Unit,
    onOpenRecords: () -> Unit,
) {
    if (enabled.isEmpty()) {
        NoSourceState(onGoSettings)
        return
    }
    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
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
                                    Text(value, style = MaterialTheme.typography.titleMedium,
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
        item { SectionTitle("最新结果") }
        items(tournament.matches.take(6)) { match ->
            MatchRow(labels, match, enabled)
        }
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
            items(tournament.fixtures) { fixture ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text("${fixture.stage} · ${fixture.date}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text(fixture.label, style = MaterialTheme.typography.bodyLarge)
                        if (fixture.note.isNotEmpty()) {
                            Text(fixture.note, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        item { SectionTitle("战绩预览") }
        items(tournament.teams.take(4)) { team ->
            TeamRow(team, enabled) { onOpenRecords() }
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

// ---------- 战绩页 ----------

@Composable
fun TeamRow(team: TeamRecord, enabled: Set<Source>, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TeamBadge(team)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${team.flag} ${team.nameCn} · ${team.nameEn}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "${team.group} · ${team.rankLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SourceTag(team.sources, enabled)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${team.points} 分",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "${team.won}胜 ${team.drawn}平 ${team.lost}负 · 进${team.gf?.toString() ?: "—"} 失${team.ga?.toString() ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

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
    val sorted = tournament.teams.sortedWith(
        compareByDescending<TeamRecord> { it.points }
            .thenByDescending { it.gd ?: Int.MIN_VALUE },
    )
    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        item {
            Text(
                "按综合积分排序 · 点球队查看比分与阵容",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
            )
        }
        items(sorted) { team -> TeamRow(team, enabled) { onOpenTeam(team) } }
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
    val matches = tournament.matches.filter { it.homeTeamId == team.id || it.awayTeamId == team.id }
    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        item {
            TextButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) { Text("← 返回") }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TeamBadge(team, 56)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("${team.flag} ${team.nameCn}", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Text(team.nameEn, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "${team.group} · ${team.rankLabel}",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "赛 ${team.played} · ${team.won}胜 ${team.drawn}平 ${team.lost}负 · " +
                            "进 ${team.gf?.toString() ?: "—"} · 失 ${team.ga?.toString() ?: "—"} · " +
                            "净 ${team.gd?.toString() ?: "—"} · 积分 ${team.points}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (team.scopeNote.isNotEmpty()) {
                        Text(team.scopeNote, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    SourceTag(team.sources, enabled)
                }
            }
        }
        item { SectionTitle("比赛记录") }
        if (matches.isEmpty()) {
            item {
                Text("该队比赛记录待同步。", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(matches) { match -> MatchRow(labels, match, enabled) }
        }
        item { SectionTitle("阵容") }
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ---------- 设置页 ----------

@Composable
fun SettingsScreen(enabled: Set<Source>, onToggle: (Source, Boolean) -> Unit) {
    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        item { SectionTitle("数据源") }
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
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
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("TokiSport 0.1.0", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "设计：MD3E + TokiUI 0.2.0-b1。数据为本地快照（2026-11-27 核对），" +
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
