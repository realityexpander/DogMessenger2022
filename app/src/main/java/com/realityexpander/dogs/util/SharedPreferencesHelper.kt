package com.realityexpander.dogs.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit
import androidx.core.text.isDigitsOnly

class SharedPreferencesHelper {

    companion object {

        private const val PREF_LAST_UPDATED_TIME_MS = "pref_last_updated_time_ms"
        private const val PREF_LAST_UPDATED_TIME_DATESTRING = "pref_last_updated_time_datestring"
        private const val PREF_NEXT_UPDATE_TIME_DATESTRING = "pref_next_update_time_datestring"
        private const val PREF_CACHE_REFRESH_INTERVAL_SECONDS = "pref_cache_interval_seconds"
        private const val DEFAULT_CACHE_REFRESH_INTERVAL_SECONDS = 500L // seconds
        private var prefs: SharedPreferences? = null

        @Volatile
        private var instance: SharedPreferencesHelper? = null
        private val LOCK = Any()

        operator fun invoke(context: Context): SharedPreferencesHelper =
            instance ?: synchronized(LOCK) {
                instance ?: buildHelper(context).also {
                    instance = it
                }
            }

        private fun buildHelper(context: Context): SharedPreferencesHelper {
            prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return SharedPreferencesHelper()
        }
    }

    fun getLastUpdatedTimeMs(): Long {
        val lastUpdatedTimeMs = prefs?.getLong(PREF_LAST_UPDATED_TIME_MS, 0L) ?: 0L

        return lastUpdatedTimeMs
    }

    fun saveLastUpdatedTimeMs(timeMs: Long) {
        val nextUpdateTime = timeMs + getCacheRefreshIntervalMs()

        prefs?.edit(commit = true) {
            putLong(PREF_LAST_UPDATED_TIME_MS, timeMs)

            // For display in preferences
            val lastUpdatedTimeDateString = timeMs.getDateStringWithSeconds()
            putString(PREF_LAST_UPDATED_TIME_DATESTRING, lastUpdatedTimeDateString)

            // For display in preferences
            val nextUpdateTimeDateString = nextUpdateTime.getDateStringWithSeconds()
            putString(PREF_NEXT_UPDATE_TIME_DATESTRING, nextUpdateTimeDateString)
        }
    }

    fun getCacheRefreshIntervalMs(): Long {
        val prefCacheRefreshIntervalSecondsStr = prefs?.getString(
            PREF_CACHE_REFRESH_INTERVAL_SECONDS,
            DEFAULT_CACHE_REFRESH_INTERVAL_SECONDS.toString()
        ) ?: DEFAULT_CACHE_REFRESH_INTERVAL_SECONDS.toString()

        if (prefCacheRefreshIntervalSecondsStr.isDigitsOnly()) {
            val clampedDurationSeconds = prefCacheRefreshIntervalSecondsStr.toLong().coerceIn(0, 100_000)

            saveCacheRefreshIntervalSeconds(clampedDurationSeconds)
            return clampedDurationSeconds * 1_000L // convert to ms
        }

        // save a default value if validation fails
        saveCacheRefreshIntervalSeconds(DEFAULT_CACHE_REFRESH_INTERVAL_SECONDS)
        return DEFAULT_CACHE_REFRESH_INTERVAL_SECONDS
    }

    fun saveCacheRefreshIntervalSeconds(durationSeconds: Long) {
        prefs?.edit(commit = true) {
            putString(PREF_CACHE_REFRESH_INTERVAL_SECONDS, durationSeconds.toString())
        }
    }


}