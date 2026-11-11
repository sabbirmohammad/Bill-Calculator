package com.powerunit.core.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubUpdateServiceTest {

    @Test
    fun testCleanVersion() {
        assertEquals("1.0.1", GitHubUpdateService.cleanVersion("v1.0.1"))
        assertEquals("1.0.1", GitHubUpdateService.cleanVersion("V1.0.1"))
        assertEquals("1.0.1", GitHubUpdateService.cleanVersion("1.0.1"))
        assertEquals("1.2.0", GitHubUpdateService.cleanVersion("v1.2.0-beta1"))
        assertEquals("1.0.1", GitHubUpdateService.cleanVersion("release-1.0.1"))
        assertEquals("2.3.4", GitHubUpdateService.cleanVersion("WattWise-v2.3.4"))
        assertEquals("1.0.1", GitHubUpdateService.cleanVersion("v1.0.1-rc.1"))
        assertEquals("2", GitHubUpdateService.cleanVersion("v2"))
    }

    @Test
    fun testIsNewerVersion() {
        // Higher patch
        assertTrue(GitHubUpdateService.isNewerVersion("1.0.1", "1.0.0"))
        assertTrue(GitHubUpdateService.isNewerVersion("v1.0.2", "1.0.1"))

        // Higher minor
        assertTrue(GitHubUpdateService.isNewerVersion("1.1.0", "1.0.9"))
        assertTrue(GitHubUpdateService.isNewerVersion("1.10.0", "1.9.5"))

        // Higher major
        assertTrue(GitHubUpdateService.isNewerVersion("2.0.0", "1.9.9"))
        assertTrue(GitHubUpdateService.isNewerVersion("v2", "1.5.0"))
        assertTrue(GitHubUpdateService.isNewerVersion("release-1.2.0", "1.1.9"))

        // Multi-component (4 parts)
        assertTrue(GitHubUpdateService.isNewerVersion("1.0.0.1", "1.0.0"))

        // Equal version
        assertFalse(GitHubUpdateService.isNewerVersion("1.0.0", "1.0.0"))
        assertFalse(GitHubUpdateService.isNewerVersion("v1.0.0", "1.0.0"))
        assertFalse(GitHubUpdateService.isNewerVersion("1.0", "1.0.0"))

        // Older version
        assertFalse(GitHubUpdateService.isNewerVersion("0.9.9", "1.0.0"))
        assertFalse(GitHubUpdateService.isNewerVersion("1.0.0", "1.0.1"))
        assertFalse(GitHubUpdateService.isNewerVersion("1.1.0", "1.2.0"))
        assertFalse(GitHubUpdateService.isNewerVersion("1.9.0", "1.10.0"))
    }
}
