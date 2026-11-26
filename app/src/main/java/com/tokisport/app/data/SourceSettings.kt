package com.tokisport.app.data

import android.content.Context
import androidx.compose.runtime.mutableStateOf

/**
 * 数据源开关（设置页）：Google Sports / 直播吧 / 懂球帝，默认全开，
 * 用 SharedPreferences 持久化，开关状态以 Compose State 暴露给 UI。
 */
class SourceSettings(context: Context) {
    private val prefs = context.getSharedPreferences("tokisport_prefs", Context.MODE_PRIVATE)

    val enabled = mutableStateOf(load())

    private fun load(): Set<Source> =
        Source.entries.filter { prefs.getBoolean(key(it), true) }.toSet()

    private fun key(source: Source) = "source_enabled_${source.name}"

    fun setEnabled(source: Source, on: Boolean) {
        prefs.edit().putBoolean(key(source), on).apply()
        enabled.value = if (on) enabled.value + source else enabled.value - source
    }
}

/**
 * 综合结果合并层：页面永远展示「已启用数据源的综合结果」。
 * - 按记录身份去重（快照本身已去重，一条记录只保留一份并带多来源标记）；
 * - 只被已关闭数据源核实的记录（sources 与已启用集合无交集）从结果中剔除；
 * - 仍保留的记录，其展示来源 = 自身 sources ∩ 已启用集合（在 UI 层计算标注）。
 * 三个来源理论上应当一致；将来实时同步若发现同一记录数值冲突，
 * 按多数决（3 中取 2）采信并标记冲突——0.1.0 快照无冲突记录。
 */
object Merger {
    fun merged(tournament: Tournament, enabled: Set<Source>): Tournament {
        if (enabled.isEmpty()) {
            return tournament.copy(teams = emptyList(), matches = emptyList())
        }
        val teams = tournament.teams
            .filter { it.sources.any { s -> s in enabled } }
            .map { team ->
                val lineup = team.lineup?.takeIf { l -> l.sources.any { s -> s in enabled } }
                if (lineup == team.lineup) team else team.copy(lineup = lineup)
            }
        val matches = tournament.matches.filter { it.sources.any { s -> s in enabled } }
        return tournament.copy(teams = teams, matches = matches)
    }

    /** 记录在当前已启用集合下应展示的来源标注，如「Google Sports · 直播吧」。 */
    fun attribution(sources: Set<Source>, enabled: Set<Source>): String =
        Source.entries.filter { it in sources && it in enabled }.joinToString(" · ") { it.label }
}
