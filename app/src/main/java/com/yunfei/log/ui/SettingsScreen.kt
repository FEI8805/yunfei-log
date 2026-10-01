package com.yunfei.log.ui

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.yunfei.log.alarm.Reminder
import com.yunfei.log.data.Store
import com.yunfei.log.logic.Dates
import com.yunfei.log.logic.WorkHours
import com.yunfei.log.util.Export

private val TIME_OPTIONS = listOf("20:00", "20:30", "21:00", "21:30", "22:00", "22:30")

@Composable
fun SettingsScreen(store: Store, tick: Int, onRequestNotification: () -> Unit) {
    val context = LocalContext.current
    var reminder by remember(tick) { mutableStateOf(store.reminderTime) }
    var retry by remember(tick) { mutableStateOf(store.retryEnabled) }
    var holidayOff by remember(tick) { mutableStateOf(store.holidayOff) }
    var showHoliday by remember { mutableStateOf(false) }

    val notificationsOn = NotificationManagerCompat.from(context).areNotificationsEnabled()
    val exactOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(AlarmManager::class.java)?.canScheduleExactAlarms() ?: false
    } else true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 22.dp)
    ) {
        SectionTitle("提醒")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceLow)
                .padding(13.dp)
        ) {
            Text("每日提醒时间", fontSize = 14.sp)
            Spacer(Modifier.height(3.dp))
            Text("当天已记录则不再提醒", fontSize = 11.5.sp, color = TextMuted)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TIME_OPTIONS.forEach { t ->
                    val on = t == reminder
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (on) PurpleContainer else Color.Transparent)
                            .border(
                                1.dp,
                                if (on) Color.Transparent else OutlineVariantColor,
                                RoundedCornerShape(9.dp)
                            )
                            .clickable {
                                reminder = t
                                store.reminderTime = t
                                Reminder.scheduleNext(context)
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            t,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (on) Color(0xFF21005D) else TextMuted
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        SettingRow(
            title = "未记录时重复催办",
            desc = if (retry) "提醒时间后 30 / 60 / 90 分钟各再提醒一次" else "只在设定时间提醒一次"
        ) {
            Switch(
                checked = retry,
                onCheckedChange = {
                    retry = it
                    store.retryEnabled = it
                    Reminder.scheduleNext(context)
                },
                colors = SwitchDefaults.colors(checkedTrackColor = Purple)
            )
        }

        Spacer(Modifier.height(8.dp))
        SettingRow(
            title = "周末与节假日不提醒",
            desc = "当天已录入加班内容时仍会提醒"
        ) {
            Switch(
                checked = holidayOff,
                onCheckedChange = {
                    holidayOff = it
                    store.holidayOff = it
                },
                colors = SwitchDefaults.colors(checkedTrackColor = Purple)
            )
        }

        SectionTitle("工时规则")
        SettingRow(title = "夏时令", desc = "5月1日 – 9月30日　9:00 – 18:30") {
            Pill("9.5 h", OkGreenBg, OkGreen)
        }
        Spacer(Modifier.height(8.dp))
        SettingRow(title = "冬时令", desc = "10月1日 – 次年4月30日　9:00 – 18:00") {
            Pill("9.0 h", OkGreenBg, OkGreen)
        }
        Spacer(Modifier.height(8.dp))
        SettingRow(
            title = "法定节假日表",
            desc = "已内置 2026 年 · 含调休上班日",
            onClick = { showHoliday = true }
        ) {
            Text("›", fontSize = 20.sp, color = TextMuted)
        }

        SectionTitle("系统权限")
        SettingRow(
            title = "通知权限",
            desc = if (notificationsOn) "已开启，可以收到提醒" else "未开启，收不到任何提醒",
            onClick = {
                if (notificationsOn) openAppSettings(context) else onRequestNotification()
            }
        ) {
            Pill(
                if (notificationsOn) "已开启" else "去开启",
                if (notificationsOn) OkGreenBg else WarnAmberBg,
                if (notificationsOn) OkGreen else WarnAmber
            )
        }
        Spacer(Modifier.height(8.dp))
        SettingRow(
            title = "精确闹钟",
            desc = "Android 12+ 需要，否则 21:00 可能不准点",
            onClick = { openExactAlarmSettings(context) }
        ) {
            Pill(
                if (exactOk) "已开启" else "去开启",
                if (exactOk) OkGreenBg else WarnAmberBg,
                if (exactOk) OkGreen else WarnAmber
            )
        }
        Spacer(Modifier.height(8.dp))
        SettingRow(
            title = "后台运行 / 自启动",
            desc = "国产 ROM 建议设为「无限制」，避免闹钟被杀",
            onClick = { openAppSettings(context) }
        ) {
            Text("›", fontSize = 20.sp, color = TextMuted)
        }

        SectionTitle("数据")
        SettingRow(
            title = "导出今天的记录",
            desc = "Markdown 格式，可发微信或存进周报",
            onClick = {
                Export.share(context, Export.dayMarkdown(store.get(Dates.today())))
            }
        ) {
            Text("›", fontSize = 20.sp, color = TextMuted)
        }
        Spacer(Modifier.height(8.dp))
        SettingRow(
            title = "存储说明",
            desc = "全部数据保存在本机，不上传任何服务器"
        ) {
            Pill("本地", SurfaceHigh, TextMuted)
        }

        Spacer(Modifier.height(22.dp))
        Text(
            "云飞日志 v1.0",
            fontSize = 11.5.sp,
            color = TextMuted,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
    }

    if (showHoliday) {
        HolidayDialog(onDismiss = { showHoliday = false })
    }
}

@Composable
private fun SettingRow(
    title: String,
    desc: String? = null,
    onClick: (() -> Unit)? = null,
    action: @Composable () -> Unit
) {
    val click = onClick
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceLow)
            .border(1.dp, OutlineVariantColor, RoundedCornerShape(12.dp))
            .then(if (click != null) Modifier.clickable { click() } else Modifier)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp)
            if (desc != null) {
                Spacer(Modifier.height(3.dp))
                Text(desc, fontSize = 11.5.sp, color = TextMuted, lineHeight = 16.sp)
            }
        }
        Spacer(Modifier.width(12.dp))
        action()
    }
}

@Composable
private fun HolidayDialog(onDismiss: () -> Unit) {
    val holidays = WorkHours.HOLIDAYS
    val names = holidays.values.toSortedSet()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("法定节假日数据", fontSize = 17.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "共 ${names.size} 个假期，${WorkHours.MAKEUP.size} 个调休上班日",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Spacer(Modifier.height(10.dp))
                names.forEach { name ->
                    val days = holidays.filterValues { it == name }.keys.sorted()
                    val first = days.first()
                    Text(
                        "$name　${Dates.month(first)}/${Dates.day(first)} 起 ${days.size} 天",
                        fontSize = 13.sp,
                        lineHeight = 22.sp
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text("调休上班日", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                WorkHours.MAKEUP.keys.sorted().forEach { k ->
                    Text(
                        "${Dates.month(k)}/${Dates.day(k)}　${WorkHours.MAKEUP[k]}",
                        fontSize = 13.sp,
                        lineHeight = 22.sp
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "注意：节假日表需要按国务院办公厅每年发布的通知逐年核对更新。",
                    fontSize = 11.5.sp,
                    color = WarnAmber
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("知道了") }
        }
    )
}

fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}

fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        openAppSettings(context)
        return
    }
    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
        .onFailure { openAppSettings(context) }
}
