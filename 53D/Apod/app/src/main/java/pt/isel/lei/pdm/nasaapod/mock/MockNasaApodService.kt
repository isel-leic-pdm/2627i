package pt.isel.lei.pdm.nasaapod.mock

import kotlinx.coroutines.delay
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.services.NasaApodService
import kotlin.random.Random

class MockNasaApodService : NasaApodService {
    override suspend fun getTodaysImage(): ApodImage {
        //Thread.sleep(10000)
        delay(2000)

        if (Random.nextInt(100) < 10)
            throw Exception("Something went wrong")
        return MockApodImages.Images.random()
    }

    override suspend fun getApods(page: Int, perPage: Int): List<ApodImage> {
        return MockApodImages.Images
    }
}