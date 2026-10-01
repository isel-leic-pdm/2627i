package pt.isel.lei.pdm.nasaapod.service

import pt.isel.lei.pdm.nasaapod.domain.ApodImage

interface NasaApodService {
    suspend fun getTodaysImage(): ApodImage
}