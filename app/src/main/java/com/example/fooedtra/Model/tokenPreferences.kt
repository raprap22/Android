package com.example.fooedtra.Model

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class tokenPreferences private constructor(private val dataStore: DataStore<androidx.datastore.preferences.core.Preferences>) {
    fun getUser(): Flow<tokenModel> {
        return dataStore.data.map { preferences ->
            tokenModel(
                preferences[TOKEN_KEY] ?:""
            )
        }
    }

    suspend fun saveToken(token: tokenModel) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = "Bearer "+ token
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: tokenPreferences? = null

        private val TOKEN_KEY = stringPreferencesKey("token")

        fun getInstance(dataStore: DataStore<androidx.datastore.preferences.core.Preferences>): tokenPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = tokenPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}