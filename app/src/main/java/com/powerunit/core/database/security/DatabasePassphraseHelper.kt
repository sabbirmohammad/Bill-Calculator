package com.powerunit.core.database.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabasePassphraseHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_FILE = "powerunit_secure_vault"
        private const val KEY_DB_PASSPHRASE = "db_passphrase_v1"
        private const val PASSPHRASE_BYTE_COUNT = 32 // 256-bit
    }

    @Synchronized
    fun getOrCreatePassphrase(): ByteArray {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val sharedPreferences = EncryptedSharedPreferences.create(
                context,
                PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            val existingPassphraseBase64 = sharedPreferences.getString(KEY_DB_PASSPHRASE, null)
            if (existingPassphraseBase64 != null) {
                Base64.getDecoder().decode(existingPassphraseBase64)
            } else {
                val newPassphrase = ByteArray(PASSPHRASE_BYTE_COUNT)
                SecureRandom().nextBytes(newPassphrase)
                val encoded = Base64.getEncoder().encodeToString(newPassphrase)
                sharedPreferences.edit().putString(KEY_DB_PASSPHRASE, encoded).apply()
                newPassphrase
            }
        } catch (e: Exception) {
            val fallbackPrefs = context.getSharedPreferences("powerunit_vault_fb", Context.MODE_PRIVATE)
            val existing = fallbackPrefs.getString(KEY_DB_PASSPHRASE, null)
            if (existing != null) {
                Base64.getDecoder().decode(existing)
            } else {
                val fallbackPass = ByteArray(PASSPHRASE_BYTE_COUNT)
                SecureRandom().nextBytes(fallbackPass)
                val encoded = Base64.getEncoder().encodeToString(fallbackPass)
                fallbackPrefs.edit().putString(KEY_DB_PASSPHRASE, encoded).apply()
                fallbackPass
            }
        }
    }
}
