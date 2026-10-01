package com.yunfei.log.data

import org.json.JSONObject

/** 出勤状态：正常出勤 / 请假 / 休假 / 调休 */
enum class Attendance(val key: String, val label: String) {
    YES("yes", "正常出勤"),
    LEAVE("leave", "请假"),
    VACATION("vacation", "休假"),
    COMPDAY("compday", "调休");

    companion object {
        fun of(key: String?): Attendance {
            values().forEach { if (it.key == key) return it }
            return YES
        }
    }
}

/** 一天的记录 */
data class DayEntry(
    val date: String,
    val am: String = "",
    val pm: String = "",
    val ot: String = "",
    val otHours: Double = 0.0,
    val attendance: Attendance = Attendance.YES,
    val done: Boolean = false,
    val updated: Long = 0L
) {
    /** 工作内容框里是否有内容（上午或下午） */
    val hasWorkText: Boolean get() = am.trim().isNotEmpty() || pm.trim().isNotEmpty()

    val hasContent: Boolean get() = hasWorkText || ot.trim().isNotEmpty() || otHours > 0.0

    /** 该天是否已被用户处理过（用来判断要不要提醒） */
    val handled: Boolean get() = done || attendance != Attendance.YES || hasContent

    fun toJson(): JSONObject = JSONObject().apply {
        put("am", am)
        put("pm", pm)
        put("ot", ot)
        put("otHours", otHours)
        put("attendance", attendance.key)
        put("done", done)
        put("updated", updated)
    }

    companion object {
        fun fromJson(date: String, o: JSONObject) = DayEntry(
            date = date,
            am = o.optString("am", ""),
            pm = o.optString("pm", ""),
            ot = o.optString("ot", ""),
            otHours = o.optDouble("otHours", 0.0),
            attendance = Attendance.of(o.optString("attendance", "yes")),
            done = o.optBoolean("done", false),
            updated = o.optLong("updated", 0L)
        )
    }
}
