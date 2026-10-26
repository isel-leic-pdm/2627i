package pt.isel.lei.pdm.nasaapod.services

import android.os.Parcelable
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pt.isel.lei.pdm.nasaapod.domain.ApodImage

class RemoteNasaApodService(
    private val http: HttpClient
) : NasaApodService {
    companion object {
        const val BASE_URL = "https://science.nasa.gov/wp-json/wp/v2/apod-basic"
        const val PAGE_QS_PARAMETER = "page"
        const val PER_PAGE_QS_PARAMETER = "per_page"

    }

    //basic?page=1&per_page=1
    override suspend fun getTodaysImage(): ApodImage {
        return getApods(1, 1).first()
    }

    override suspend fun getApods(
        page: Int,
        perPage: Int
    ): List<ApodImage> {
        val resp = http.get(BASE_URL) {
            parameter(PAGE_QS_PARAMETER, page.toString())
            parameter(PER_PAGE_QS_PARAMETER, perPage.toString())
        }

        return resp.body<List<ApodImageDto>>()
            .map { img -> img.toApodImage() }

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