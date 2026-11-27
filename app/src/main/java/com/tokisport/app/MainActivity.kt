package com.tokisport.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.tokisport.app.data.MatchResult
import com.tokisport.app.data.Merger
import com.tokisport.app.data.SourceSettings
import com.tokisport.app.data.TeamRecord
import com.tokisport.app.data.Tournament
import com.tokisport.app.ui.ArenaScreen
import com.tokisport.app.ui.AvatarButton
import com.tokisport.app.ui.FloatingNavBar
import com.tokisport.app.ui.FocusDetailScreen
import com.tokisport.app.ui.MatchDetailScreen
import com.tokisport.app.ui.NavEntry
import com.tokisport.app.ui.OverlayTitleHeader
import com.tokisport.app.ui.ProfileScreen
import com.tokisport.app.ui.StandingsScreen
import com.tokisport.app.ui.TeamDetailScreen
import com.tokisport.app.ui.TokiSportTheme
import com.tokisport.app.ui.TournamentChipRow
import com.tokisport.app.ui.tokiPageColor
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settings = SourceSettings(applicationContext)
        setContent {
            TokiSportTheme {
                TokiSportApp(settings)
            }
        }
    }
}

private enum class Tab(val label: String, val icon: ImageVector) {
    ARENA("竞技", Icons.Rounded.SportsSoccer),
    STANDINGS("积分", Icons.Rounded.Leaderboard),
}

/** 二级页导航栈（焦点 / 球队 / 比赛详情 / 我的），后进先出，系统返回键逐层退。 */
private sealed interface Dest {
    data class Focus(val tournamentId: String) : Dest
    data class Team(val tournamentId: String, val team: TeamRecord) : Dest
    data class Match(val tournamentId: String, val match: MatchResult) : Dest
    data object Profile : Dest
}

@Composable
private fun TokiSportApp(settings: SourceSettings) {
    val dataSource = remember { com.tokisport.app.data.SnapshotSportsDataSource() }
    val tournaments = remember { dataSource.tournaments() }
    val enabled by settings.enabled

    var tab by remember { mutableStateOf(Tab.ARENA) }
    var tournamentId by remember { mutableStateOf(tournaments.first().id) }
    var stack by remember { mutableStateOf<List<Dest>>(emptyList()) }
    val current = stack.lastOrNull()

    // 综合结果：全部赛事按已启用数据源合并后的视图（竞技页统一展示、积分页按选中赛事）。
    val mergedAll = remember(tournaments, enabled) {
        tournaments.map { Merger.merged(it, enabled) }
    }
    val labelsByTournament = remember(mergedAll) {
        mergedAll.associate { t ->
            t.id to (t.teams.associate { it.id to "${it.flag} ${it.nameCn}" } + t.extraTeamLabels)
        }
    }
    fun mergedOf(id: String): Tournament = mergedAll.first { it.id == id }
    fun push(dest: Dest) { stack = stack + dest }
    fun pop() { stack = stack.dropLast(1) }

    BackHandler(enabled = stack.isNotEmpty()) { pop() }

    val hazeState = rememberHazeState()
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val contentTop = statusTop + 72.dp
    val contentBottom = if (current == null) 96.dp else 24.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tokiPageColor()),
    ) {
        // 内容根：注册为 Haze 内容源，标题头与底栏的玻璃从这里采样（TokiMusic 同款结构）。
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = contentTop, bottom = contentBottom),
            ) {
                when (val dest = current) {
                    null -> when (tab) {
                        Tab.ARENA -> ArenaScreen(
                            tournaments = mergedAll,
                            labelsByTournament = labelsByTournament,
                            enabled = enabled,
                            onOpenProfile = { push(Dest.Profile) },
                            onOpenFocus = { push(Dest.Focus(it.id)) },
                            onOpenMatch = { t, m -> push(Dest.Match(t.id, m)) },
                        )
                        Tab.STANDINGS -> Column {
                            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                                TournamentChipRow(tournaments, tournamentId) { tournamentId = it }
                            }
                            StandingsScreen(
                                tournament = mergedOf(tournamentId),
                                enabled = enabled,
                                onOpenProfile = { push(Dest.Profile) },
                                onOpenTeam = { push(Dest.Team(tournamentId, it)) },
                            )
                        }
                    }
                    is Dest.Focus -> {
                        val t = mergedOf(dest.tournamentId)
                        FocusDetailScreen(
                            tournament = t,
                            labels = labelsByTournament[t.id] ?: emptyMap(),
                            enabled = enabled,
                            onOpenMatch = { tt, m -> push(Dest.Match(tt.id, m)) },
                        )
                    }
                    is Dest.Team -> {
                        val t = mergedOf(dest.tournamentId)
                        TeamDetailScreen(
                            tournament = t,
                            labels = labelsByTournament[t.id] ?: emptyMap(),
                            team = t.teams.firstOrNull { it.id == dest.team.id } ?: dest.team,
                            enabled = enabled,
                            onOpenTeam = { push(Dest.Team(dest.tournamentId, it)) },
                            onOpenMatch = { m -> push(Dest.Match(dest.tournamentId, m)) },
                        )
                    }
                    is Dest.Match -> {
                        val t = mergedOf(dest.tournamentId)
                        MatchDetailScreen(
                            tournament = t,
                            labels = labelsByTournament[t.id] ?: emptyMap(),
                            match = dest.match,
                            enabled = enabled,
                            onOpenTeam = { push(Dest.Team(dest.tournamentId, it)) },
                            onOpenMatch = { m -> push(Dest.Match(dest.tournamentId, m)) },
                        )
                    }
                    Dest.Profile -> ProfileScreen(
                        enabled = enabled,
                        onToggle = { source, on -> settings.setEnabled(source, on) },
                    )
                }
            }
        }
        // 统一标题头（strong 玻璃）：一级页标题 + 右侧头像进「我的」；二级页带返回 chip。
        OverlayTitleHeader(
            title = when (val dest = current) {
                null -> tab.label
                is Dest.Focus -> mergedOf(dest.tournamentId).name
                is Dest.Team -> dest.team.nameCn
                is Dest.Match -> "比赛详情"
                Dest.Profile -> "我的"
            },
            hazeState = hazeState,
            onBack = if (current != null) ({ pop() }) else null,
            trailing = if (current == null) {
                { AvatarButton(hazeState = hazeState, onClick = { push(Dest.Profile) }) }
            } else {
                null
            },
        )
        // 悬浮底栏（TokiMusic 口径：纯图标、灰紫 pill、加强档玻璃），只在一级页显示。
        if (current == null) {
            FloatingNavBar(
                entries = Tab.entries.map { NavEntry(it.icon, it.label) },
                selectedIndex = Tab.entries.indexOf(tab),
                onSelect = { tab = Tab.entries[it] },
                hazeState = hazeState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 8.dp),
            )
        }
    }
}
