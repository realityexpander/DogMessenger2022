package com.realityexpander.dogs.util.mms

import android.app.Service
import android.content.Intent
import android.os.IBinder


/**
 * Needed to make default sms app for testing
 */
class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
