package tbank.example

import io.ktor.client.call.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import org.slf4j.LoggerFactory
import tbank.example.dto.News
import tbank.example.service.convertNewsToCsvFile
import tbank.example.service.fetchNewsData
import java.io.File
import kotlin.system.measureTimeMillis

private val log = LoggerFactory.getLogger("Main")

suspend fun main_sync() {
    val execTime = measureTimeMillis {
        val news = fetchNewsData(400)
        val fileName = "collection_of_news_sync.csv"
        val filePath = "./src/main/resources/hw4/$fileName"
        val file = File(filePath)
        if (!file.exists()) {
            withContext(Dispatchers.IO) {
                file.createNewFile()
            }
        }
        convertNewsToCsvFile(news, file)
    }
    log.debug("Новости были успешно записаны!")
    log.info("Время выполнения метода: $execTime ms")
}

suspend fun main() {
    val threadsCount = 10
    val threadsName = "fx-thrd-exctr"
    val executor = newFixedThreadPoolContext(threadsCount, threadsName)
    val channel = Channel<List<News>>()
    val scope = CoroutineScope(executor)

    val newsFetchTasks = (1..threadsCount).map { i ->
        scope.launch {
            try {
                for (page in 1..20) {
                    if (page % threadsCount == i - 1) {
                        val news = fetchNewsData(newsCount = 20, page = page)
                        if (news.isNotEmpty()) {
                            channel.send(news)
                        }
                    }
                }
            } catch (e: NoTransformationFoundException) {
                log.error("Произошла ошибка в ходе выполнения задачи. Причина: ${e.message}.\nStackTrace: ${e.stackTrace}")
            }
        }
    }

    val writeToCsvTask = scope.launch(executor) {
        processor(channel)
    }

    val execTime = measureTimeMillis {
        runBlocking {
            newsFetchTasks.forEach { it.join() }
            channel.close()
            writeToCsvTask.join()
        }
    }

    log.debug("Новости были успешно записаны!")
    log.info("Время выполнения метода: $execTime ms")
}

suspend fun processor(channel: ReceiveChannel<List<News>>) {
    val fileName = "collection_of_news_async.csv"
    val filePath = "./src/main/resources/hw4/$fileName"

    val file = File(filePath)
    if (!file.exists()) {
        withContext(Dispatchers.IO) {
            file.createNewFile()
        }
    }

    for (news in channel) {
        convertNewsToCsvFile(news, file)
    }
}