package tbank.example.service

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory
import tbank.example.dto.News
import kotlin.jvm.Throws

const val kudaGoApiUrl: String = "https://kudago.com/public-api/v1.4/news"
private val log = LoggerFactory.getLogger("KudaGoApiClient")

@Throws(SerializationException::class, IllegalArgumentException::class)
suspend fun fetchNewsData(newsCount: Int): List<News> {
    log.debug("Начало получения новостей из сервиса KudaGo. Входной пользовательский параметр кол-во новостей: ${newsCount}.")
    val client = HttpClient(CIO) {
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    log.debug("Кол-во новостей, которое будет извлечено: {}.", newsCount)
    try {
        val response: HttpResponse = client.get(kudaGoApiUrl) {
            parameter("lang", "ru")
            parameter("location", "spb")
//        parameter("order_by", "publication_date") // параметр по умолчанию
            parameter("page_size", newsCount)
            parameter("page", 1)
            parameter("fields", "id,title,place,description,site_url,favorites_count,comments_count,publication_date")
            parameter("expand", "place")
        }

        if (!response.status.isSuccess()) {
            val errorMessage: String =
                "Произошла ошибка при попытке извлечения новостей из API KudaGo. Ожидаемый ответ: ${HttpStatusCode.OK}." +
                        "Полученный ответ: ${response.status}. Ответ от сервиса: ${response.bodyAsText()}."
            log.error(errorMessage)
            throw IllegalArgumentException(errorMessage)
        }
        val json = Json {
            ignoreUnknownKeys = true
        }
        val responseBody: JsonObject = json.decodeFromString(response.bodyAsText())
        val news: JsonArray = responseBody.get("results")
            ?.jsonArray
            ?: JsonArray(emptyList())
        log.debug("Кол-во полученных новостей: ${news.size}.")
        return news.map<JsonElement, News> { element -> json.decodeFromJsonElement(element) }
            .toList()
    } catch (e: SerializationException) {
        log.error("Произошла ошибка при попытке десериализации ответа от API KudaGo. Причина: ${e.message}.\nStackTrace: ${e.stackTrace}")
        throw e
    } catch (e : Exception) {
        log.error("Произошла ошибка при попытке извлечения новостей из API KudaGo. Причина: ${e.message}.\nStackTrace: ${e.stackTrace}")
        throw e
    }
}