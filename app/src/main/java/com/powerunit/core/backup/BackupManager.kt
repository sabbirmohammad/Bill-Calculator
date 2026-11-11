package com.powerunit.core.backup

import android.content.Context
import com.powerunit.core.common.DateUtils
import com.powerunit.core.database.PowerUnitDatabase
import com.powerunit.core.database.entity.MeterEntity
import com.powerunit.core.database.entity.ReadingEntity
import com.powerunit.core.database.entity.TariffEntity
import com.powerunit.core.database.entity.TariffSlabEntity
import com.powerunit.domain.model.MeterType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigDecimal
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: PowerUnitDatabase
) {
    companion object {
        private const val ITERATIONS = 10000
        private const val KEY_LENGTH = 256
        private const val SALT_LENGTH = 16
        private const val IV_LENGTH = 12
        private const val TAG_LENGTH_BIT = 128
    }

    suspend fun createEncryptedBackup(passphrase: String): File = withContext(Dispatchers.IO) {
        val jsonString = exportDatabaseToJson()
        val plainBytes = jsonString.toByteArray(Charsets.UTF_8)

        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)

        val iv = ByteArray(IV_LENGTH)
        random.nextBytes(iv)

        val keySpec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKey = SecretKeySpec(factory.generateSecret(keySpec).encoded, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BIT, iv))
        val encryptedBytes = cipher.doFinal(plainBytes)

        val backupsDir = File(context.filesDir, "backups").apply { mkdirs() }
        val timestamp = DateUtils.formatDate(DateUtils.currentInstant(), "yyyyMMdd_HHmmss")
        val backupFile = File(backupsDir, "wattwise_backup_$timestamp.wattwise")

        FileOutputStream(backupFile).use { fos ->
            fos.write(salt)
            fos.write(iv)
            fos.write(encryptedBytes)
        }

        backupFile
    }

    suspend fun restoreEncryptedBackup(backupFile: File, passphrase: String): Int = withContext(Dispatchers.IO) {
        val fileBytes = FileInputStream(backupFile).use { it.readBytes() }
        require(fileBytes.size > SALT_LENGTH + IV_LENGTH) {
            "Invalid backup file: file is too small"
        }

        val salt = fileBytes.copyOfRange(0, SALT_LENGTH)
        val iv = fileBytes.copyOfRange(SALT_LENGTH, SALT_LENGTH + IV_LENGTH)
        val cipherBytes = fileBytes.copyOfRange(SALT_LENGTH + IV_LENGTH, fileBytes.size)

        val keySpec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKey = SecretKeySpec(factory.generateSecret(keySpec).encoded, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BIT, iv))
        val decryptedBytes = cipher.doFinal(cipherBytes)

        val jsonString = String(decryptedBytes, Charsets.UTF_8)
        importDatabaseFromJson(jsonString)
    }

    suspend fun exportDatabaseToJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", DateUtils.currentInstant().toString())

        // Meters
        val meters = database.meterDao().getAllMeters().first()
        val metersArray = JSONArray()
        meters.forEach { m ->
            val mObj = JSONObject().apply {
                put("id", m.id)
                put("name", m.name)
                put("type", m.type)
                put("startReading", m.startReading.toString())
                put("multiplierFactor", m.multiplierFactor.toString())
                put("isActive", m.isActive)
                put("createdAt", m.createdAt.toString())
            }
            metersArray.put(mObj)
        }
        root.put("meters", metersArray)

        // Readings
        val readings = database.readingDao().getReadingsByMeter(1L).first()
        val readingsArray = JSONArray()
        readings.forEach { r ->
            val rObj = JSONObject().apply {
                put("id", r.id)
                put("meterId", r.meterId)
                put("readingValue", r.readingValue.toString())
                put("recordedAt", r.recordedAt.toString())
                put("note", r.note ?: "")
                put("isEstimated", r.isEstimated)
                put("isMeterReset", r.isMeterReset)
                put("createdAt", r.createdAt.toString())
                put("updatedAt", r.updatedAt.toString())
            }
            readingsArray.put(rObj)
        }
        root.put("readings", readingsArray)

        return root.toString(2)
    }

    suspend fun importDatabaseFromJson(jsonString: String): Int {
        val root = JSONObject(jsonString)
        val readingsArray = root.getJSONArray("readings")
        val readings = mutableListOf<ReadingEntity>()

        for (i in 0 until readingsArray.length()) {
            val rObj = readingsArray.getJSONObject(i)
            readings.add(
                ReadingEntity(
                    id = rObj.getLong("id"),
                    meterId = rObj.getLong("meterId"),
                    readingValue = BigDecimal(rObj.getString("readingValue")),
                    recordedAt = Instant.parse(rObj.getString("recordedAt")),
                    note = rObj.optString("note").ifEmpty { null },
                    isEstimated = rObj.getBoolean("isEstimated"),
                    isMeterReset = rObj.getBoolean("isMeterReset"),
                    createdAt = Instant.parse(rObj.getString("createdAt")),
                    updatedAt = Instant.parse(rObj.getString("updatedAt"))
                )
            )
        }

        database.readingDao().insertReadings(readings)
        return readings.size
    }
}
