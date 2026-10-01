package com.yunfei.log.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

/**
 * 极简本地存储：所有记录存成一个 JSON 字符串放在 SharedPreferences 里。
 * 对个人用量（每天一条）来说完全够用，也避免了引入数据库依赖。
 */
class Store private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("yunfei_log", Context.MODE_PRIVATE)

    private val entries = LinkedHashMap<String, DayEntry>()

    var reminderTime: String
        get() = prefs.getString(KEY_TIME, "21:00") ?: "21:00"
        set(value) {
            prefs.edit().putString(KEY_TIME, value).apply()
        }

    var retryEnabled: Boolean
        get() = prefs.getBoolean(KEY_RETRY, true)
        set(value) {
            prefs.edit().putBoolean(KEY_RETRY, value).apply()
        }

    var holidayOff: Boolean
        get() = prefs.getBoolean(KEY_HOLIDAY_OFF, true)
        set(value) {
            prefs.edit().putBoolean(KEY_HOLIDAY_OFF, value).apply()
        }

    init {
        load()
    }

    private fun load() {
        val raw = prefs.getString(KEY_ENTRIES, null) ?: return
        try {
            val obj = JSONObject(raw)
            val it = obj.keys()
            while (it.hasNext()) {
                val k = it.next()
                val o = obj.optJSONObject(k) ?: continue
                entries[k] = DayEntry.fromJson(k, o)
            }
        } catch (t: Throwable) {
            // 数据损坏时忽略，保持可用
        }
    }

    fun save() {
        val obj = JSONObject()
        entries.forEach { (k, v) -> obj.put(k, v.toJson()) }
        prefs.edit().putString(KEY_ENTRIES, obj.toString()).apply()
    }

    fun get(date: String): DayEntry = entries[date] ?: DayEntry(date)

    fun put(entry: DayEntry) {
        if (entry.hasContent || entry.done || entry.attendance != Attendance.YES) {
            entries[entry.date] = entry
        } else {
            entries.remove(entry.date)
        }
        save()
    }

    fun all(): List<DayEntry> = entries.values.toList()

    companion object {
        private const val KEY_ENTRIES = "entries_json"
        private const val KEY_TIME = "reminder_time"
        private const val KEY_RETRY = "retry_enabled"
        private const val KEY_HOLIDAY_OFF = "holiday_off"

        @Volatile
        private var instance: Store? = null

        fun get(context: Context): Store =
            instance ?: synchronized(this) {
                instance ?: Store(context.applicationContext).also { instance = it }
            }
    }
}
