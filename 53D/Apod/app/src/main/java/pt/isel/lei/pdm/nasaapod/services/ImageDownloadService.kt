package pt.isel.lei.pdm.nasaapod.services

import pt.isel.lei.pdm.nasaapod.domain.ApodImage

interface ImageDownloadService {
    fun start(vararg images: ApodImage)
}