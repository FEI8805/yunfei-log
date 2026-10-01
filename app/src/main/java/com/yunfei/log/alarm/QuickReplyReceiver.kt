package com.yunfei.log.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.yunfei.log.data.Store
import com.yunfei.log.logic.Dates

/** 通知栏直接回复，一句话完成当天记录 */
class QuickReplyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val text = RemoteInput.getResultsFromIntent(intent)
            ?.getCharSequence(Notifier.KEY_REPLY)
            ?.toString()
            ?.trim()

        if (!text.isNullOrEmpty()) {
            val store = Store.get(context)
            val today = Dates.today()
            val cur = store.get(today)
            val now = System.currentTimeMillis()

            val updated = when {
                cur.am.trim().isEmpty() -> cur.copy(am = text, done = true, updated = now)
                cur.pm.trim().isEmpty() -> cur.copy(pm = text, done = true, updated = now)
                else -> cur.copy(pm = cur.pm + "\n" + text, done = true, updated = now)
            }
            store.put(updated)
            Notifier.cancel(context)
        }

        Reminder.scheduleNext(context)
    }
}
