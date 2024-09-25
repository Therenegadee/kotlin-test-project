package tbank.example.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Place(
    val id: Int,
    val title: String,
    val slug: String,
    val address: String,
    @SerialName("site_url")
    val siteUrl: String,
    @SerialName("is_closed")
    val isClosed: Boolean,
    val location: String
)