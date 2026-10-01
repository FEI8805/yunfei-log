package com.yunfei.log

import android.app.Application
import com.yunfei.log.alarm.Notifier
import com.yunfei.log.alarm.Reminder
import com.yunfei.log.data.Store

class YunfeiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Store.get(this)
        Notifier.ensureChannel(this)
        Reminder.scheduleNext(this)
    }
}
