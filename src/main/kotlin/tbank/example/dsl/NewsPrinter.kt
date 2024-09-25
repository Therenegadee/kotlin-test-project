package tbank.example.dsl

import tbank.example.dto.News

fun printNews(news: List<News>) {
    val newsInfo = html {
        body {
            h1 { +"Сводка новостей:" }
            ul {
                for (i in news.indices) {
                    val newsPost: News = news.get(i)
                    li {
                        h2 {
                            b { +"Заголовок новости: " }
                            +"\"${newsPost.title}\""
                            p { +"Дата публикации: ${newsPost.publicationDate}" }
                        }
                        p {
                            b { +"Рейтинг новости: " }
                            +"${newsPost.rating}"
                        }
                        p { a(href = newsPost.siteUrl) { +"Ссылка на новость" } }
                        p {
                            b { +"Кол-во лайков: " }
                            +"${newsPost.favoritesCount}"
                        }
                        p {
                            b { +"Кол-во комментариев: " }
                            +"${newsPost.commentsCount}"
                        }
                        p {
                            b { +"Описание новости:" }
                            p { +newsPost.description }
                        }
                    }

                }
            }
        }
    }
    println(newsInfo)
}