package com.yunfei.log.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.yunfei.log.data.Store
import com.yunfei.log.logic.Dates

@Composable
fun AppRoot(store: Store, tick: Int) {
    var showSplash by remember { mutableStateOf(true) }
    var tab by remember { mutableStateOf(0) }
    var date by remember { mutableStateOf(Dates.today()) }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    fun requestNotificationIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                when (tab) {
                    0 -> TodayScreen(store = store, date = date, tick = tick)
                    1 -> HistoryScreen(
                        store = store,
                        tick = tick,
                        onEdit = {
                            date = it
                            tab = 0
                        }
                    )
                    2 -> StatsScreen(store = store, tick = tick)
                    else -> SettingsScreen(
                        store = store,
                        tick = tick,
                        onRequestNotification = { requestNotificationIfNeeded() }
                    )
                }
            }

            NavigationBar(containerColor = SurfaceMid) {
                NavItem("✎", "今日", tab == 0) {
                    tab = 0
                    date = Dates.today()
                }
                NavItem("▤", "历史", tab == 1) { tab = 1 }
                NavItem("▦", "统计", tab == 2) { tab = 2 }
                NavItem("⚙", "设置", tab == 3) { tab = 3 }
            }
        }

        if (showSplash) {
            SplashScreen {
                showSplash = false
                requestNotificationIfNeeded()
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.NavItem(
    glyph: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Text(
                glyph,
                fontSize = 17.sp,
                color = if (selected) Color(0xFF1D192B) else TextMuted
            )
        },
        label = { Text(label, fontSize = 11.sp) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF1D192B),
            selectedTextColor = Color(0xFF1D192B),
            indicatorColor = PurpleSoft,
            unselectedTextColor = TextMuted
        )
    )
}
