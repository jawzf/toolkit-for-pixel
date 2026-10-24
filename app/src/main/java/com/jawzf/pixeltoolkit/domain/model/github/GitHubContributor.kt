package com.jawzf.pixeltoolkit.domain.model.github

import com.google.gson.annotations.SerializedName

data class GitHubContributor(
    @SerializedName("login")
    val login: String,
    @SerializedName("avatar_url")
    val avatarUrl: String,
    @SerializedName("html_url")
    val htmlUrl: String,
    @SerializedName("contributions")
    val contributions: Int = 0,
)
