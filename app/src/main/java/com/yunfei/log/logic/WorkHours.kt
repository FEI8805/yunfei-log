package com.yunfei.log.logic

import com.yunfei.log.data.Attendance
import com.yunfei.log.data.DayEntry
import kotlin.math.roundToInt

enum class DayType { WORKDAY, MAKEUP, WEEKEND, HOLIDAY }

data class DayCalc(
    val normal: Double,
    val autoOt: Double,
    val manualOt: Double
) {
    val ot: Double get() = autoOt + manualOt
    val total: Double get() = normal + ot
    val counted: Boolean get() = normal > 0.0 || ot > 0.0
}

/** 工时规则引擎 */
object WorkHours {

    /**
     * 法定节假日表。
     * 注意：这是 2026 年的示例数据，正式使用前请按国务院办公厅每年发布的
     * 《关于节假日安排的通知》核对，并逐年补充。
     */
    val HOLIDAYS: Map<String, String> = mapOf(
        "2026-01-01" to "元旦", "2026-01-02" to "元旦", "2026-01-03" to "元旦",
        "2026-02-16" to "春节", "2026-02-17" to "春节", "2026-02-18" to "春节",
        "2026-02-19" to "春节", "2026-02-20" to "春节", "2026-02-21" to "春节",
        "2026-02-22" to "春节",
        "2026-04-04" to "清明", "2026-04-05" to "清明", "2026-04-06" to "清明",
        "2026-05-01" to "劳动节", "2026-05-02" to "劳动节", "2026-05-03" to "劳动节",
        "2026-05-04" to "劳动节", "2026-05-05" to "劳动节",
        "2026-06-19" to "端午", "2026-06-20" to "端午", "2026-06-21" to "端午",
        "2026-09-25" to "中秋", "2026-09-26" to "中秋", "2026-09-27" to "中秋",
        "2026-10-01" to "国庆", "2026-10-02" to "国庆", "2026-10-03" to "国庆",
        "2026-10-04" to "国庆", "2026-10-05" to "国庆", "2026-10-06" to "国庆",
        "2026-10-07" to "国庆"
    )

    /** 调休上班日（周末但要上班） */
    val MAKEUP: Map<String, String> = mapOf(
        "2026-01-04" to "元旦调休",
        "2026-02-14" to "春节调休",
        "2026-02-28" to "春节调休",
        "2026-05-09" to "劳动节调休",
        "2026-10-10" to "国庆调休"
    )

    fun dayType(date: String): DayType {
        if (MAKEUP.containsKey(date)) return DayType.MAKEUP
        if (HOLIDAYS.containsKey(date)) return DayType.HOLIDAY
        return if (Dates.isWeekend(date)) DayType.WEEKEND else DayType.WORKDAY
    }

    fun isWorkday(date: String): Boolean {
        val t = dayType(date)
        return t == DayType.WORKDAY || t == DayType.MAKEUP
    }

    /** 夏时令：5月1日 - 9月30日 */
    fun isSummer(date: String): Boolean {
        val m = Dates.month(date)
        return m in 5..9
    }

    /** 标准工时：夏时令 9.5 小时（9:00-18:30），冬时令 9.0 小时（9:00-18:00） */
    fun standardHours(date: String): Double = if (isSummer(date)) 9.5 else 9.0

    fun seasonLabel(date: String): String =
        if (isSummer(date)) "夏时令 9:00–18:30" else "冬时令 9:00–18:00"

    fun typeLabel(date: String): String = when (dayType(date)) {
        DayType.HOLIDAY -> "法定节假日·" + (HOLIDAYS[date] ?: "")
        DayType.MAKEUP -> "调休上班日"
        DayType.WEEKEND -> "周末休息日"
        DayType.WORKDAY -> "正常工作日"
    }

    /**
     * 单日工时计算：
     * - 正常工作日 + 正常出勤 + 有工作内容 -> 计标准工时
     * - 没有记录的工作日 -> 不计工时
     * - 请假 / 休假 / 调休 -> 不计正常工时，加班照算
     * - 周末 / 法定节假日 + 有工作内容 -> 自动按标准工时折算为加班
     */
    fun calc(entry: DayEntry): DayCalc {
        val workday = isWorkday(entry.date)
        val std = standardHours(entry.date)
        val onLeave = entry.attendance != Attendance.YES
        var normal = 0.0
        var autoOt = 0.0

        if (workday) {
            if (!onLeave && entry.hasWorkText) normal = std
        } else if (!onLeave && entry.hasWorkText) {
            autoOt = std
        }

        return DayCalc(
            normal = normal,
            autoOt = autoOt,
            manualOt = if (entry.otHours > 0.0) entry.otHours else 0.0
        )
    }
}

/** 某个月的汇总 */
data class MonthStats(
    val year: Int,
    val month: Int,
    val normal: Double,
    val ot: Double,
    val attendDays: Int,
    val otDays: Int,
    val workdays: Int,
    val restdays: Int
) {
    val total: Double get() = normal + ot
    val avg: Double get() = if (attendDays > 0) total / attendDays else 0.0
}

fun monthStats(store: com.yunfei.log.data.Store, year: Int, month: Int): MonthStats {
    val days = Dates.daysInMonth(year, month)
    val todayKey = Dates.today()
    val todayCal = Dates.calendarOf(todayKey)
    var normal = 0.0
    var ot = 0.0
    var attend = 0
    var otDays = 0
    var workdays = 0
    var restdays = 0

    for (d in 1..days) {
        val key = String.format(java.util.Locale.US, "%04d-%02d-%02d", year, month, d)
        if (WorkHours.isWorkday(key)) workdays++ else restdays++
        if (Dates.calendarOf(key).after(todayCal)) continue
        val c = WorkHours.calc(store.get(key))
        normal += c.normal
        ot += c.ot
        if (c.counted) attend++
        if (c.ot > 0.0) otDays++
    }
    return MonthStats(year, month, normal, ot, attend, otDays, workdays, restdays)
}

fun fmtHours(v: Double): String {
    val r = (v * 10).roundToInt() / 10.0
    return if (r == r.toInt().toDouble()) r.toInt().toString() + ".0" else r.toString()
}
