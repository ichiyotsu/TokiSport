package com.tokisport.app.data

import com.tokisport.app.data.Source.DONGQIUDI
import com.tokisport.app.data.Source.GOOGLE_SPORTS
import com.tokisport.app.data.Source.ZHIBO8

/**
 * 0.1.0 本地快照（2026-11-27 核对）。
 *
 * 来源标注原则（如实打标，不夸大）：
 * - 比分/积分：以 Google Sports 结果为准，经 FIFA 赛事报道交叉核对 -> [GOOGLE_SPORTS]。
 * - 决赛与中国队阵容：直播吧首发名单（上级已逐页核实） -> [ZHIBO8]。
 * - 懂球帝只打标在其比赛页实际出现过的记录（U17 中国队三场小组赛） -> [DONGQIUDI]。
 * 未核实到的数据一律不写：进失球缺失用 null（UI 显示「—」），阵容缺失用「待同步」占位。
 */
object SnapshotData {

    private val GS = setOf(GOOGLE_SPORTS)

    // ---------- 2026 FIFA 世界杯 ----------

    private val spainLineup = Lineup(
        formation = "4-2-3-1",
        context = "决赛 vs 阿根廷 · 首发阵容（连续三场不变）",
        players = listOf(
            "23 乌奈·西蒙（门将）", "12 波罗", "22 库巴西", "14 拉波尔特", "24 库库雷利亚",
            "16 罗德里（队长）", "8 法比安·鲁伊斯", "10 奥尔莫",
            "19 亚马尔", "15 巴埃纳", "21 奥亚萨瓦尔",
        ),
        source = "直播吧决赛首发名单",
        sources = setOf(ZHIBO8),
    )

    private val argentinaLineup = Lineup(
        formation = "4-4-2",
        context = "决赛 vs 西班牙 · 首发阵容",
        players = listOf(
            "23 达米安·马丁内斯（门将）", "4 蒙铁尔", "13 罗梅罗", "6 利桑德罗·马丁内斯", "3 塔利亚菲科",
            "7 德保罗", "24 恩佐·费尔南德斯", "20 麦卡利斯特", "15 尼古拉斯·冈萨雷斯",
            "10 梅西（队长）", "9 阿尔瓦雷斯",
        ),
        source = "直播吧决赛首发名单",
        sources = setOf(ZHIBO8),
    )

