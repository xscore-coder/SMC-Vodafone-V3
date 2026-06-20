package com.vodafone.smc

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.vodafone.smc.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class WidgetRowItem(
    val id: String,
    val title: String,
    val priority: String,
    val summary: String,
    val isCritical: Boolean,
    val timestamp: Long
)

class PCProblemWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val db = AppDatabase.getDatabase(context, scope)
                val allProblems = db.appDao().getAllPCProblems().first()
                val allITAR = db.appDao().getAllITARRequests().first()
                val allVPN = db.appDao().getAllVPNRequests().first()
                val allNT = db.appDao().getAllNTCreations().first()

                val items = mutableListOf<WidgetRowItem>()

                val excludedStatuses = setOf("completed", "approved", "closed", "active")

                // PC Problems (Unfinished if status is not in excluded list)
                allProblems.filter { it.status.lowercase() !in excludedStatuses }.forEach {
                    items.add(WidgetRowItem(
                        id = it.ticketNumber,
                        title = it.site,
                        priority = it.priority,
                        summary = it.detailedSummary,
                        isCritical = it.priority == "Critical" || it.priority == "Urgent",
                        timestamp = it.createdAt
                    ))
                }

                // ITAR Requests (Unfinished if status is "Pending" or "Review Needed" and not in excluded list)
                allITAR.filter { (it.status == "Pending" || it.status == "Review Needed") && it.status.lowercase() !in excludedStatuses }.forEach {
                    items.add(WidgetRowItem(
                        id = it.rId,
                        title = it.employeeName,
                        priority = if (it.status == "Review Needed") "URGENT" else "MEDIUM",
                        summary = "ITAR: ${it.requestType}",
                        isCritical = it.status == "Review Needed",
                        timestamp = it.createdAt
                    ))
                }

                // VPN Requests (Unfinished if status is not in excluded list and not Rejected)
                allVPN.filter { it.status.lowercase() !in excludedStatuses && it.status != "Rejected" }.forEach {
                    items.add(WidgetRowItem(
                        id = it.srNumber,
                        title = it.employeeName,
                        priority = if (it.status == "Urgent") "URGENT" else "LOW",
                        summary = "VPN: ${it.vpnType}",
                        isCritical = it.status == "Urgent",
                        timestamp = it.createdAt
                    ))
                }

                // NT Creations (Unfinished if status is not in excluded list)
                allNT.filter { it.status.lowercase() !in excludedStatuses }.forEach {
                    items.add(WidgetRowItem(
                        id = it.reqId,
                        title = it.employeeName,
                        priority = if (it.status == "Urgent") "URGENT" else "MEDIUM",
                        summary = "NT: ${it.jobTitle} (${it.department})",
                        isCritical = it.status == "Urgent",
                        timestamp = it.createdAt
                    ))
                }

                val sortedUnfinished = items.sortedByDescending { it.timestamp }

                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId, sortedUnfinished)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        unfinishedList: List<WidgetRowItem>
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        views.setTextViewText(R.id.widget_count, "${unfinishedList.size} Active Item(s)")

        // Dynamically set empty state text for both requests and problems
        views.setTextViewText(
            R.id.widget_empty_state,
            "✨ System Secure & Active ✨\n\nAll workstations are online, and there are currently no unfinished items in the queue."
        )

        if (unfinishedList.isEmpty()) {
            views.setViewVisibility(R.id.widget_empty_state, View.VISIBLE)
            views.setViewVisibility(R.id.widget_row_1, View.GONE)
            views.setViewVisibility(R.id.widget_row_2, View.GONE)
            views.setViewVisibility(R.id.widget_row_3, View.GONE)
            views.setViewVisibility(R.id.widget_row_4, View.GONE)
        } else {
            views.setViewVisibility(R.id.widget_empty_state, View.GONE)

            val rowIds = listOf(R.id.widget_row_1, R.id.widget_row_2, R.id.widget_row_3, R.id.widget_row_4)
            val lblIdIds = listOf(R.id.widget_lbl_id_1, R.id.widget_lbl_id_2, R.id.widget_lbl_id_3, R.id.widget_lbl_id_4)
            val lblSiteIds = listOf(R.id.widget_lbl_site_1, R.id.widget_lbl_site_2, R.id.widget_lbl_site_3, R.id.widget_lbl_site_4)
            val lblPrioIds = listOf(R.id.widget_lbl_prio_1, R.id.widget_lbl_prio_2, R.id.widget_lbl_prio_3, R.id.widget_lbl_prio_4)
            val lblSummaryIds = listOf(R.id.widget_lbl_summary_1, R.id.widget_lbl_summary_2, R.id.widget_lbl_summary_3, R.id.widget_lbl_summary_4)

            for (i in 0 until 4) {
                if (i < unfinishedList.size) {
                    val item = unfinishedList[i]
                    views.setViewVisibility(rowIds[i], View.VISIBLE)
                    views.setTextViewText(lblIdIds[i], item.id)
                    views.setTextViewText(lblSiteIds[i], item.title)
                    views.setTextViewText(lblPrioIds[i], item.priority.uppercase())
                    views.setTextViewText(lblSummaryIds[i], item.summary)

                    if (item.isCritical) {
                        views.setInt(rowIds[i], "setBackgroundResource", R.drawable.widget_row_bg_critical)
                        views.setTextColor(lblPrioIds[i], android.graphics.Color.parseColor("#FCA5A5"))
                    } else {
                        views.setInt(rowIds[i], "setBackgroundResource", R.drawable.widget_row_bg_normal)
                        views.setTextColor(lblPrioIds[i], android.graphics.Color.parseColor("#94A3B8"))
                    }
                } else {
                    views.setViewVisibility(rowIds[i], View.GONE)
                }
            }
        }

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)
        views.setOnClickPendingIntent(R.id.widget_empty_state, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        fun triggerUpdate(context: Context) {
            val intent = Intent(context, PCProblemWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                ComponentName(context, PCProblemWidgetProvider::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}
