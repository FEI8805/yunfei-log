package com.yunfei.log.util

import android.content.Context
import android.content.Intent
import com.yunfei.log.data.DayEntry
import com.yunfei.log.logic.Dates
import com.yunfei.log.logic.WorkHours
import com.yunfei.log.logic.fmtHours

object Export {

    fun dayMarkdown(e: DayEntry): String {
        val c = WorkHours.calc(e)
        val sb = StringBuilder()
        sb.append("# 云飞日志 · ")
            .append(Dates.month(e.date)).append("月")
            .append(Dates.day(e.date)).append("日（")
            .append(WorkHours.typeLabel(e.date)).append("）\n\n")
        sb.append("出勤：").append(e.attendance.label).append("\n\n")
        sb.append("## 上午\n").append(e.am.ifBlank { "—" }).append("\n\n")
        sb.append("## 下午\n").append(e.pm.ifBlank { "—" }).append("\n\n")
        sb.append("## 加班")
        if (c.ot > 0.0) sb.append("（").append(fmtHours(c.ot)).append(" 小时）")
        sb.append("\n").append(e.ot.ifBlank { "—" }).append("\n\n---\n")
        sb.append("工时合计：正常 ").append(fmtHours(c.normal))
            .append(" h + 加班 ").append(fmtHours(c.ot))
            .append(" h = ").append(fmtHours(c.total)).append(" h\n")
        if (c.autoOt > 0.0) {
            sb.append("（其中 ").append(fmtHours(c.autoOt)).append(" h 为休息日录入内容自动折算）\n")
        }
        return sb.toString()
    }

    fun share(context: Context, text: String, subject: String = "云飞日志") {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(send, "分享工作记录").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(chooser) }
    }
}
