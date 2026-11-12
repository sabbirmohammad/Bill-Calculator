package com.powerunit.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wattwise_settings")

@Singleton
class SettingsPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val DEFAULT_GITHUB_REPO = "sabbirmohammad/Bill-Calculator"
        val KEY_BILLING_CYCLE_DAY = intPreferencesKey("billing_cycle_day")
        val KEY_CO2_EMISSION_FACTOR = doublePreferencesKey("co2_emission_factor")
        val KEY_REMINDER_TIME = stringPreferencesKey("reminder_time")
        val KEY_REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val KEY_USE_BENGALI_DIGITS = booleanPreferencesKey("use_bengali_digits")
        val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
        val KEY_APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val KEY_APP_LOCK_TIMEOUT_SECONDS = intPreferencesKey("app_lock_timeout_seconds")
        val KEY_HIDE_AMOUNTS = booleanPreferencesKey("hide_amounts")
        val KEY_SCREENSHOT_BLOCK = booleanPreferencesKey("screenshot_block")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_GITHUB_REPO = stringPreferencesKey("github_repo")
        val KEY_AUTO_CHECK_UPDATES = booleanPreferencesKey("auto_check_updates")
        val KEY_TARIFF_SEEDED = booleanPreferencesKey("tariff_seeded")
    }

    val tariffSeeded: Flow<Boolean> = context.dataStore.data.map { it[KEY_TARIFF_SEEDED] ?: false }

    val billingCycleDay: Flow<Int> = context.dataStore.data.map { it[KEY_BILLING_CYCLE_DAY] ?: 1 }
    val co2Factor: Flow<Double> = context.dataStore.data.map { it[KEY_CO2_EMISSION_FACTOR] ?: 0.55 }
    val reminderTime: Flow<String> = context.dataStore.data.map { it[KEY_REMINDER_TIME] ?: "21:00" }
    val reminderEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_REMINDER_ENABLED] ?: true }
    val useBengaliDigits: Flow<Boolean> = context.dataStore.data.map { it[KEY_USE_BENGALI_DIGITS] ?: false }
    val appLanguage: Flow<String> = context.dataStore.data.map { it[KEY_APP_LANGUAGE] ?: "en" }
    val appLockEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_APP_LOCK_ENABLED] ?: false }
    val appLockTimeoutSeconds: Flow<Int> = context.dataStore.data.map { it[KEY_APP_LOCK_TIMEOUT_SECONDS] ?: 0 }
    val hideAmounts: Flow<Boolean> = context.dataStore.data.map { it[KEY_HIDE_AMOUNTS] ?: false }
    val screenshotBlock: Flow<Boolean> = context.dataStore.data.map { it[KEY_SCREENSHOT_BLOCK] ?: false }
    val themeMode: Flow<String> = context.dataStore.data.map { it[KEY_THEME_MODE] ?: "SYSTEM" }
    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { it[KEY_DYNAMIC_COLOR] ?: false }
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[KEY_ONBOARDING_COMPLETED] ?: false }
    val githubRepo: Flow<String> = context.dataStore.data.map {
        val saved = it[KEY_GITHUB_REPO]
        if (saved.isNullOrBlank() || saved.contains("electricity-bill", ignoreCase = true) || !saved.contains('/')) {
            DEFAULT_GITHUB_REPO
        } else {
            saved
        }
    }
    val autoCheckUpdates: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_CHECK_UPDATES] ?: true }

    suspend fun setBillingCycleDay(day: Int) {
        context.dataStore.edit { it[KEY_BILLING_CYCLE_DAY] = day.coerceIn(1, 28) }
    }

    suspend fun setCo2Factor(factor: Double) {
        context.dataStore.edit { it[KEY_CO2_EMISSION_FACTOR] = factor }
    }

    suspend fun setReminderTime(time: String) {
        context.dataStore.edit { it[KEY_REMINDER_TIME] = time }
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_REMINDER_ENABLED] = enabled }
    }

    suspend fun setUseBengaliDigits(enabled: Boolean) {
        context.dataStore.edit { it[KEY_USE_BENGALI_DIGITS] = enabled }
    }

    suspend fun setAppLanguage(lang: String) {
        context.dataStore.edit { it[KEY_APP_LANGUAGE] = lang }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_APP_LOCK_ENABLED] = enabled }
    }

    suspend fun setAppLockTimeoutSeconds(seconds: Int) {
        context.dataStore.edit { it[KEY_APP_LOCK_TIMEOUT_SECONDS] = seconds }
    }

    suspend fun setHideAmounts(enabled: Boolean) {
        context.dataStore.edit { it[KEY_HIDE_AMOUNTS] = enabled }
    }

    suspend fun setScreenshotBlock(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SCREENSHOT_BLOCK] = enabled }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DYNAMIC_COLOR] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setGithubRepo(repo: String) {
        context.dataStore.edit { it[KEY_GITHUB_REPO] = repo.trim() }
    }

    suspend fun setAutoCheckUpdates(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_CHECK_UPDATES] = enabled }
    }

    suspend fun setTariffSeeded(seeded: Boolean) {
        context.dataStore.edit { it[KEY_TARIFF_SEEDED] = seeded }
    }

    suspend fun isTariffSeeded(): Boolean {
        return context.dataStore.data.first()[KEY_TARIFF_SEEDED] ?: false
    }
}
