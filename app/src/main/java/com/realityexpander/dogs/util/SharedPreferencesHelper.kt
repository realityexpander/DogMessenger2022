package com.realityexpander.dogs.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit
import androidx.core.text.isDigitsOnly

class SharedPreferencesHelper {

    companion object {

        private const val PREF_LAST_UPDATED_TIME = "pref_last_updated_time"
        private const val PREF_LAST_UPDATED_TIME_DATESTRING = "pref_last_updated_time_datestring"
        private const val PREF_CACHE_DURATION = "pref_cache_duration"
        private var prefs: SharedPreferences? = null

        @Volatile private var instance: SharedPreferencesHelper? = null
        private val LOCK = Any()

        operator fun invoke(context: Context): SharedPreferencesHelper = instance ?: synchronized(LOCK) {
            instance ?: buildHelper(context).also {
                instance = it
            }
        }

        private fun buildHelper(context: Context) : SharedPreferencesHelper {
            prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return SharedPreferencesHelper()
        }
    }

    fun saveLastUpdatedTime(time: Long) {
        prefs?.edit(commit = true) {
            putLong(PREF_LAST_UPDATED_TIME, time)

            val dateString = time.getDateString()
            putString(PREF_LAST_UPDATED_TIME_DATESTRING, dateString)
        }
    }

    fun getUpdateTime(): Long {
        val prefLong = prefs?.getLong(PREF_LAST_UPDATED_TIME,  0L) ?: 0L

        return prefLong.toLong().coerceIn(0, 10000)
    }

    fun saveCacheDuration(duration: Int) {
        prefs?.edit(commit = true) { putString(PREF_CACHE_DURATION, duration.toString()) }
    }

    fun getCacheDuration(): Int {
        val prefString = prefs?.getString(PREF_CACHE_DURATION, "5") ?: "5"
        if(prefString.isDigitsOnly()) return prefString.toInt().coerceIn(0, 10000)

        // save a default value if validation fails
        val defaultValue = 5
        saveCacheDuration(defaultValue)
        return defaultValue
    }
}