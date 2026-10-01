package com.yunfei.log.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunfei.log.data.Attendance
import com.yunfei.log.data.DayEntry
import com.yunfei.log.data.Store
import com.yunfei.log.logic.Dates
import com.yunfei.log.logic.DayType
import com.yunfei.log.logic.WorkHours
import com.yunfei.log.logic.fmtHours

@Composable
fun TodayScreen(store: Store, date: String, tick: Int) {
    var entry by remember(date, tick) { mutableStateOf(store.get(date)) }
    var menuOpen by remember { mutableStateOf(false) }
    var otText by remember(date, tick) {
        mutableStateOf(if (entry.otHours > 0.0) trimNum(entry.otHours) else "")
    }

    val calc = WorkHours.calc(entry)
    val type = WorkHours.dayType(date)
    val workday = WorkHours.isWorkday(date)
    val locked = entry.attendance != Attendance.YES

    fun commit(next: DayEntry) {
        val e = next.copy(updated = System.currentTimeMillis())
        entry = e
        store.put(e)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 22.dp)
    ) {
        // ---------- 日期行 ----------
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${Dates.month(date)}月${Dates.day(date)}日",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(8.dp))
            Text(Dates.weekdayLabel(date), fontSize = 13.sp, color = TextMuted)
            Spacer(Modifier.weight(1f))
            Pill(
                text = when (type) {
                    DayType.HOLIDAY -> "节假日·" + (WorkHours.HOLIDAYS[date] ?: "")
                    DayType.MAKEUP -> "调休上班"
                    DayType.WEEKEND -> "周末"
                    DayType.WORKDAY -> "工作日"
                },
                bg = when (type) {
                    DayType.HOLIDAY -> HolidayOrangeBg
                    DayType.MAKEUP, DayType.WORKDAY -> PurpleContainer
                    DayType.WEEKEND -> SurfaceHigh
                },
                fg = when (type) {
                    DayType.HOLIDAY -> HolidayOrange
                    DayType.MAKEUP, DayType.WORKDAY -> Color(0xFF21005D)
                    DayType.WEEKEND -> TextMuted
                }
            )
            Spacer(Modifier.width(6.dp))
            Pill(
                text = if (entry.done) "已记录" else "未记录",
                bg = if (entry.done) OkGreenBg else WarnAmberBg,
                fg = if (entry.done) OkGreen else WarnAmber
            )
        }

        Spacer(Modifier.height(12.dp))

        // ---------- 出勤 ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceLow)
                .border(1.dp, OutlineVariantColor, RoundedCornerShape(16.dp))
                .padding(13.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("今日正常出勤", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    if (workday) {
                        Text(
                            "选「否」可登记请假、休假、调休",
                            fontSize = 11.5.sp,
                            color = TextMuted
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceHigh)
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SegButton("是", entry.attendance == Attendance.YES, false) {
                        commit(entry.copy(attendance = Attendance.YES))
                    }
                    Box {
                        SegButton("否", entry.attendance != Attendance.YES, true) {
                            menuOpen = true
                        }
                        DropdownMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("请假") },
                                onClick = {
                                    menuOpen = false
                                    commit(entry.copy(attendance = Attendance.LEAVE))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("休假") },
                                onClick = {
                                    menuOpen = false
                                    commit(entry.copy(attendance = Attendance.VACATION))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("调休") },
                                onClick = {
                                    menuOpen = false
                                    commit(entry.copy(attendance = Attendance.COMPDAY))
                                }
                            )
                        }
                    }
                }
            }
            if (locked) {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(9.dp))
                        .background(HolidayOrangeBg)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        "已登记：${entry.attendance.label}　当日不计正常工时，加班照常计入",
                        fontSize = 11.5.sp,
                        color = HolidayOrange
                    )
                }
            }
        }

        Spacer(Modifier.height(11.dp))

        // ---------- 工作内容 ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (locked) Color(0xFFF1ECF5) else SurfaceLow)
                .border(1.dp, OutlineVariantColor, RoundedCornerShape(16.dp))
                .padding(13.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .width(3.5.dp)
                        .height(15.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Purple)
                )
                Spacer(Modifier.width(8.dp))
                Text("工作内容", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                if (workday) {
                    Text(WorkHours.seasonLabel(date), fontSize = 11.sp, color = TextMuted)
                }
            }
            Spacer(Modifier.height(10.dp))

            Text("上午", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Spacer(Modifier.height(5.dp))
            OutlinedTextField(
                value = entry.am,
                onValueChange = { commit(entry.copy(am = it)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !locked,
                minLines = 1,
                maxLines = 4,
                placeholder = {
                    Text(
                        if (locked) "已选择${entry.attendance.label}，无需填写" else "上午做了什么工作？",
                        fontSize = 13.sp
                    )
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))

            Text("下午", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Spacer(Modifier.height(5.dp))
            OutlinedTextField(
                value = entry.pm,
                onValueChange = { commit(entry.copy(pm = it)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !locked,
                minLines = 1,
                maxLines = 4,
                placeholder = {
                    Text(
                        if (locked) "已选择${entry.attendance.label}，无需填写" else "下午做了什么工作？",
                        fontSize = 13.sp
                    )
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(Modifier.height(11.dp))

        // ---------- 加班 ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceLow)
                .border(1.dp, OutlineVariantColor, RoundedCornerShape(16.dp))
                .padding(13.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .width(3.5.dp)
                        .height(15.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(OtRed)
                )
                Spacer(Modifier.width(8.dp))
                Text("加班", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = entry.ot,
                onValueChange = { commit(entry.copy(ot = it)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 1,
                maxLines = 3,
                placeholder = { Text("加班做了哪些工作？（没有可留空）", fontSize = 13.sp) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("加班时长", fontSize = 12.sp, color = TextMuted)
                Spacer(Modifier.width(10.dp))
                OutlinedTextField(
                    value = otText,
                    onValueChange = { raw ->
                        val filtered = raw.filter { it.isDigit() || it == '.' }
                        otText = filtered
                        commit(entry.copy(otHours = filtered.toDoubleOrNull() ?: 0.0))
                    },
                    modifier = Modifier.width(104.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("小时", fontSize = 12.sp, color = TextMuted)
            }
        }

        Spacer(Modifier.height(14.dp))

        // ---------- 工时汇总 ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceMid)
                .padding(15.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("今日工时合计", fontSize = 13.sp, color = TextMuted)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            fmtHours(calc.total),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            " 小时",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(WorkHours.seasonLabel(date), fontSize = 11.sp, color = TextMuted)
                    Text(
                        "${fmtHours(calc.normal)} h",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(9.dp))
            Text(
                "正常工时 ${fmtHours(calc.normal)} ＋ 加班工时 ${fmtHours(calc.ot)} ＝ 合计 ${fmtHours(calc.total)} 小时",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(11.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                val total = calc.total
                if (total <= 0.0) {
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(SurfaceHigh)
                    )
                } else {
                    if (calc.normal > 0.0) {
                        Box(
                            Modifier
                                .weight(calc.normal.toFloat())
                                .fillMaxHeight()
                                .background(Purple)
                        )
                    }
                    if (calc.ot > 0.0) {
                        Box(
                            Modifier
                                .weight(calc.ot.toFloat())
                                .fillMaxHeight()
                                .background(OtRed)
                        )
                    }
                }
            }
            val note = noteText(calc.autoOt, calc.normal, entry, workday, type)
            if (note != null) {
                Spacer(Modifier.height(8.dp))
                Text(note, fontSize = 11.5.sp, color = TextMuted)
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { commit(entry.copy(done = !entry.done)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = entry.hasContent || entry.done,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple)
        ) {
            Text(
                if (entry.done) "修改记录" else "完成记录",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "每晚 ${store.reminderTime} 未记录时会提醒你",
            fontSize = 11.5.sp,
            color = TextMuted,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun noteText(
    autoOt: Double,
    normal: Double,
    entry: DayEntry,
    workday: Boolean,
    type: DayType
): String? = when {
    autoOt > 0.0 -> "其中 ${fmtHours(autoOt)} 小时由" +
            (if (type == DayType.WEEKEND) "周末" else "节假日") + "录入内容自动折算为加班"
    entry.attendance != Attendance.YES -> "已登记${entry.attendance.label}：当日正常工时不计，加班照常计算"
    !workday -> "休息日：正常工时不计，只统计加班时长"
    normal <= 0.0 -> "没有记录的工作日不计入工时"
    else -> null
}

@Composable
private fun SegButton(
    text: String,
    selected: Boolean,
    warn: Boolean,
    onClick: () -> Unit
) {
    val bg = when {
        !selected -> Color.Transparent
        warn -> HolidayOrange
        else -> Purple
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(17.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else TextMuted,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun trimNum(v: Double): String =
    if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
