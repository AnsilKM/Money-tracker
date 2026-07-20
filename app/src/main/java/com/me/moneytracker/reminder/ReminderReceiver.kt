package com.me.moneytracker.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.me.moneytracker.MainActivity

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val accountId = intent.getLongExtra("accountId", -1L)
        val name = intent.getStringExtra("name") ?: "Account"
        val type = intent.getStringExtra("type") ?: "CREDIT_CARD"
        val amount = intent.getDoubleExtra("amount", 0.0)
        val dueDay = intent.getIntExtra("dueDay", 1)
        val dueDateMillis = intent.getLongExtra("dueDateMillis", 0L)
        val isRecurring = intent.getBooleanExtra("isRecurring", true)

        if (accountId == -1L) return

        // Create Notification Channel
        val channelId = "payment_reminders"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Payment Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for credit cards, loan repayments, and single payments"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Title and description based on account type
        val title = when (type) {
            "LOAN" -> "Loan EMI Due Today"
            "PERSON" -> "Single Payment Due"
            else -> "Credit Card Bill Due"
        }
        val message = when (type) {
            "LOAN" -> "EMI of ₹%,.2f for $name is due.".format(amount)
            "PERSON" -> "Payment of ₹%,.2f for '$name' is due today.".format(amount)
            else -> "Repayment for Credit Card '$name' is due today."
        }

        // PendingIntent to launch MainActivity
        val mainIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            accountId.toInt(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Using standard Android alarm icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(accountId.toInt(), notification)

        // Only reschedule if recurring
        if (isRecurring) {
            ReminderManager.scheduleReminder(context, accountId, name, type, amount, dueDay, dueDateMillis, true)
        }
    }
}
