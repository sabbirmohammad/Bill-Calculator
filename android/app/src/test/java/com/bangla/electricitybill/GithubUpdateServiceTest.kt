package com.bangla.electricitybill

import com.bangla.electricitybill.core.update.GithubUpdateService
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GithubUpdateServiceTest {

    private val sampleAtomXml = """<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom" xml:lang="en-US">
  <id>tag:github.com,2008:https://github.com/sabbirmohammad/Bill-Calculator/releases</id>
  <link type="text/html" rel="alternate" href="https://github.com/sabbirmohammad/Bill-Calculator/releases"/>
  <link type="application/atom+xml" rel="self" href="https://github.com/sabbirmohammad/Bill-Calculator/releases.atom"/>
  <title>Release notes from Bill-Calculator</title>
  <updated>2026-11-02T20:31:52Z</updated>
  <entry>
    <id>tag:github.com,2008:Repository/1443454072/v1.0.0</id>
    <updated>2026-11-02T20:36:39Z</updated>
    <link rel="alternate" type="text/html" href="https://github.com/sabbirmohammad/Bill-Calculator/releases/tag/v1.0.0"/>
    <title>v1.0.0 - বাংলাদেশ বিদ্যুৎ বিল ক্যালকুলেটর (Android &amp; Web)</title>
    <content type="html">&lt;h2&gt;⚡ বাংলাদেশ বিদ্যুৎ মিটার রিডিং ও বিল ক্যালকুলেটর (v1.0.0)&lt;/h2&gt;
&lt;h3&gt;📱 সরাসরি APK ডাউনলোড ও ইনস্টলেশন&lt;/h3&gt;
&lt;ul&gt;
&lt;li&gt;&lt;strong&gt;১০০% অফলাইন:&lt;/strong&gt; ইন্টারনেট ছাড়াই সমস্ত হিসাব।&lt;/li&gt;
&lt;li&gt;&lt;strong&gt;স্বয়ংক্রিয় ওটিএ আপডেট:&lt;/strong&gt; গিটহাবে নতুন রিলিজ আসলে সরাসরি আপডেট।&lt;/li&gt;
&lt;/ul&gt;</content>
  </entry>
</feed>"""

    @Test
    fun testVersionComparison() {
        val service = GithubUpdateService(OkHttpClient(), Gson())

        assertTrue(service.isVersionNewer(remote = "1.0.2", current = "1.0.1"))
        assertTrue(service.isVersionNewer(remote = "1.0.1", current = "1.0.0"))
        assertTrue(service.isVersionNewer(remote = "2.0.0", current = "1.9.9"))
        assertTrue(service.isVersionNewer(remote = "1.1.0", current = "1.0.9"))
        assertFalse(service.isVersionNewer(remote = "1.0.2", current = "1.0.2"))
        assertFalse(service.isVersionNewer(remote = "1.0.1", current = "1.0.1"))
        assertFalse(service.isVersionNewer(remote = "1.0.0", current = "1.0.0"))
        assertFalse(service.isVersionNewer(remote = "1.0.1", current = "1.0.2"))
        assertFalse(service.isVersionNewer(remote = "0.9.9", current = "1.0.0"))
        assertFalse(service.isVersionNewer(remote = "", current = "1.0.0"))
    }

    @Test
    fun testHtmlToMarkdownFormatting() {
        val service = GithubUpdateService(OkHttpClient(), Gson())

        val html = "<h2>Title</h2><p>Description</p><ul><li>Feature 1</li><li>Feature 2</li></ul>"
        val formatted = service.formatHtmlToMarkdown(html)

        assertTrue(formatted.contains("Title"))
        assertTrue(formatted.contains("Description"))
        assertTrue(formatted.contains("• Feature 1"))
        assertTrue(formatted.contains("• Feature 2"))
        assertFalse(formatted.contains("<h2>"))
        assertFalse(formatted.contains("<li>"))
    }

    @Test
    fun testAtomFeedBypassesRateLimit403() = runBlocking {
        // OkHttpClient interceptor that simulates GitHub REST API returning 403 Forbidden,
        // but Atom Feed returning 200 OK
        val mockClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val url = request.url.toString()
                if (url.endsWith("releases.atom")) {
                    Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body(sampleAtomXml.toResponseBody("application/atom+xml".toMediaType()))
                        .build()
                } else if (url.contains("api.github.com")) {
                    Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(403)
                        .message("Rate Limit Exceeded")
                        .body("""{"message":"API rate limit exceeded"}""".toResponseBody("application/json".toMediaType()))
                        .build()
                } else {
                    Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body("".toResponseBody("text/plain".toMediaType()))
                        .build()
                }
            }
            .build()

        val service = GithubUpdateService(mockClient, Gson())
        val result = service.checkLatestRelease(currentVersionName = "0.9.0")

        assertTrue("Expected check to succeed via Atom feed", result.isSuccess)
        val info = result.getOrNull()
        assertNotNull(info)
        assertEquals("1.0.0", info?.versionName)
        assertEquals("v1.0.0 - বাংলাদেশ বিদ্যুৎ বিল ক্যালকুলেটর (Android & Web)", info?.title)
        assertTrue(info?.isUpdateAvailable == true)
        assertEquals(
            "https://github.com/sabbirmohammad/Bill-Calculator/releases/download/v1.0.0/app-release.apk",
            info?.apkDownloadUrl
        )
        assertTrue(info?.releaseNotes?.contains("১০০% অফলাইন") == true)
    }

    @Test
    fun testUpToDateVersionCheck() = runBlocking {
        val mockClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(sampleAtomXml.toResponseBody("application/atom+xml".toMediaType()))
                    .build()
            }
            .build()

        val service = GithubUpdateService(mockClient, Gson())
        val result = service.checkLatestRelease(currentVersionName = "1.0.0")

        assertTrue(result.isSuccess)
        val info = result.getOrNull()
        assertNotNull(info)
        assertFalse("Update should not be available when on 1.0.0", info?.isUpdateAvailable == true)
    }
}
