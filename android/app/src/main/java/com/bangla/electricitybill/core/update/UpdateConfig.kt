package com.bangla.electricitybill.core.update

object UpdateConfig {
    /**
     * Default GitHub owner and repo name.
     * Can be customized or overridden if repository name changes.
     */
    const val GITHUB_OWNER = "sabbirmohammad"
    const val GITHUB_REPO = "Bill-Calculator"

    /**
     * Primary endpoint: GitHub Releases Atom feed (rate-limit free).
     * Does not require authentication and is not constrained by GitHub API 60 req/hr limits.
     */
    fun getReleasesAtomUrl(owner: String = GITHUB_OWNER, repo: String = GITHUB_REPO): String {
        return "https://github.com/$owner/$repo/releases.atom"
    }

    /**
     * Fallback endpoint: GitHub REST API (subject to 60 req/hr unauthenticated IP rate limit).
     */
    fun getLatestReleaseUrl(owner: String = GITHUB_OWNER, repo: String = GITHUB_REPO): String {
        return "https://api.github.com/repos/$owner/$repo/releases/latest"
    }

    /**
     * Fallback endpoint: GitHub web redirect for latest release (rate-limit free).
     */
    fun getLatestReleaseWebUrl(owner: String = GITHUB_OWNER, repo: String = GITHUB_REPO): String {
        return "https://github.com/$owner/$repo/releases/latest"
    }

    /**
     * User-facing GitHub releases webpage.
     */
    fun getReleasesPageUrl(owner: String = GITHUB_OWNER, repo: String = GITHUB_REPO): String {
        return "https://github.com/$owner/$repo/releases"
    }

    /**
     * Direct APK download URL for a specific release tag.
     */
    fun getDirectApkDownloadUrl(tag: String, fileName: String = "app-release.apk", owner: String = GITHUB_OWNER, repo: String = GITHUB_REPO): String {
        val safeTag = if (tag.startsWith("v", ignoreCase = true)) tag else "v$tag"
        return "https://github.com/$owner/$repo/releases/download/$safeTag/$fileName"
    }

    /**
     * Direct APK download URL for the latest release.
     */
    fun getLatestApkDownloadUrl(fileName: String = "app-release.apk", owner: String = GITHUB_OWNER, repo: String = GITHUB_REPO): String {
        return "https://github.com/$owner/$repo/releases/latest/download/$fileName"
    }
}

