package com.mee.moneytracker.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

object ReminderManager {
    fun scheduleReminder(
        context: Context,
        accountId: Long,
        name: String,
        type: String,
        amount: Double?,
        dueDay: Int?,          // Day of month for recurring reminders
        dueDateMillis: Long?,  // Exact epoch millis for one-time reminders
        isRecurring: Boolean
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("accountId", accountId)
            putExtra("name", name)
            putExtra("type", type)
            putExtra("amount", amount ?: 0.0)
            putExtra("dueDay", dueDay ?: 1)
            putExtra("dueDateMillis", dueDateMillis ?: 0L)
            putExtra("isRecurring", isRecurring)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            accountId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerMillis = if (isRecurring && dueDay != null) {
            val calendar = Calendar.getInstance().apply {
                val now = Calendar.getInstance()
                set(Calendar.DAY_OF_MONTH, dueDay.coerceIn(1, 28))
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (before(now)) {
                    add(Calendar.MONTH, 1)
                }
            }
            calendar.timeInMillis
        } else if (dueDateMillis != null && dueDateMillis > 0L) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = dueDateMillis
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            calendar.timeInMillis
        } else {
            return
        }

        try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
            Log.d("ReminderManager", "Scheduled reminder for $name (ID $accountId, Recurring: $isRecurring) at ${java.util.Date(triggerMillis)}")
        } catch (e: Exception) {
            Log.e("ReminderManager", "Error scheduling reminder: ${e.message}")
        }
    }

    fun cancelReminder(context: Context, accountId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            accountId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("ReminderManager", "Cancelled reminder for ID $accountId")
        }
    }
}
