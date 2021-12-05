package com.realityexpander.dogs.util.mms

import android.content.Context
import android.net.Uri
import android.util.Log
import com.klinker.android.send_message.MmsReceivedReceiver


class MmsReceivedReceiverImpl : MmsReceivedReceiver() {
    override fun onMessageReceived(context: Context, messageUri: Uri) {
        Log.v("MmsReceived", "message received: $messageUri")
    }

    override fun onError(context: Context, error: String) {
        Log.v("MmsReceived", "error: $error")
    }
}
