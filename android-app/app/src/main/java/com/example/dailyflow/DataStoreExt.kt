package com.example.dailyflow

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "daily_flow")
val TASK_STATE_KEY = stringPreferencesKey("task_state")
val REMINDER_STATE_KEY = stringPreferencesKey("reminder_state")
val ALARM_STATE_KEY = stringPreferencesKey("alarm_state")
