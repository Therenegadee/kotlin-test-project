package tbank.example.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tbank.example.serializer.LocalDateSerializer
import java.time.LocalDate

@Serializable
data class News(
    val id: Int,
    val title: String,
    @Serializable
    var place: Place?,
    val description: String,
    @SerialName("site_url")
    val siteUrl: String,
    @SerialName("favorites_count")
    val favoritesCount: Int,
    @SerialName("comments_count")
    val commentsCount: Int,
    @Serializable(with = LocalDateSerializer::class)
    @SerialName("publication_date")
    val publicationDate: LocalDate,
) {
    val rating: Double by lazy { 1.0 / (1 + Math.exp(-(favoritesCount / (commentsCount + 1).toDouble()))) }
}

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