package com.yunfei.log.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yunfei.log.data.Store
import com.yunfei.log.logic.Dates
import com.yunfei.log.logic.WorkHours

/** 闹钟到点时判断要不要提醒 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val store = Store.get(context)
        val today = Dates.today()
        val entry = store.get(today)
        val isRetry = intent.getIntExtra("slot", 0) > 0

        // 已记录 / 已登记请假休假调休 -> 不再提醒
        val handled = entry.handled

        // 周末与节假日默认不提醒（有录入内容说明在加班，照常提醒）
        val skipRestDay = store.holidayOff && !WorkHours.isWorkday(today) && !entry.hasWorkText

        if (!handled && !skipRestDay) {
            Notifier.show(context, isRetry)
        }

        // 顺延排下一天的闹钟
        Reminder.scheduleNext(context)
    }
}
