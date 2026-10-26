package pt.isel.lei.pdm.nasaapod.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pt.isel.lei.pdm.nasaapod.domain.ApodImage

class RemoteNasaApodService(private val httpClient: HttpClient) : NasaApodService {

    companion object {
        const val BASE_URL = "https://science.nasa.gov/wp-json/wp/v2/apod-basic"
        const val PAGE_QS_ARGUMENT = "page"
        const val PER_PAGE_QS_ARGUMENT = "per_page"
    }

    //https://science.nasa.gov/wp-json/wp/v2/apod-basic?page=1&per_page=1
    override suspend fun getTodaysImage(): ApodImage {
        return getImages(1, 1).first()
    }

    override suspend fun getImages(
        page: Int,
        perPage: Int
    ): List<ApodImage> {
        val resp = httpClient.get(BASE_URL) {
            parameter(PAGE_QS_ARGUMENT, page)
            parameter(PER_PAGE_QS_ARGUMENT, perPage)
        }

        return resp.body<List<ApodImageDto>>().map {
            it.toApodImage()
        }
    }
}


@Serializable
data class ApodImageDto(
    val title: String,
    @SerialName("copyright")
    val author: String,
    @SerialName("explanation")
    val description: String,
    val date: String,
    @SerialName("hdurl")
    val imageUrl: String,
    val url: String,
) {
}

fun ApodImageDto.toApodImage() = ApodImage(
    title = title,
    author = author,
    description = description,
    date = date,
    imageUrl = imageUrl,
    url = url
)