    private val worldCup = Tournament(
        id = "wc2026",
        name = "2026 FIFA 世界杯",
        shortName = "2026世界杯",
        dates = "2026年6月11日 – 7月19日",
        venue = "加拿大 · 墨西哥 · 美国",
        status = "已结束",
        heroTitle = "🏆 西班牙夺冠",
        heroBody = "决赛加时赛 1-0 击败阿根廷，费兰·托雷斯第 106 分钟打入制胜球。西班牙时隔 16 年再夺世界杯，全程 8 场仅失 1 球。",
        focusStats = listOf(
            "参赛球队" to "48 支",
            "总场次" to "104 场",
            "总进球" to "308 球",
            "金靴" to "姆巴佩 10 球",
            "西班牙失球" to "8 场仅 1 球",
            "乌奈·西蒙" to "7 场零封",
        ),
        teams = listOf(
            TeamRecord("spain", "西班牙", "Spain", "🇪🇸", 0xFFAA151B, "H组第1", "冠军",
                5, 5, 0, 0, 9, 1, "淘汰赛完整路径（小组赛 2胜1平）", spainLineup, GS),
            TeamRecord("argentina", "阿根廷", "Argentina", "🇦🇷", 0xFF75AADB, "J组第1", "亚军",
                5, 4, 0, 1, 11, 7, "淘汰赛完整路径（小组赛 3战全胜）", argentinaLineup, GS),
            TeamRecord("england", "英格兰", "England", "🇬🇧", 0xFF3D5A99, "—", "季军",
                3, 2, 0, 1, 9, 7, "仅收录 1/4 决赛起", null, GS),
            TeamRecord("france", "法国", "France", "🇫🇷", 0xFF0055A4, "—", "第 4 名",
                3, 1, 0, 2, 6, 8, "仅收录 1/4 决赛起", null, GS),
            TeamRecord("morocco", "摩洛哥", "Morocco", "🇲🇦", 0xFFC1272D, "—", "八强",
                1, 0, 0, 1, 0, 2, "仅收录 1/4 决赛起", null, GS),
            TeamRecord("belgium", "比利时", "Belgium", "🇧🇪", 0xFFED2939, "—", "八强",
                1, 0, 0, 1, 1, 2, "仅收录 1/4 决赛起", null, GS),
            TeamRecord("norway", "挪威", "Norway", "🇳🇴", 0xFFBA0C2F, "—", "八强",
                1, 0, 0, 1, 1, 2, "仅收录 1/4 决赛起", null, GS),
            TeamRecord("switzerland", "瑞士", "Switzerland", "🇨🇭", 0xFFDA291C, "—", "八强",
                1, 0, 0, 1, 1, 3, "仅收录 1/4 决赛起", null, GS),
        ),
        matches = listOf(
            MatchResult("决赛", "7月19日", "spain", "argentina", 1, 0,
                "加时赛 · 费兰·托雷斯 106' · 大都会人寿球场 · 80,663 人", GS),
            MatchResult("季军赛", "7月18日", "england", "france", 6, 4, "", GS),
            MatchResult("半决赛", "7月14日", "france", "spain", 0, 2, "", GS),
            MatchResult("半决赛", "7月15日", "england", "argentina", 1, 2, "", GS),
            MatchResult("1/4决赛", "7月9日–11日", "france", "morocco", 2, 0, "", GS),
            MatchResult("1/4决赛", "7月9日–11日", "spain", "belgium", 2, 1, "", GS),
            MatchResult("1/4决赛", "7月9日–11日", "england", "norway", 2, 1, "", GS),
            MatchResult("1/4决赛", "7月9日–11日", "argentina", "switzerland", 3, 1, "", GS),
            MatchResult("1/8决赛", "7月4日–7日", "spain", "portugal", 1, 0, "", GS),
            MatchResult("1/8决赛", "7月4日–7日", "argentina", "egypt-wc", 3, 2, "", GS),
            MatchResult("1/16决赛", "6月28日–7月3日", "spain", "austria", 3, 0, "", GS),
            MatchResult("1/16决赛", "6月28日–7月3日", "argentina", "caboverde", 3, 2, "", GS),
        ),
        fixtures = emptyList(),
        dataScope = "快照范围：西班牙、阿根廷为完整淘汰赛路径；其余球队自 1/4 决赛起收录。小组赛完整积分表待后续同步。决赛两队首发已核实（直播吧）。",
        extraTeamLabels = mapOf(
            "portugal" to "🇵🇹 葡萄牙",
            "egypt-wc" to "🇪🇬 埃及",
            "austria" to "🇦🇹 奥地利",
            "caboverde" to "🇨🇻 佛得角",
        ),
    )

    // ---------- 男足 U17 世界杯（卡塔尔 2026） ----------

    private val chinaLineup = Lineup(
        formation = "4-4-2",
        context = "小组赛第 3 轮 vs 斐济 · 首发阵容",
        players = listOf(
            "21 江宇涵（门将）", "2 张旭尧", "3 金昱成", "4 南子勋", "5 李钧鹏",
            "6 周雨诺（队长）", "7 邝兆镭", "10 万项", "11 何思凡", "13 潘朝伟", "9 赵松源",
        ),
        source = "直播吧 / 中国足球队官方名单",
        sources = setOf(ZHIBO8),
    )

    private fun u17Team(
        id: String, cn: String, en: String, flag: String, color: Long,
        group: String, rank: String, p: Int, w: Int, d: Int, l: Int,
        gf: Int?, ga: Int?, lineup: Lineup? = null, sources: Set<Source> = GS,
    ) = TeamRecord(id, cn, en, flag, color, group, rank, p, w, d, l, gf, ga, "", lineup, sources)

