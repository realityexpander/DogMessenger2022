package com.realityexpander.dogs.util.mms

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.realityexpander.dogs.R


/**
 * Needed to make default sms app for testing
 */
class SmsReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onReceive(context: Context, intent: Intent) {
        val smsExtra = intent.extras?.get("pdus") as Array<*>
        var body = ""
        for (i in smsExtra.indices) {
            val sms = SmsMessage.createFromPdu(smsExtra[i] as ByteArray)
            body += sms.messageBody
        }
        val notification: Notification = Notification.Builder(context)
            .setContentText(body)
            .setContentTitle("New Message")
            .setSmallIcon(com.klinker.android.send_message.R.drawable.ic_alert)
            .setStyle(Notification.BigTextStyle().bigText(body))
            .build()
        val notificationManagerCompat: NotificationManagerCompat =
            NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(1, notification)
    }
}
