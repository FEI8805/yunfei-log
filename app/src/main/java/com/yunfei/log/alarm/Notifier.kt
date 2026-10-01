package com.yunfei.log.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.yunfei.log.MainActivity
import com.yunfei.log.R

object Notifier {

    const val CHANNEL_ID = "daily_reminder"
    const val NOTIFICATION_ID = 2100
    const val KEY_REPLY = "reply_text"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_desc)
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun show(context: Context, retry: Boolean) {
        ensureChannel(context)
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val contentIntent = PendingIntent.getActivity(
            context,
            3002,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Android 12 起，带 RemoteInput 的 PendingIntent 必须是 MUTABLE
        var replyFlags = PendingIntent.FLAG_UPDATE_CURRENT
        replyFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            replyFlags or PendingIntent.FLAG_MUTABLE
        } else {
            replyFlags or PendingIntent.FLAG_IMMUTABLE
        }
        val replyIntent = PendingIntent.getBroadcast(
            context,
            3001,
            Intent(context, QuickReplyReceiver::class.java).apply {
                action = "com.yunfei.log.action.REPLY"
            },
            replyFlags
        )

        val remoteInput = RemoteInput.Builder(KEY_REPLY)
            .setLabel(context.getString(R.string.notif_reply_hint))
            .build()

        val replyAction = NotificationCompat.Action.Builder(
            R.drawable.ic_stat_log,
            context.getString(R.string.notif_reply),
            replyIntent
        ).addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .build()

        val title = if (retry) "还没记录今天的工作（再次提醒）"
        else context.getString(R.string.notif_title)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_log)
            .setContentTitle(title)
            .setContentText(context.getString(R.string.notif_text))
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "花 1 分钟写一句今天做了什么吧，直接在通知里回复就能记录，不用打开 App。"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .addAction(replyAction)

        try {
            manager.notify(NOTIFICATION_ID, builder.build())
        } catch (t: Throwable) {
            // 用户关闭通知权限等情况，忽略
        }
    }

    fun cancel(context: Context) {
        runCatching { NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID) }
    }
}
