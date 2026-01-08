package com.arc.tilescreen

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class QuickStatus(val label:String){
    AVAILABLE("Available"),
    BUSY("Busy"),
    AWAY("Away")
}

private val Context.dataStore by preferencesDataStore(name = "quick_status_prefs")

class QuickStatusStore(private val appContext: Context) {

    private val KEY_STATUS = stringPreferencesKey("status")

    val statusFlow: Flow<QuickStatus> =
        appContext.dataStore.data.map { prefs ->
            val raw = prefs[KEY_STATUS]
            raw?.let{ runCatching { QuickStatus.valueOf(it) }.getOrNull() } ?: QuickStatus.AVAILABLE
        }

    suspend fun setStatus(status: QuickStatus){
        appContext.dataStore.edit{ it[KEY_STATUS] = status.name}
    }
}