package tbank.example.service

import org.jetbrains.annotations.Nullable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tbank.example.dto.News
import java.io.File
import java.time.LocalDate

var log: Logger = LoggerFactory.getLogger("NewsService")

suspend fun main() {
    val startPeriod: LocalDate = LocalDate.now().minusYears(10)
    val endPeriod: LocalDate = LocalDate.now()
    val period: ClosedRange<LocalDate> = startPeriod..endPeriod
    val news: List<News>? = fetchMostRatedNews(100, period)
    val file: File? = news?.let { convertPojoToCsvFile(it, "news-kudago-${period}") }
}

@Nullable
suspend fun fetchMostRatedNews(count: Int, period: ClosedRange<LocalDate>): List<News>? {
    log.info("Начало получения новостей из сервиса KudaGo, отсортированных по рейтингу. Входные параметры: кол-во новостей - ${count}, период - ${period}.")
    try {
        var news: List<News> = fetchNewsData(count)
            .filter { newsPost -> period.contains(newsPost.publicationDate) }
            .toList()
        news = sortNewsByRatingSequentially(news)
        log.info("Данные о новостях за период ${period} были успешно получены и отсортированы по рейтингу.")
        return news
    } catch (e: Exception) {
        log.error("Не удалось извлечь новости из API KudaGo. Прчина: ${e.message}.\nStackTrace: ${e.stackTrace}")
        return null
    }
}

fun sortNewsByRatingSequentially(news: List<News>): List<News> {
    log.debug("Начало сортировки новостей по рейтингу с помощью sequence.")
    val sortedNews: List<News> = news.asSequence()
        .sortedByDescending { newsPost -> newsPost.rating }
        .toList()
    log.debug("Список новостей был успешно отсортирован по рейтингу с помощью sequence. Самый высокий рейтинг новости: ${sortedNews.first().rating}. Самый низкий рейтинг: ${sortedNews.last().rating}.")
    return sortedNews
}

fun sortNewsByRatingInLoop(news: List<News>): List<News> {
    log.debug("Начало сортировки новостей по рейтингу с помощью цикла for.")
    val sortedNews: MutableList<News> = mutableListOf<News>();
    sortedNews.addAll(news)
    for (i in sortedNews.indices) {
        for (j in 0 until sortedNews.size - 1 - i) {
            if (sortedNews[j].rating < sortedNews[j + 1].rating) {
                val temp = sortedNews[j]
                sortedNews[j] = sortedNews[j + 1]
                sortedNews[j + 1] = temp
            }
        }
    }
    log.debug("Список новостей был успешно отсортирован по рейтингу с помощью цикла for. Самый высокий рейтинг новости: ${sortedNews.first().rating}. Самый низкий рейтинг: ${sortedNews.last().rating}.")
    return sortedNews
}