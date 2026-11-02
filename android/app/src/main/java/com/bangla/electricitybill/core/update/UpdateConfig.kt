package com.bangla.electricitybill.core.update

object UpdateConfig {
    /**
     * Default GitHub owner and repo name.
     * Can be customized or overridden if repository name changes.
     */
    const val GITHUB_OWNER = "sabbirmohammad"
    const val GITHUB_REPO = "Bill-Calculator"

    fun getLatestReleaseUrl(owner: String = GITHUB_OWNER, repo: String = GITHUB_REPO): String {
        return "https://api.github.com/repos/$owner/$repo/releases/latest"
    }

    fun getReleasesPageUrl(owner: String = GITHUB_OWNER, repo: String = GITHUB_REPO): String {
        return "https://github.com/$owner/$repo/releases"
    }
}
