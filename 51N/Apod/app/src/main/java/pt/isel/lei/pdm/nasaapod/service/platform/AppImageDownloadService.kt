package pt.isel.lei.pdm.nasaapod.service.platform

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.service.ImageDownloadService

class AppImageDownloadService(
    val context: Context
) : ImageDownloadService {


    override fun start(vararg images: ApodImage) {
        AppDownloaderForegroundService.startService(context, *images)
    }
}