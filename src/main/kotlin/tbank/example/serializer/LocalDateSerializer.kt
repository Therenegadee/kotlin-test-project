package tbank.example.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Serializer(forClass = LocalDate::class)
object LocalDateSerializer : KSerializer<LocalDate> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override val descriptor = buildClassSerialDescriptor("LocalDate")

    override fun serialize(encoder: Encoder, value: LocalDate) {
        val unixTimestamp = value.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        encoder.encodeLong(unixTimestamp)
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        val unixTimestamp = decoder.decodeLong()
        return Instant.ofEpochSecond(unixTimestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    }
}