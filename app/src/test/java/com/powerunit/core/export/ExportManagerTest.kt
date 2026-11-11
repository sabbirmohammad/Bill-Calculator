package com.powerunit.core.export

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ExportManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val context = mockk<Context>(relaxed = true)
    private lateinit var exportManager: ExportManager

    @Before
    fun setUp() {
        every { context.cacheDir } returns tempFolder.root
        exportManager = ExportManager(context)
    }

    @Test
    fun exportManager_initializesProperly() {
        assertNotNull(exportManager)
    }
}
