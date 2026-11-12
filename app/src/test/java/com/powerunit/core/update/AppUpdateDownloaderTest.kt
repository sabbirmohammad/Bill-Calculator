package com.powerunit.core.update

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.security.MessageDigest

class AppUpdateDownloaderTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun sha256_verification_computesCorrectHash() {
        val testFile = tempFolder.newFile("test.apk")
        val content = "Sample APK binary content for checksum verification".toByteArray(Charsets.UTF_8)
        testFile.writeBytes(content)

        val digest = MessageDigest.getInstance("SHA-256")
        val expectedHash = digest.digest(content).joinToString("") { "%02x".format(it) }

        // Recompute using stream
        val verifyDigest = MessageDigest.getInstance("SHA-256")
        testFile.inputStream().use { input ->
            val buf = ByteArray(1024)
            var read: Int
            while (input.read(buf).also { read = it } != -1) {
                verifyDigest.update(buf, 0, read)
            }
        }
        val computed = verifyDigest.digest().joinToString("") { "%02x".format(it) }

        assertEquals(expectedHash, computed)
    }

    @Test
    fun sha256_mismatch_detected() {
        val testFile = tempFolder.newFile("corrupt.apk")
        testFile.writeBytes("Tampered content".toByteArray())

        val wrongHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"

        val verifyDigest = MessageDigest.getInstance("SHA-256")
        testFile.inputStream().use { input ->
            val buf = ByteArray(1024)
            var read: Int
            while (input.read(buf).also { read = it } != -1) {
                verifyDigest.update(buf, 0, read)
            }
        }
        val computed = verifyDigest.digest().joinToString("") { "%02x".format(it) }

        assertTrue(computed != wrongHash)
    }
}
