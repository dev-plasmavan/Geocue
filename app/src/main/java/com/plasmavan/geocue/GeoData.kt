package com.plasmavan.geocue

import kotlinx.serialization.Serializable

@Serializable
data class GeoData(
    val id: Int,
    val name: String,
    val url: String,
    val map: String
)