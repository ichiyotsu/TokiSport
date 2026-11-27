package com.tokisport.app.data

/**
 * 数据层按 Google Sports 的结构建模（games / standings / teams 三个页签的字段形态），
 * UI 只依赖 [SportsDataSource] 接口，不依赖具体来源。
 *
 * 三个预备数据源（设置页可分别开关，默认全开）：
 * [Source.GOOGLE_SPORTS]、[Source.ZHIBO8]、[Source.DONGQIUDI]。
 * 每条快照记录都带 [sources] 标记，注明它被哪些数据源核实过；
 * 页面展示的是「已启用数据源的综合结果」：按比赛/球队身份去重合并，
 * 一条记录只展示一次并标注来源；只被已关闭数据源核实的记录会从综合结果中消失。
 * 三个来源理论上应当一致；将来实时同步若发现冲突，按多数决（3 中取 2）采信并标记冲突。
 *
 * 注意：Google Sports 没有官方免费 API。0.1.0 使用本地快照实现 [SnapshotSportsDataSource]，
 * 快照来自 Google Sports 结果、直播吧、懂球帝与 FIFA 报道的交叉核对（如实标注每条来源）。
 * 之后接入 SerpApi / SearchApi 等解析 Google Sports 的服务时，只需新增一个
 * 实现同一接口的数据源并提供 API Key，UI 无需改动。
 */

/** 数据源（「我的」页三个开关与之一一对应）。 */
enum class Source(val label: String, val role: String) {
    GOOGLE_SPORTS("Google Sports", "比分 · 积分榜"),
    ZHIBO8("直播吧", "比分 · 阵容"),
    DONGQIUDI("懂球帝", "比分 · 阵容交叉核对"),
}

/** 一场比赛（对应 Google Sports 的 games 页签条目）。 */
data class MatchResult(
    val stage: String,
    val date: String,
    val homeTeamId: String,
    val awayTeamId: String,
    val homeScore: Int,
    val awayScore: Int,
    val note: String = "",
    /** 核实过本条记录的数据源集合。 */
    val sources: Set<Source> = emptySet(),
)

/** 一场未开赛的赛程。 */
data class Fixture(
    val stage: String,
    val date: String,
    val label: String,
    val note: String = "",
)

/** 首发阵容（仅收录已核实来源的场次；其余球队在详情页显示「待同步」）。 */
data class Lineup(
    val formation: String,
    val context: String,
    val players: List<String>,
    val source: String,
    /** 核实过本条阵容的数据源集合。 */
    val sources: Set<Source> = emptySet(),
)

/**
 * 一支球队在某项赛事中的战绩汇总（对应 Google Sports 的 standings 页签行）。
 * gf/ga 为 null 表示该项数据在本次快照范围内尚未核实，UI 显示「—」，绝不编造。
 */
data class TeamRecord(
    val id: String,
    val nameCn: String,
    val nameEn: String,
    val flag: String,
    /** 队徽回退色（0xAARRGGBB）：真实队徽（TheSportsDB，见 ui/Crests.kt）缺失时才用彩色首字圆回退，不伪造队徽。 */
    val color: Long,
    val group: String,
    val rankLabel: String,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val gf: Int?,
    val ga: Int?,
    val scopeNote: String = "",
    val lineup: Lineup? = null,
    /** 核实过本条战绩的数据源集合。 */
    val sources: Set<Source> = emptySet(),
    /**
     * 按场次收录的首发阵容，key = 对手球队 id（同一赛事内一支球队对同一对手只收录一场时唯一）。
     * 非空时详情页按比赛逐场展示阵容，优先于 [lineup]；其他球队保持单阵容/待同步不变。
     */
    val matchLineups: Map<String, Lineup> = emptyMap(),
) {
    val gd: Int? get() = if (gf != null && ga != null) gf - ga else null
    val points: Int get() = won * 3 + drawn
}

/** 一项赛事的完整快照。 */
data class Tournament(
    val id: String,
    val name: String,
    val shortName: String,
    val dates: String,
    val venue: String,
    val status: String,
    val heroTitle: String,
    val heroBody: String,
    val focusStats: List<Pair<String, String>>,
    val teams: List<TeamRecord>,
    val matches: List<MatchResult>,
    val fixtures: List<Fixture>,
    val dataScope: String,
    /** 只出现在比分里、没有独立战绩页的球队：id -> 「国旗 + 中文名」。 */
    val extraTeamLabels: Map<String, String> = emptyMap(),
)

/** 数据源接口：games / standings / teams 均从这里取。 */
interface SportsDataSource {
    fun tournaments(): List<Tournament>
    fun tournament(id: String): Tournament?
    fun matchesOf(tournament: Tournament, teamId: String): List<MatchResult> =
        tournament.matches.filter { it.homeTeamId == teamId || it.awayTeamId == teamId }
}

/** 0.1.0 本地快照实现。 */
class SnapshotSportsDataSource : SportsDataSource {
    override fun tournaments(): List<Tournament> = SnapshotData.tournaments
    override fun tournament(id: String): Tournament? =
        SnapshotData.tournaments.firstOrNull { it.id == id }
}
