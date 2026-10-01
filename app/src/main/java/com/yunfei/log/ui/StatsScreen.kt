package com.yunfei.log.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunfei.log.data.Store
import com.yunfei.log.logic.Dates
import com.yunfei.log.logic.DayType
import com.yunfei.log.logic.WorkHours
import com.yunfei.log.logic.fmtHours
import com.yunfei.log.logic.monthStats
import java.util.Locale

@Composable
fun StatsScreen(store: Store, tick: Int) {
    val today = Dates.today()
    var year by remember { mutableStateOf(Dates.year(today)) }
    var month by remember { mutableStateOf(Dates.month(today)) }
    val stats = remember(year, month, tick) { monthStats(store, year, month) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 22.dp)
    ) {
        // ---------- 月份选择 ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(40.dp))
                .background(SurfaceMid)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ArrowButton("‹") {
                if (month == 1) {
                    month = 12; year -= 1
                } else month -= 1
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    Dates.monthLabel(year, month),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (year == Dates.year(today) && month == Dates.month(today)) "本月" else "历史月份",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
            ArrowButton("›") {
                if (month == 12) {
                    month = 1; year += 1
                } else month += 1
            }
        }
        Spacer(Modifier.height(12.dp))

        // ---------- 指标 ----------
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(
                Modifier.weight(1f),
                "${fmtHours(stats.total)}h",
                "当月总工时",
                Color(0xFF1D1B20)
            )
            MetricCard(
                Modifier.weight(1f),
                "${fmtHours(stats.ot)}h",
                "其中加班工时",
                OtRed
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(
                Modifier.weight(1f),
                "${stats.attendDays} 天",
                "出勤天数",
                Color(0xFF1D1B20)
            )
            MetricCard(
                Modifier.weight(1f),
                "${fmtHours(stats.avg)}h",
                "出勤日均工时",
                Color(0xFF1D1B20)
            )
        }
        Spacer(Modifier.height(12.dp))

        // ---------- 工时构成 ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceLow)
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("工时构成", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text(
                    "加班占比 " + if (stats.total > 0) "${(stats.ot / stats.total * 100).toInt()}%" else "0%",
                    fontSize = 11.5.sp,
                    color = TextMuted
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
            ) {
                if (stats.total <= 0.0) {
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(SurfaceHigh)
                    )
                } else {
                    if (stats.normal > 0.0) {
                        Box(
                            Modifier
                                .weight(stats.normal.toFloat())
                                .fillMaxHeight()
                                .background(Purple)
                        )
                    }
                    if (stats.ot > 0.0) {
                        Box(
                            Modifier
                                .weight(stats.ot.toFloat())
                                .fillMaxHeight()
                                .background(OtRed)
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "正常工时 ${fmtHours(stats.normal)} h　·　加班工时 ${fmtHours(stats.ot)} h",
                fontSize = 11.5.sp,
                color = TextMuted
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                LegendItem(HolidayOrangeBg, "节假日")
                LegendItem(MakeupBlueBg, "调休上班")
                LegendItem(SurfaceHigh, "周末")
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---------- 日历 ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceLow)
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("当月日历", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text(
                    if (stats.attendDays > 0) "出勤 ${stats.attendDays} 天 · 加班 ${stats.otDays} 天"
                    else "该月暂无出勤记录",
                    fontSize = 11.5.sp,
                    color = TextMuted
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth()) {
                listOf("一", "二", "三", "四", "五", "六", "日").forEach { w ->
                    Text(
                        w,
                        modifier = Modifier.weight(1f),
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(4.dp))

            val offset = Dates.firstDayOffset(year, month)
            val days = Dates.daysInMonth(year, month)
            for (row in 0 until 6) {
                Row(Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val cell = row * 7 + col
                        val dayNum = cell - offset + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(2.dp)
                        ) {
                            if (dayNum in 1..days) {
                                val key = String.format(Locale.US, "%04d-%02d-%02d", year, month, dayNum)
                                DayCell(store, key, tick)
                            } else {
                                Box(Modifier.aspectRatio(1f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---------- 8 周强度 ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceLow)
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("最近 8 周工时强度", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text(heatMeta(store, tick), fontSize = 11.5.sp, color = TextMuted)
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                HeatWeeks(store, tick)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("少", fontSize = 10.5.sp, color = TextMuted)
                Spacer(Modifier.width(6.dp))
                listOf(SurfaceHigh, Color(0xFFD0BCFF), Color(0xFFB69DF8), Color(0xFF8E75D6), Purple).forEach {
                    Box(
                        Modifier
                            .padding(horizontal = 2.dp)
                            .size(11.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(it)
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text("多", fontSize = 10.5.sp, color = TextMuted)
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---------- 规则说明 ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceMid)
                .padding(14.dp)
        ) {
            Text("工时计算规则", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(
                "夏时令（5月1日–9月30日）正常工时 9.5 小时（9:00–18:30）\n" +
                        "冬时令（10月1日–次年4月30日）正常工时 9.0 小时（9:00–18:00）\n" +
                        "周末与法定节假日：正常工时不计，只统计加班时长\n" +
                        "调休上班日按正常工作日计算\n" +
                        "休息日有录入内容时，自动按当季标准工时折算为加班\n" +
                        "没有记录的工作日不计入工时；请假 / 休假 / 调休当日不计正常工时\n" +
                        "加班工时 = 加班框填写时长 + 休息日自动折算时长",
                fontSize = 11.5.sp,
                color = TextMuted,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun ArrowButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 22.sp, color = TextMuted)
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier,
    value: String,
    label: String,
    valueColor: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceLow)
            .padding(13.dp)
    ) {
        Text(value, fontSize = 25.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 11.5.sp, color = TextMuted)
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 10.5.sp, color = TextMuted)
    }
}

@Composable
private fun DayCell(store: Store, key: String, tick: Int) {
    val entry = remember(key, tick) { store.get(key) }
    val calc = WorkHours.calc(entry)
    val type = WorkHours.dayType(key)
    val bg = when (type) {
        DayType.HOLIDAY -> HolidayOrangeBg
        DayType.MAKEUP -> MakeupBlueBg
        DayType.WEEKEND -> SurfaceHigh
        DayType.WORKDAY -> Color(0xFFEDE7F6)
    }
    val isToday = key == Dates.today()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(9.dp))
            .background(bg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "${Dates.day(key)}",
            fontSize = 11.5.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isToday) Purple else MaterialTheme.colorScheme.onSurface
        )
        if (calc.total > 0.0) {
            Text(
                fmtHours(calc.total),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (calc.ot > 0.0) OtRed else Purple
            )
        } else if (type == DayType.HOLIDAY) {
            Text(
                WorkHours.HOLIDAYS[key] ?: "",
                fontSize = 8.sp,
                color = HolidayOrange,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun HeatWeeks(store: Store, tick: Int) {
    val today = Dates.today()
    val mondayIndex = (Dates.weekday(today) + 5) % 7
    val start = Dates.shiftDays(today, -(mondayIndex + 49))
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (w in 0 until 8) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (d in 0 until 7) {
                    val key = Dates.shiftDays(start, w * 7 + d)
                    val entry = remember(key, tick) { store.get(key) }
                    val calc = WorkHours.calc(entry)
                    val future = key > today
                    val color = when {
                        future -> Color.Transparent
                        calc.total >= 12.0 -> Purple
                        calc.total >= 10.0 -> Color(0xFF8E75D6)
                        calc.total >= 9.0 -> Color(0xFFB69DF8)
                        calc.total > 0.0 -> Color(0xFFD0BCFF)
                        else -> SurfaceHigh
                    }
                    Box(
                        Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}

private fun heatMeta(store: Store, tick: Int): String {
    val today = Dates.today()
    var n = 0
    for (i in 0 until 56) {
        val key = Dates.shiftDays(today, -i)
        if (WorkHours.calc(store.get(key)).total > 0.0) n++
    }
    return "近 8 周有工时 $n 天"
}
