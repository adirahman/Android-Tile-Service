package com.arc.tilescreen

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ghost_input_prefs")

class QuickNoteStore(private val appContext: Context) {
    private val KEY_NOTE = stringPreferencesKey("quick_note")

    val noteFlow: Flow<String> = appContext.dataStore.data.map { prefs -> prefs[KEY_NOTE].orEmpty()}

    suspend fun setNote(note: String){
        appContext.dataStore.edit{ it[KEY_NOTE] = note}
    }

    suspend fun clear(){
        appContext.dataStore.edit{ it.remove(KEY_NOTE) }
    }
}