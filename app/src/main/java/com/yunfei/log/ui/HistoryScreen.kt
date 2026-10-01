package com.yunfei.log.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunfei.log.data.DayEntry
import com.yunfei.log.data.Store
import com.yunfei.log.logic.Dates
import com.yunfei.log.logic.WorkHours
import com.yunfei.log.logic.fmtHours

@Composable
fun HistoryScreen(store: Store, tick: Int, onEdit: (String) -> Unit) {
    var query by remember { mutableStateOf("") }

    val days = remember(tick, query) {
        val today = Dates.today()
        val q = query.trim()
        (0 until 180).mapNotNull { i ->
            val date = Dates.shiftDays(today, -i)
            val e = store.get(date)
            if (!e.hasContent && e.attendance.key == "yes") return@mapNotNull null
            val text = listOf(e.am, e.pm, e.ot).filter { it.isNotBlank() }.joinToString(" ")
            if (q.isNotEmpty() && !text.contains(q, ignoreCase = true)) return@mapNotNull null
            e
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("搜索工作内容…", fontSize = 13.5.sp) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            shape = RoundedCornerShape(22.dp)
        )
        Spacer(Modifier.height(12.dp))

        if (days.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceMid)
                    .padding(vertical = 34.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (query.isNotEmpty()) "没有匹配的记录" else "还没有任何记录",
                    fontSize = 13.5.sp,
                    color = TextMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(days, key = { it.date }) { e -> HistoryItem(e, onClick = { onEdit(e.date) }) }
                item { Spacer(Modifier.height(14.dp)) }
            }
        }
    }
}

@Composable
private fun HistoryItem(e: DayEntry, onClick: () -> Unit) {
    val calc = WorkHours.calc(e)
    val text = listOf(e.am, e.pm, e.ot).filter { it.isNotBlank() }.joinToString(" ").replace("\n", " ")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceLow)
            .border(1.dp, OutlineVariantColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.width(44.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "${Dates.day(e.date)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text("周" + "日一二三四五六"[Dates.weekday(e.date) - 1], fontSize = 10.5.sp, color = TextMuted)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text.ifBlank { "当天没有工作内容记录" },
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (text.isBlank()) TextMuted else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                WorkHours.typeLabel(e.date) +
                        if (calc.ot > 0.0) " · 加班 ${fmtHours(calc.ot)}h" else "",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
        Spacer(Modifier.width(8.dp))
        Pill(
            text = if (calc.total > 0.0) "${fmtHours(calc.total)}h" else "0h",
            bg = if (calc.ot > 0.0) OtRedBg else OkGreenBg,
            fg = if (calc.ot > 0.0) OtRed else OkGreen
        )
    }
}
