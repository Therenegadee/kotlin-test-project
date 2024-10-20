package tbank.example.service

import com.fasterxml.jackson.databind.MapperFeature
import java.io.File
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tbank.example.dto.News
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

private val log: Logger = LoggerFactory.getLogger("CsvConverter")

@Throws(IOException::class)
fun convertNewsToCsvFile(news: Collection<News>, fileName: String?): File {
    val filepath: String = "./src/main/resources/hw4/${fileName ?: "collection-of-news-${UUID.randomUUID()}"}.csv"
    val file: File = File(filepath)
    if (file.exists()) {
        val errorMessage: String = "Файл с именем $fileName уже существует по пути: $filepath"
        log.error(errorMessage)
        throw IllegalArgumentException(errorMessage)
    }
    return convertNewsToCsvFile(news, file)
}

@Throws(IOException::class)
fun convertNewsToCsvFile(news: Collection<News>, file: File): File {
    log.debug("Начало конвертации списка Новостей в CSV файл.")

    val csvMapper: CsvMapper = CsvMapper().apply {
        enable(CsvParser.Feature.TRIM_SPACES)
        enable(CsvParser.Feature.SKIP_EMPTY_LINES)
        disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        registerModule(JavaTimeModule())
    }

    log.debug("Путь к файлу, в который будут записаны новости: ${file.absoluteFile}.")

    val schema: CsvSchema = CsvSchema.builder()
        .addColumn("Идентификатор")
        .addColumn("Заголовок")
        .addColumn("Описание")
        .addColumn("Дата публикации")
        .addColumn("Рейтинг")
        .addColumn("Кол-во лайков")
        .addColumn("Кол-во комментариев")
        .addColumn("Название места")
        .addColumn("Адрес места")
        .addColumn("Локация места")
        .addColumn("Ссылка")
        .build();
    try {
        FileWriter(file, true).use { writer ->
            if (file.length() == 0L) {
                csvMapper.writer(schema)
                    .writeValues(writer)
                    .writeAll(emptyList<List<Any>>())
            }
            csvMapper.writer(schema.withHeader())
                .writeValues(writer)
                .writeAll(news.map { newsPost ->
                    listOf(
                        newsPost.id,
                        newsPost.title,
                        newsPost.description,
                        newsPost.publicationDate,
                        newsPost.rating,
                        newsPost.favoritesCount,
                        newsPost.commentsCount,
                        newsPost.place?.title ?: "",
                        newsPost.place?.address ?: "",
                        newsPost.place?.location ?: "",
                        newsPost.siteUrl
                    )
                })
                .close()
        }
    } catch (e: IOException) {
        log.error("В процессе конвертации списка новостей произошла ошибка. Причина: ${e.message}.\nStackTrace: ${e.stackTrace}.")
        throw e
    }
    log.debug("Список новостей была успешно сконвертирована в CSV файл по пути: ${file.absoluteFile}")
    return file
}

// не получилось сделать типизированный метод конвертации классов в csv, т.к. либа не поддерживает nested objects конвертацию
// поэтому сделал отдельный метод строго под News
@Throws(IOException::class)
inline fun <reified T> convertPojoToCsvFile(data: Collection<T>, fileName: String?): File {
    val log: Logger = LoggerFactory.getLogger("CsvInlineConverter")
    log.debug("Начало конвертации списка объектов класса ${T::class.simpleName} в CSV файл.")

    val csvMapper: CsvMapper = CsvMapper().apply {
        enable(CsvParser.Feature.TRIM_SPACES)
        enable(CsvParser.Feature.SKIP_EMPTY_LINES)
        disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        registerModule(JavaTimeModule())
    }

    val filepath: String =
        "./src/main/resources/hw4/${fileName ?: "collection-of-${T::class.simpleName}-${UUID.randomUUID()}"}.csv"
    val file: File = File(filepath)

    if (file.exists()) {
        val errorMessage: String = "Файл с именем $fileName уже существует по пути: $filepath"
        log.error(errorMessage)
        throw IllegalArgumentException(errorMessage)
    }


    log.debug("Путь к файлу, в который будут записаны объекты: $filepath.")
    try {
        FileWriter(file).use { writer ->
            csvMapper.writer(csvMapper.typedSchemaFor(T::class.java).withHeader())
                .writeValues(writer)
                .writeAll(data)
                .close()
        }
    } catch (e: IOException) {
        log.error("В процессе конвертации коллекции объектов класса ${T::class} произошла ошибка. Причина: ${e.message}.\nStackTrace: ${e.stackTrace}.")
        throw e
    }
    log.debug("Коллекция данных класса ${T::class.simpleName} была успешно сконвертирована в CSV файл по пути: $filepath")
    return file
}