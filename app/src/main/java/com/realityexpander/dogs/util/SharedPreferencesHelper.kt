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
        private const val PREF_NEXT_UPDATE_TIME_DATESTRING = "pref_next_update_time_datestring"
        private const val PREF_CACHE_DURATION = "pref_cache_duration"
        private const val DEFAULT_CACHE_DURATION = 500
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

    fun getLastUpdatedTimeMs(): Long {
        val timeMs = prefs?.getLong(PREF_LAST_UPDATED_TIME, 0L) ?: 0L

        return timeMs
    }

    fun saveLastUpdatedTimeMs(timeMs: Long) {
        prefs?.edit(commit = true) {
            putLong(PREF_LAST_UPDATED_TIME, timeMs)

            // For display in preferences
            val lastUpdatedTimeDateString = timeMs.getDateStringWithSeconds()
            putString(PREF_LAST_UPDATED_TIME_DATESTRING, lastUpdatedTimeDateString)

            // For display in preferences
            val nextUpdateTimeDateString = (timeMs + getCacheRefreshIntervalMs()).getDateStringWithSeconds()
            putString(PREF_NEXT_UPDATE_TIME_DATESTRING, nextUpdateTimeDateString)

        }
    }

    fun getCacheRefreshIntervalMs(): Int {
        // prefString is in seconds
        val prefString = prefs?.getString(PREF_CACHE_DURATION, DEFAULT_CACHE_DURATION.toString()) ?: DEFAULT_CACHE_DURATION.toString()
        if(prefString.isDigitsOnly()) {
            val clampedInterval = prefString.toInt().coerceIn(0, 100_000)

            saveCacheRefreshIntervalMs(clampedInterval)
            return clampedInterval * 1_000 // convert to ms
        }

        // save a default value if validation fails
        saveCacheRefreshIntervalMs(DEFAULT_CACHE_DURATION)
        return DEFAULT_CACHE_DURATION
    }

    fun saveCacheRefreshIntervalMs(durationMs: Int) {
        prefs?.edit(commit = true) {
            putString(PREF_CACHE_DURATION, durationMs.toString())
        }
    }


}