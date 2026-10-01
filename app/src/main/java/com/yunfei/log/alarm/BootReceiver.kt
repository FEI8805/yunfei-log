package com.yunfei.log.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** 开机、应用更新、改时间、换时区后重新排闹钟 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Reminder.scheduleNext(context)
    }
}
