package pt.isel.lei.pdm.nasaapod.mock

import kotlinx.coroutines.delay
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.services.NasaApodService

class MockNasaApodService : NasaApodService {
    override suspend fun getTodaysImage(): ApodImage {
        //Thread.sleep(10000)
        delay(10000)

        return MockApodImages.Images.random()
    }
}