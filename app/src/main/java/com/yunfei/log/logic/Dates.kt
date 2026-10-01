package com.yunfei.log.logic

import java.util.Calendar
import java.util.Locale

object Dates {

    private const val WEEK_CHARS = "日一二三四五六"

    fun key(c: Calendar): String = String.format(
        Locale.US, "%04d-%02d-%02d",
        c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)
    )

    fun today(): String = key(Calendar.getInstance())

    fun calendarOf(date: String): Calendar {
        val parts = date.split("-")
        val c = Calendar.getInstance()
        c.clear()
        c.set(
            parts[0].toInt(),
            parts[1].toInt() - 1,
            parts[2].toInt(),
            12, 0, 0
        )
        return c
    }

    fun shiftDays(date: String, days: Int): String {
        val c = calendarOf(date)
        c.add(Calendar.DAY_OF_MONTH, days)
        return key(c)
    }

    fun year(date: String): Int = calendarOf(date).get(Calendar.YEAR)
    fun month(date: String): Int = calendarOf(date).get(Calendar.MONTH) + 1
    fun day(date: String): Int = calendarOf(date).get(Calendar.DAY_OF_MONTH)

    /** Calendar.SUNDAY = 1 ... Calendar.SATURDAY = 7 */
    fun weekday(date: String): Int = calendarOf(date).get(Calendar.DAY_OF_WEEK)

    fun isWeekend(date: String): Boolean {
        val w = weekday(date)
        return w == Calendar.SUNDAY || w == Calendar.SATURDAY
    }

    fun weekdayLabel(date: String): String =
        "星期" + WEEK_CHARS[weekday(date) - 1]

    fun daysInMonth(year: Int, month: Int): Int {
        val c = Calendar.getInstance()
        c.clear()
        c.set(year, month - 1, 1, 12, 0, 0)
        return c.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /** 该月 1 号是周几，按“周一为一周第一天”折算的偏移量（0..6） */
    fun firstDayOffset(year: Int, month: Int): Int {
        val c = Calendar.getInstance()
        c.clear()
        c.set(year, month - 1, 1, 12, 0, 0)
        val w = c.get(Calendar.DAY_OF_WEEK) // 1=周日
        return (w + 5) % 7
    }

    fun monthLabel(year: Int, month: Int): String = "${year}年${month}月"

    fun hm(millis: Long): String {
        val c = Calendar.getInstance()
        c.timeInMillis = millis
        return String.format(
            Locale.US, "%d月%d日 %02d:%02d",
            c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
            c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)
        )
    }
}
