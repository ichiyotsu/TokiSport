package com.tokisport.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tokisport.app.data.Merger
import com.tokisport.app.data.SnapshotSportsDataSource
import com.tokisport.app.data.SourceSettings
import com.tokisport.app.data.TeamRecord
import com.tokisport.app.ui.HomeScreen
import com.tokisport.app.ui.RecordsScreen
import com.tokisport.app.ui.SettingsScreen
import com.tokisport.app.ui.TeamDetailScreen
import com.tokisport.app.ui.TokiSportTheme
import com.tokisport.app.ui.TournamentSwitcher

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

private enum class Tab(val label: String, val glyph: String) {
    HOME("主页", "🏠"),
    RECORDS("战绩", "🏆"),
    SETTINGS("设置", "⚙️"),
}

@Composable
private fun TokiSportApp(settings: SourceSettings) {
    val dataSource = remember { SnapshotSportsDataSource() }
    val tournaments = remember { dataSource.tournaments() }
    val enabled by settings.enabled

    var tab by remember { mutableStateOf(Tab.HOME) }
    var tournamentId by remember { mutableStateOf(tournaments.first().id) }
    var selectedTeam by remember { mutableStateOf<TeamRecord?>(null) }

    val full = tournaments.first { it.id == tournamentId }
    // 综合结果：按已启用数据源合并/过滤后的赛事视图（标签查全量表，保证比分行队名完整）。
    val merged = remember(full, enabled) { Merger.merged(full, enabled) }
    val labels = remember(full) {
        full.teams.associate { it.id to "${it.flag} ${it.nameCn}" } + full.extraTeamLabels
    }

    Scaffold(
        bottomBar = {
            // TokiUI 悬浮胶囊底栏：主页 / 战绩 / 设置
            Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(modifier = Modifier.padding(6.dp)) {
                        Tab.entries.forEach { t ->
                            val selected = tab == t && selectedTeam == null || tab == t && t == Tab.RECORDS && selectedTeam != null
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (selected) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.weight(1f),
                            ) {
                                TextButton(onClick = {
                                    tab = t
                                    if (t != Tab.RECORDS) selectedTeam = null
                                }) {
                                    Text(
                                        "${t.glyph} ${t.label}",
                                        color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val team = selectedTeam
            if (team != null && tab == Tab.RECORDS) {
                TeamDetailScreen(
                    tournament = merged,
                    labels = labels,
                    team = merged.teams.firstOrNull { it.id == team.id } ?: team,
                    enabled = enabled,
                    onBack = { selectedTeam = null },
                )
            } else {
                Text(
                    text = when (tab) {
                        Tab.HOME -> "TokiSport"
                        Tab.RECORDS -> "战绩"
                        Tab.SETTINGS -> "设置"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
                when (tab) {
                    Tab.HOME -> {
                        Column {
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                TournamentSwitcher(tournaments, tournamentId) { tournamentId = it }
                            }
                            HomeScreen(
                                tournament = merged,
                                labels = labels,
                                enabled = enabled,
                                onGoSettings = { tab = Tab.SETTINGS },
                                onOpenRecords = { tab = Tab.RECORDS },
                            )
                        }
                    }
                    Tab.RECORDS -> {
                        Column {
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                TournamentSwitcher(tournaments, tournamentId) { tournamentId = it }
                            }
                            RecordsScreen(
                                tournament = merged,
                                enabled = enabled,
                                onGoSettings = { tab = Tab.SETTINGS },
                                onOpenTeam = { selectedTeam = it },
                            )
                        }
                    }
                    Tab.SETTINGS -> SettingsScreen(
                        enabled = enabled,
                        onToggle = { source, on -> settings.setEnabled(source, on) },
                    )
                }
            }
        }
    }
}
