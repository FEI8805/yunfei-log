package com.yunfei.log

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import com.yunfei.log.alarm.Notifier
import com.yunfei.log.alarm.Reminder
import com.yunfei.log.data.Store
import com.yunfei.log.ui.AppRoot
import com.yunfei.log.ui.YunfeiTheme

class MainActivity : ComponentActivity() {

    private val resumeTick = mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Notifier.ensureChannel(this)
        setContent {
            YunfeiTheme {
                AppRoot(store = Store.get(this), tick = resumeTick.value)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        resumeTick.value += 1
        Reminder.scheduleNext(this)
    }
}
