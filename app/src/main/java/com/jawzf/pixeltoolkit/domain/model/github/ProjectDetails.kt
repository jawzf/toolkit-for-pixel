package com.jawzf.pixeltoolkit.domain.model.github

import com.google.gson.annotations.SerializedName

data class ProjectDetails(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("repo")
    val repo: String = "",
    @SerializedName("stars")
    val stars: Int = 0,
    @SerializedName("downloads")
    val downloads: Int = 0,
    @SerializedName("latestVersion")
    val latestVersion: String? = null,
    @SerializedName("latestReleaseAt")
    val latestReleaseAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
)