    private val u17 = Tournament(
        id = "u17wc2026",
        name = "U17 世界杯（男足）",
        shortName = "U17世界杯",
        dates = "2026年11月19日 – 12月13日",
        venue = "卡塔尔 · 多哈（阿斯拜尔区）",
        status = "进行中 · 小组赛",
        heroTitle = "小组赛收官在即",
        heroBody = "A–H 组排名已定：西班牙 U17 三战全胜、22 球零失球领跑；中国 U17 以 1 胜 1 平 1 负积 4 分晋级 32 强，为 21 年来中国男足国字号首次在世界大赛小组出线。I–L 组最后一轮 11月27日进行。",
        focusStats = listOf(
            "参赛球队" to "48 支",
            "西班牙 U17" to "22 球 · 0 失球",
            "中国 U17" to "积 4 分晋级",
            "淘汰赛" to "11月30日起",
            "决赛" to "12月13日",
        ),
        teams = listOf(
            u17Team("greece", "希腊", "Greece", "🇬🇷", 0xFF0D5EAF, "A组", "第 1 · 晋级", 3, 3, 0, 0, 8, 3),
            u17Team("egypt", "埃及", "Egypt", "🇪🇬", 0xFFCE1126, "A组", "第 2 · 晋级", 3, 2, 0, 1, 7, 3),
            u17Team("qatar", "卡塔尔", "Qatar", "🇶🇦", 0xFF8A1538, "A组", "第 3", 3, 1, 0, 2, 3, 6),
            u17Team("panama", "巴拿马", "Panama", "🇵🇦", 0xFF005293, "A组", "第 4", 3, 0, 0, 3, 2, 8),
            u17Team("korea", "韩国", "Korea Republic", "🇰🇷", 0xFFCD2E3A, "B组", "第 1 · 晋级", 3, 3, 0, 0, 12, 2),
            u17Team("ecuador", "厄瓜多尔", "Ecuador", "🇪🇨", 0xFFFCD116, "B组", "晋级", 3, 1, 1, 1, null, null),
            u17Team("uganda", "乌干达", "Uganda", "🇺🇬", 0xFFD90000, "B组", "晋级", 3, 1, 1, 1, null, null),
            u17Team("australia", "澳大利亚", "Australia", "🇦🇺", 0xFF00843D, "C组", "第 1 · 晋级", 3, 1, 2, 0, 4, 2),
            u17Team("argentina-u17", "阿根廷", "Argentina", "🇦🇷", 0xFF75AADB, "C组", "第 2 · 晋级", 3, 1, 1, 1, null, null),
            u17Team("france-u17", "法国", "France", "🇫🇷", 0xFF0055A4, "D组", "第 1 · 晋级", 3, 2, 0, 1, 6, 2),
            u17Team("saudi", "沙特阿拉伯", "Saudi Arabia", "🇸🇦", 0xFF006C35, "D组", "第 2 · 晋级", 3, 2, 0, 1, 5, 4),
            u17Team("italy", "意大利", "Italy", "🇮🇹", 0xFF0064AA, "E组", "第 1 · 晋级", 3, 3, 0, 0, 11, 2),
            u17Team("cotedivoire", "科特迪瓦", "Côte d'Ivoire", "🇨🇮", 0xFFF77F00, "E组", "第 2 · 晋级", 3, 2, 0, 1, null, null),
            u17Team("uzbekistan", "乌兹别克斯坦", "Uzbekistan", "🇺🇿", 0xFF0099B5, "E组", "第 3", 3, 1, 0, 2, null, null),
            u17Team("jamaica", "牙买加", "Jamaica", "🇯🇲", 0xFF009B3A, "E组", "第 4", 3, 0, 0, 3, null, null),
            u17Team("croatia", "克罗地亚", "Croatia", "🇭🇷", 0xFF0A3D91, "F组", "第 1 · 晋级", 3, 2, 1, 0, 12, 1),
            u17Team("senegal", "塞内加尔", "Senegal", "🇸🇳", 0xFF00853F, "F组", "第 2 · 晋级", 3, 1, 2, 0, 11, 2),
            u17Team("tajikistan", "塔吉克斯坦", "Tajikistan", "🇹🇯", 0xFFCC0000, "F组", "第 3", 3, 1, 1, 1, null, null),
            u17Team("cuba", "古巴", "Cuba", "🇨🇺", 0xFF002A8F, "F组", "第 4", 3, 0, 0, 3, null, null),
            u17Team("mali", "马里", "Mali", "🇲🇱", 0xFF14A44A, "G组", "第 1 · 晋级", 3, 3, 0, 0, 7, 1),
            u17Team("belgium-u17", "比利时", "Belgium", "🇧🇪", 0xFFED2939, "G组", "第 2 · 晋级", 3, 2, 0, 1, null, null),
            u17Team("vietnam", "越南", "Vietnam", "🇻🇳", 0xFFDA251D, "G组", "第 3", 3, 0, 1, 2, null, null),
            u17Team("newzealand", "新西兰", "New Zealand", "🇳🇿", 0xFF00247D, "G组", "第 4", 3, 0, 1, 2, null, null),
            u17Team("spain-u17", "西班牙", "Spain", "🇪🇸", 0xFFAA151B, "H组", "第 1 · 晋级", 3, 3, 0, 0, 22, 0),
            u17Team("morocco-u17", "摩洛哥", "Morocco", "🇲🇦", 0xFFC1272D, "H组", "第 2 · 晋级", 3, 1, 1, 1, 8, 4),
            u17Team("china", "中国", "China PR", "🇨🇳", 0xFFDE2910, "H组", "第 3 · 晋级", 3, 1, 1, 1, 10, 7,
                lineup = chinaLineup, sources = setOf(GOOGLE_SPORTS, DONGQIUDI)),
            u17Team("fiji", "斐济", "Fiji", "🇫🇯", 0xFF68BFE5, "H组", "第 4", 3, 0, 0, 3, null, null),
        ),
        matches = listOf(
            // 11月26日（第 3 轮，E–H 组）
            MatchResult("小组赛 H组", "11月26日", "china", "fiji", 8, 0,
                "赵松源、邝兆镭、万项、张君豪、梁仕宇进球（含一记乌龙）", setOf(GOOGLE_SPORTS, DONGQIUDI)),
            MatchResult("小组赛 H组", "11月26日", "morocco-u17", "spain-u17", 0, 2, "", GS),
            MatchResult("小组赛 E组", "11月26日", "uzbekistan", "italy", 1, 3, "", GS),
            MatchResult("小组赛 E组", "11月26日", "jamaica", "cotedivoire", 1, 3, "", GS),
            MatchResult("小组赛 F组", "11月26日", "tajikistan", "senegal", 1, 1, "", GS),
            MatchResult("小组赛 F组", "11月26日", "croatia", "cuba", 7, 0, "", GS),
            MatchResult("小组赛 G组", "11月26日", "vietnam", "mali", 0, 2, "", GS),
            MatchResult("小组赛 G组", "11月26日", "newzealand", "belgium-u17", 0, 6, "", GS),
            // 11月25日（第 3 轮，A–D 组）
            MatchResult("小组赛 A组", "11月25日", "greece", "qatar", 4, 1, "", GS),
            MatchResult("小组赛 A组", "11月25日", "panama", "egypt", 1, 4, "", GS),
            MatchResult("小组赛 B组", "11月25日", "ecuador", "korea", 2, 3, "", GS),
            MatchResult("小组赛 B组", "11月25日", "uganda", "newcaledonia", 6, 0, "", GS),
            MatchResult("小组赛 C组", "11月25日", "denmark", "argentina-u17", 3, 3, "", GS),
            MatchResult("小组赛 C组", "11月25日", "australia", "mozambique", 2, 2, "", GS),
            MatchResult("小组赛 D组", "11月25日", "uruguay", "france-u17", 0, 3, "", GS),
            MatchResult("小组赛 D组", "11月25日", "haiti", "saudi", 1, 2, "", GS),
            // 中国队前两轮
            MatchResult("小组赛 H组", "第 2 轮", "china", "morocco-u17", 2, 2,
                "赵松源梅开二度", setOf(GOOGLE_SPORTS, DONGQIUDI)),
            MatchResult("小组赛 H组", "第 1 轮", "spain-u17", "china", 5, 0, "", setOf(GOOGLE_SPORTS, DONGQIUDI)),
        ),
        fixtures = listOf(
            Fixture("小组赛 I–L 组", "11月27日", "I–L 组最后一轮今日进行", "积分榜待更新"),
            Fixture("32 强淘汰赛", "11月30日起", "对阵待小组赛全部结束后确定", ""),
            Fixture("决赛", "12月13日", "卡塔尔 · 多哈", ""),
        ),
        dataScope = "快照范围：A–H 组第 3 轮后积分；I–L 组最后一轮 11月27日进行，待更新。部分球队进失球尚未核实，显示为「—」。中国队首发（vs 斐济）已核实（直播吧）。",
        extraTeamLabels = mapOf(
            "newcaledonia" to "🇳🇨 新喀里多尼亚",
            "denmark" to "🇩🇰 丹麦",
            "mozambique" to "🇲🇿 莫桑比克",
            "uruguay" to "🇺🇾 乌拉圭",
            "haiti" to "🇭🇹 海地",
        ),
    )

    val tournaments: List<Tournament> = listOf(worldCup, u17)
}
