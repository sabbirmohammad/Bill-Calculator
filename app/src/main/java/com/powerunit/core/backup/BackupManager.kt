package com.powerunit.core.backup

import android.content.Context
import com.powerunit.core.common.DateUtils
import com.powerunit.core.database.PowerUnitDatabase
import com.powerunit.core.database.entity.MeterEntity
import com.powerunit.core.database.entity.ReadingEntity
import com.powerunit.core.database.entity.TariffVersionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigDecimal
import java.security.SecureRandom
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: PowerUnitDatabase
) {
    companion object {
        // OWASP 2024/2026 recommended iteration count for PBKDF2-HMAC-SHA256
        const val ITERATIONS_V2 = 600000
        const val ITERATIONS_LEGACY_V1 = 10000

        private const val KEY_LENGTH = 256
        private const val SALT_LENGTH = 16
        private const val IV_LENGTH = 12
        private const val TAG_LENGTH_BIT = 128

        val MAGIC_BYTES = byteArrayOf('W'.code.toByte(), 'A'.code.toByte(), 'T'.code.toByte(), 'T'.code.toByte())
        const val VERSION_2: Byte = 0x02
    }

    suspend fun createEncryptedBackup(passphrase: String): File = withContext(Dispatchers.IO) {
        val passphraseChars = passphrase.toCharArray()
        try {
            createEncryptedBackupInternal(passphraseChars)
        } finally {
            Arrays.fill(passphraseChars, '\u0000')
        }
    }

    private fun createEncryptedBackupInternal(passphraseChars: CharArray): File {
        val jsonString = exportDatabaseToJsonSync()
        val plainBytes = jsonString.toByteArray(Charsets.UTF_8)

        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)

        val iv = ByteArray(IV_LENGTH)
        random.nextBytes(iv)

        val keySpec = PBEKeySpec(passphraseChars, salt, ITERATIONS_V2, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val derivedKeyBytes = factory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(derivedKeyBytes, "AES")

        // Authenticated Header: MAGIC (4 bytes) + VERSION (1 byte)
        val header = ByteArray(MAGIC_BYTES.size + 1).apply {
            System.arraycopy(MAGIC_BYTES, 0, this, 0, MAGIC_BYTES.size)
            this[MAGIC_BYTES.size] = VERSION_2
        }

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BIT, iv))
        cipher.updateAAD(header)
        val encryptedBytes = cipher.doFinal(plainBytes)

        val backupsDir = File(context.filesDir, "backups").apply { mkdirs() }
        val timestamp = DateUtils.formatDate(DateUtils.currentInstant(), "yyyyMMdd_HHmmss")
        val backupFile = File(backupsDir, "wattwise_backup_$timestamp.wattwise")

        FileOutputStream(backupFile).use { fos ->
            fos.write(header)
            fos.write(salt)
            fos.write(iv)
            fos.write(encryptedBytes)
        }

        return backupFile
    }

    suspend fun restoreEncryptedBackup(backupFile: File, passphrase: String): Int = withContext(Dispatchers.IO) {
        val passphraseChars = passphrase.toCharArray()
        try {
            restoreEncryptedBackupInternal(backupFile, passphraseChars)
        } finally {
            Arrays.fill(passphraseChars, '\u0000')
        }
    }

    private fun restoreEncryptedBackupInternal(backupFile: File, passphraseChars: CharArray): Int {
        val fileBytes = FileInputStream(backupFile).use { it.readBytes() }
        require(fileBytes.size > SALT_LENGTH + IV_LENGTH) {
            "Invalid backup file: file is too small"
        }

        // Check if file starts with "WATT" magic header
        val hasMagic = fileBytes.size >= 5 &&
            fileBytes[0] == MAGIC_BYTES[0] &&
            fileBytes[1] == MAGIC_BYTES[1] &&
            fileBytes[2] == MAGIC_BYTES[2] &&
            fileBytes[3] == MAGIC_BYTES[3]

        val (iterations, offset, isV2) = if (hasMagic) {
            val version = fileBytes[4]
            if (version == VERSION_2) {
                Triple(ITERATIONS_V2, 5, true)
            } else {
                Triple(ITERATIONS_LEGACY_V1, 5, true)
            }
        } else {
            // Legacy V1 format: no header, 10,000 iterations, offset 0
            Triple(ITERATIONS_LEGACY_V1, 0, false)
        }

        require(fileBytes.size >= offset + SALT_LENGTH + IV_LENGTH) {
            "Corrupt backup file: incomplete headers"
        }

        val salt = fileBytes.copyOfRange(offset, offset + SALT_LENGTH)
        val iv = fileBytes.copyOfRange(offset + SALT_LENGTH, offset + SALT_LENGTH + IV_LENGTH)
        val cipherBytes = fileBytes.copyOfRange(offset + SALT_LENGTH + IV_LENGTH, fileBytes.size)

        val keySpec = PBEKeySpec(passphraseChars, salt, iterations, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKey = SecretKeySpec(factory.generateSecret(keySpec).encoded, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BIT, iv))
        if (isV2) {
            val header = fileBytes.copyOfRange(0, offset)
            cipher.updateAAD(header)
        }

        val decryptedBytes = try {
            cipher.doFinal(cipherBytes)
        } catch (e: Exception) {
            throw SecurityException("Invalid passphrase or corrupted backup file", e)
        }

        val jsonString = String(decryptedBytes, Charsets.UTF_8)
        return importDatabaseFromJsonSync(jsonString)
    }

    suspend fun exportDatabaseToJson(): String = withContext(Dispatchers.IO) {
        exportDatabaseToJsonSync()
    }

    private fun exportDatabaseToJsonSync(): String {
        val root = JSONObject()
        root.put("version", 2)
        root.put("exportedAt", DateUtils.currentInstant().toString())

        // 1. Meters (All meters)
        val meters = try {
            kotlinx.coroutines.runBlocking { database.meterDao().getAllMeters().first() }
        } catch (_: Exception) {
            emptyList()
        }
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

        // 2. Readings (All meters, backward-compatible fallback to meter 1)
        val readings = try {
            kotlinx.coroutines.runBlocking {
                val all = database.readingDao().getAllReadings().firstOrNull() ?: emptyList()
                if (all.isNotEmpty()) all else database.readingDao().getReadingsByMeter(1L).first()
            }
        } catch (_: Exception) {
            emptyList()
        }
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

        // 3. Tariff Versions
        try {
            val tariffVersions = kotlinx.coroutines.runBlocking {
                database.tariffVersionDao().getAllVersions().firstOrNull() ?: emptyList()
            }
            val versionsArray = JSONArray()
            tariffVersions.forEach { tv ->
                val tvObj = JSONObject().apply {
                    put("id", tv.id)
                    put("planId", tv.planId)
                    put("effectiveFromCycle", tv.effectiveFromCycle)
                    put("slabs", tv.slabs)
                    put("fixedCharge", tv.fixedCharge.toString())
                    put("demandCharge", tv.demandCharge.toString())
                    put("vatPercent", tv.vatPercent.toString())
                    put("rebatePercent", tv.rebatePercent.toString())
                    put("otherCharges", tv.otherCharges.toString())
                    put("createdAt", tv.createdAt)
                }
                versionsArray.put(tvObj)
            }
            root.put("tariffVersions", versionsArray)
        } catch (_: Exception) {
            // Ignore optional export
        }

        return root.toString(2)
    }

    suspend fun importDatabaseFromJson(jsonString: String): Int = withContext(Dispatchers.IO) {
        importDatabaseFromJsonSync(jsonString)
    }

    private fun importDatabaseFromJsonSync(jsonString: String): Int {
        val root = JSONObject(jsonString)

        // 1. Restore Meters if present
        if (root.has("meters")) {
            val metersArray = root.getJSONArray("meters")
            val meters = mutableListOf<MeterEntity>()
            for (i in 0 until metersArray.length()) {
                val mObj = metersArray.getJSONObject(i)
                meters.add(
                    MeterEntity(
                        id = mObj.getLong("id"),
                        name = mObj.getString("name"),
                        type = mObj.getString("type"),
                        startReading = BigDecimal(mObj.getString("startReading")),
                        multiplierFactor = BigDecimal(mObj.optString("multiplierFactor", "1.0")),
                        isActive = mObj.optBoolean("isActive", true),
                        createdAt = Instant.parse(mObj.getString("createdAt"))
                    )
                )
            }
            if (meters.isNotEmpty()) {
                kotlinx.coroutines.runBlocking {
                    try {
                        database.meterDao().insertMeters(meters)
                    } catch (_: Exception) {
                        meters.forEach { database.meterDao().insertMeter(it) }
                    }
                }
            }
        }

        // 2. Restore Readings
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

        kotlinx.coroutines.runBlocking {
            database.readingDao().insertReadings(readings)
        }

        // 3. Restore Tariff Versions if present
        if (root.has("tariffVersions")) {
            val tvArray = root.getJSONArray("tariffVersions")
            for (i in 0 until tvArray.length()) {
                val tvObj = tvArray.getJSONObject(i)
                val version = TariffVersionEntity(
                    id = tvObj.getLong("id"),
                    planId = tvObj.optLong("planId", 1L),
                    effectiveFromCycle = tvObj.getString("effectiveFromCycle"),
                    slabs = tvObj.getString("slabs"),
                    fixedCharge = BigDecimal(tvObj.getString("fixedCharge")),
                    demandCharge = BigDecimal(tvObj.getString("demandCharge")),
                    vatPercent = BigDecimal(tvObj.getString("vatPercent")),
                    rebatePercent = BigDecimal(tvObj.optString("rebatePercent", "0.0")),
                    otherCharges = BigDecimal(tvObj.optString("otherCharges", "0.0")),
                    createdAt = tvObj.optLong("createdAt", System.currentTimeMillis())
                )
                kotlinx.coroutines.runBlocking {
                    try {
                        database.tariffVersionDao().insertVersion(version)
                    } catch (_: Exception) {
                        // Ignore individual restore collision
                    }
                }
            }
        }

        return readings.size
    }
}
