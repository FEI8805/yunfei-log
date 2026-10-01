package com.yunfei.log.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.yunfei.log.data.Store
import java.util.Calendar

/** 提醒调度：每天在设定时间触发，未记录时按配置重复催办 */
object Reminder {

    private const val MAX_SLOTS = 8
    private const val REQUEST_BASE = 1000
    const val ACTION_ALARM = "com.yunfei.log.action.ALARM"

    fun scheduleNext(context: Context) {
        val store = Store.get(context)
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        for (i in 0 until MAX_SLOTS) {
            runCatching { am.cancel(pendingIntent(context, i)) }
        }

        slots(store.reminderTime, store.retryEnabled).forEachIndexed { index, triggerAt ->
            if (index < MAX_SLOTS) setExact(am, triggerAt, pendingIntent(context, index))
        }
    }

    private fun slots(time: String, retry: Boolean): List<Long> {
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 21
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val offsets = if (retry) intArrayOf(0, 30, 60, 90) else intArrayOf(0)
        val now = System.currentTimeMillis()
        val result = ArrayList<Long>()

        for (dayOffset in 0..1) {
            val base = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            for (offset in offsets) {
                val t = base + offset * 60_000L
                if (t > now + 3_000L) result.add(t)
                if (result.size >= MAX_SLOTS) return result
            }
        }
        return result
    }

    private fun setExact(am: AlarmManager, triggerAt: Long, pi: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } catch (t: Throwable) {
            runCatching { am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi) }
        }
    }

    private fun pendingIntent(context: Context, index: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_ALARM
            putExtra("slot", index)
        }
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = flags or PendingIntent.FLAG_IMMUTABLE
        }
        return PendingIntent.getBroadcast(context, REQUEST_BASE + index, intent, flags)
    }
}
