package com.bangla.electricitybill.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_APP_LANGUAGE = stringPreferencesKey("app_language") // "bn" or "en"
        val KEY_APP_THEME = stringPreferencesKey("app_theme") // "SYSTEM", "LIGHT", "DARK"
        val KEY_MONTHLY_BUDGET = stringPreferencesKey("monthly_budget")
        val KEY_REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val KEY_REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val KEY_REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val KEY_DISMISSED_UPDATE_VERSION = stringPreferencesKey("dismissed_update_version")
        val KEY_DISMISSED_UPDATE_TIME = androidx.datastore.preferences.core.longPreferencesKey("dismissed_update_time")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val appLanguage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_APP_LANGUAGE] ?: "bn"
    }

    val appTheme: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_APP_THEME] ?: "SYSTEM"
    }

    val monthlyBudget: Flow<BigDecimal?> = context.dataStore.data.map { preferences ->
        preferences[KEY_MONTHLY_BUDGET]?.let { if (it.isNotBlank()) BigDecimal(it) else null }
    }

    val reminderEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_REMINDER_ENABLED] ?: false
    }

    val reminderTime: Flow<Pair<Int, Int>> = context.dataStore.data.map { preferences ->
        val hour = preferences[KEY_REMINDER_HOUR] ?: 20
        val minute = preferences[KEY_REMINDER_MINUTE] ?: 0
        Pair(hour, minute)
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setAppLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_APP_LANGUAGE] = language
        }
    }

    suspend fun setAppTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_APP_THEME] = theme
        }
    }

    suspend fun setMonthlyBudget(budget: BigDecimal?) {
        context.dataStore.edit { preferences ->
            if (budget != null) {
                preferences[KEY_MONTHLY_BUDGET] = budget.toPlainString()
            } else {
                preferences.remove(KEY_MONTHLY_BUDGET)
            }
        }
    }

    suspend fun setReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_REMINDER_ENABLED] = enabled
            preferences[KEY_REMINDER_HOUR] = hour
            preferences[KEY_REMINDER_MINUTE] = minute
        }
    }

    suspend fun recordDismissedUpdate(version: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DISMISSED_UPDATE_VERSION] = version
            preferences[KEY_DISMISSED_UPDATE_TIME] = System.currentTimeMillis()
        }
    }

    val dismissedUpdateInfo: Flow<Pair<String?, Long>> = context.dataStore.data.map { preferences ->
        Pair(
            preferences[KEY_DISMISSED_UPDATE_VERSION],
            preferences[KEY_DISMISSED_UPDATE_TIME] ?: 0L
        )
    }
}
