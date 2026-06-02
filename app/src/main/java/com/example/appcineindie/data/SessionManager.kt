package com.example.appcineindie.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    companion object {
        val USER_UID = stringPreferencesKey("user_uid")
        val USER_TYPE = stringPreferencesKey("user_type") // "cinephile" or "spectator"
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    suspend fun saveSession(uid: String, userType: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_UID] = uid
            preferences[USER_TYPE] = userType
            preferences[IS_LOGGED_IN] = true
        }
    }

    val userUid: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_UID]
    }

    val userType: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_TYPE]
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
