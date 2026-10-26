package pt.isel.lei.pdm.nasaapod.mock

import kotlinx.coroutines.delay
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.service.NasaApodService
import kotlin.random.Random

class MockNasaApodService : NasaApodService {
    override suspend fun getTodaysImage(): ApodImage {
        //delay(2000)

        //if (Random.nextInt(10) <= 1)
        //  throw Exception("Something went wrong")

        return MockApodImages.Images.random()
    }

    override suspend fun getImages(page: Int, perPage: Int): List<ApodImage> {
        return MockApodImages.Images
    }
}