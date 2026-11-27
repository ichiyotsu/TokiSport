package com.tokisport.app.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tokisport.app.R
import com.tokisport.app.data.TeamRecord

/**
 * 真实球队队徽（本轮新增）：来源 TheSportsDB 国家队徽标（strBadge），
 * 已下载内嵌在 res/drawable-nodpi（离线可用），U17 球队用其足协成年国家队队徽（同一徽标）。
 * 覆盖数据内全部 52 支有战绩记录的球队，以及只出现在比分里的 9 支球队。
 * 查不到真实队徽时回退原来的彩色首字圆——不绘制、不伪造队徽。
 */
private val crestByTeamId: Map<String, Int> = mapOf(
    // 2026 世界杯
    "spain" to R.drawable.crest_spain,
    "argentina" to R.drawable.crest_argentina,
    "england" to R.drawable.crest_england,
    "france" to R.drawable.crest_france,
    "morocco" to R.drawable.crest_morocco,
    "belgium" to R.drawable.crest_belgium,
    "norway" to R.drawable.crest_norway,
    "switzerland" to R.drawable.crest_switzerland,
    "portugal" to R.drawable.crest_portugal,
    "egypt-wc" to R.drawable.crest_egypt,
    "austria" to R.drawable.crest_austria,
    "caboverde" to R.drawable.crest_caboverde,
    // U17 世界杯
    "greece" to R.drawable.crest_greece,
    "egypt" to R.drawable.crest_egypt,
    "qatar" to R.drawable.crest_qatar,
    "panama" to R.drawable.crest_panama,
    "korea" to R.drawable.crest_korea,
    "ecuador" to R.drawable.crest_ecuador,
    "uganda" to R.drawable.crest_uganda,
    "australia" to R.drawable.crest_australia,
    "argentina-u17" to R.drawable.crest_argentina,
    "france-u17" to R.drawable.crest_france,
    "saudi" to R.drawable.crest_saudi,
    "italy" to R.drawable.crest_italy,
    "cotedivoire" to R.drawable.crest_cotedivoire,
    "uzbekistan" to R.drawable.crest_uzbekistan,
    "jamaica" to R.drawable.crest_jamaica,
    "croatia" to R.drawable.crest_croatia,
    "senegal" to R.drawable.crest_senegal,
    "tajikistan" to R.drawable.crest_tajikistan,
    "cuba" to R.drawable.crest_cuba,
    "mali" to R.drawable.crest_mali,
    "belgium-u17" to R.drawable.crest_belgium,
    "vietnam" to R.drawable.crest_vietnam,
    "newzealand" to R.drawable.crest_newzealand,
    "spain-u17" to R.drawable.crest_spain,
    "morocco-u17" to R.drawable.crest_morocco,
    "china" to R.drawable.crest_china,
    "fiji" to R.drawable.crest_fiji,
    "brazil" to R.drawable.crest_brazil,
    "ireland" to R.drawable.crest_ireland,
    "tanzania" to R.drawable.crest_tanzania,
    "costarica" to R.drawable.crest_costarica,
    "dominican" to R.drawable.crest_dominican,
    "chile" to R.drawable.crest_chile,
    "algeria" to R.drawable.crest_algeria,
    "montenegro" to R.drawable.crest_montenegro,
    "mexico" to R.drawable.crest_mexico,
    "venezuela" to R.drawable.crest_venezuela,
    "romania" to R.drawable.crest_romania,
    "cameroon" to R.drawable.crest_cameroon,
    "colombia" to R.drawable.crest_colombia,
    "serbia" to R.drawable.crest_serbia,
    "japan" to R.drawable.crest_japan,
    "honduras" to R.drawable.crest_honduras,
    "newcaledonia" to R.drawable.crest_newcaledonia,
    "denmark" to R.drawable.crest_denmark,
    "mozambique" to R.drawable.crest_mozambique,
    "uruguay" to R.drawable.crest_uruguay,
    "haiti" to R.drawable.crest_haiti,
)

/** 球队 id 对应的真实队徽资源（无则 null，调用方走回退）。 */
@DrawableRes
fun crestRes(teamId: String): Int? = crestByTeamId[teamId]

/** 按球队 id 渲染队徽：有真实队徽用图片，没有则用国旗 emoji 占位（仅比分标签球队的兜底）。 */
@Composable
fun TeamCrestById(teamId: String, flagFallback: String, size: Int) {
    val res = crestRes(teamId)
    if (res != null) {
        Image(
            painter = painterResource(res),
            contentDescription = null,
            modifier = Modifier.size(size.dp),
            contentScale = ContentScale.Fit,
        )
    } else {
        Text(
            text = flagFallback,
            style = if (size >= 40) MaterialTheme.typography.headlineSmall
            else MaterialTheme.typography.bodyMedium,
        )
    }
}

/**
 * 球队徽标：优先真实队徽图片；无队徽时回退彩色首字圆（占位方案，不伪造队徽）。
 */
@Composable
fun TeamBadge(team: TeamRecord, size: Int = 44) {
    val res = crestRes(team.id)
    if (res != null) {
        Image(
            painter = painterResource(res),
            contentDescription = team.nameCn,
            modifier = Modifier.size(size.dp),
            contentScale = ContentScale.Fit,
        )
        return
    }
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
