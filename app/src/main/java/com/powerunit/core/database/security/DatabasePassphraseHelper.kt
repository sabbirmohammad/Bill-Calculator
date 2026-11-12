package com.powerunit.core.database.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabasePassphraseHelper @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "wattwise_db_master_key_v1"
        private const val PREFS_FILE = "wattwise_secure_vault_prefs"
        private const val KEY_ENCRYPTED_PASSPHRASE = "enc_db_passphrase"
        private const val KEY_IV = "enc_db_iv"
        private const val PASSPHRASE_BYTE_COUNT = 32 // 256-bit

        // Legacy Tink EncryptedSharedPreferences prefs names for migration
        private const val LEGACY_PREFS_FILE = "powerunit_secure_vault"
        private const val LEGACY_FB_PREFS_FILE = "powerunit_vault_fb"
        private const val LEGACY_KEY_DB_PASSPHRASE = "db_passphrase_v1"
    }

    @Volatile
    private var inMemoryCachedPassphrase: ByteArray? = null

    @Synchronized
    fun getOrCreatePassphrase(): ByteArray {
        inMemoryCachedPassphrase?.let { return it }

        val prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        val encryptedBase64 = prefs.getString(KEY_ENCRYPTED_PASSPHRASE, null)
        val ivBase64 = prefs.getString(KEY_IV, null)

        if (!encryptedBase64.isNullOrBlank() && !ivBase64.isNullOrBlank()) {
            try {
                val encryptedBytes = Base64.getDecoder().decode(encryptedBase64)
                val iv = Base64.getDecoder().decode(ivBase64)
                val decrypted = decryptWithKeystore(encryptedBytes, iv)
                inMemoryCachedPassphrase = decrypted
                return decrypted
            } catch (_: Exception) {
                // If Keystore is temporarily unavailable or non-Android, fall through safely
            }
        }

        // Try migrating existing passphrase from legacy vault so existing users do not lose data
        val legacyPassphrase = tryMigrateLegacyPassphrase()
        val passphraseToPersist = legacyPassphrase ?: ByteArray(PASSPHRASE_BYTE_COUNT).also {
            SecureRandom().nextBytes(it)
        }

        try {
            val (encrypted, iv) = encryptWithKeystore(passphraseToPersist)
            prefs.edit()
                .putString(KEY_ENCRYPTED_PASSPHRASE, Base64.getEncoder().encodeToString(encrypted))
                .putString(KEY_IV, Base64.getEncoder().encodeToString(iv))
                .commit()
            inMemoryCachedPassphrase = passphraseToPersist
            return passphraseToPersist
        } catch (_: Exception) {
            // For unit testing environments without AndroidKeyStore provider, store in fallback
            val fbPrefs = context.getSharedPreferences(LEGACY_FB_PREFS_FILE, Context.MODE_PRIVATE)
            val fbExisting = fbPrefs.getString(LEGACY_KEY_DB_PASSPHRASE, null)
            val fbBytes = if (fbExisting != null) {
                Base64.getDecoder().decode(fbExisting)
            } else {
                val encoded = Base64.getEncoder().encodeToString(passphraseToPersist)
                fbPrefs.edit().putString(LEGACY_KEY_DB_PASSPHRASE, encoded).commit()
                passphraseToPersist
            }
            inMemoryCachedPassphrase = fbBytes
            return fbBytes
        }
    }

    private fun getOrCreateMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            if (entry != null) return entry.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false) // Allows access during boot/WorkManager
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun encryptWithKeystore(plainBytes: ByteArray): Pair<ByteArray, ByteArray> {
        val secretKey = getOrCreateMasterKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainBytes)
        return Pair(encrypted, iv)
    }

    private fun decryptWithKeystore(encryptedBytes: ByteArray, iv: ByteArray): ByteArray {
        val secretKey = getOrCreateMasterKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(encryptedBytes)
    }

    private fun tryMigrateLegacyPassphrase(): ByteArray? {
        // 1. Check legacy EncryptedSharedPreferences
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val legacyEncPrefs = EncryptedSharedPreferences.create(
                context,
                LEGACY_PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            val legacyValue = legacyEncPrefs.getString(LEGACY_KEY_DB_PASSPHRASE, null)
            if (!legacyValue.isNullOrBlank()) {
                return Base64.getDecoder().decode(legacyValue)
            }
        } catch (_: Exception) {
            // Ignore legacy read failure
        }

        // 2. Check legacy fallback file
        try {
            val fbPrefs = context.getSharedPreferences(LEGACY_FB_PREFS_FILE, Context.MODE_PRIVATE)
            val fbValue = fbPrefs.getString(LEGACY_KEY_DB_PASSPHRASE, null)
            if (!fbValue.isNullOrBlank()) {
                return Base64.getDecoder().decode(fbValue)
            }
        } catch (_: Exception) {
            // Ignore
        }

        return null
    }
}
