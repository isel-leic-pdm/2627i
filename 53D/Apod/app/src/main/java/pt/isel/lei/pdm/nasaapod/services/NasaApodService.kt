package pt.isel.lei.pdm.nasaapod.services

import pt.isel.lei.pdm.nasaapod.domain.ApodImage

interface NasaApodService {
    suspend fun getTodaysImage(): ApodImage
    suspend fun getApods(page: Int = 1, perPage: Int = 10) : List<ApodImage>
}