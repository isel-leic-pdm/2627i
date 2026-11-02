package pt.isel.lei.pdm.nasaapod.service.platform

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import pt.isel.lei.pdm.nasaapod.BuildConfig
import pt.isel.lei.pdm.nasaapod.DependencyContainer
import pt.isel.lei.pdm.nasaapod.NasaApodApplication
import java.time.LocalDate

class AppDailyNotifierWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val service = (applicationContext as DependencyContainer).nasaService
        val notificationManager = (applicationContext as NasaApodApplication).appNotificationManager

        var apod = service.getTodaysImage()

        if (BuildConfig.DEBUG || apod.date != LocalDate.now().toString()) {
            notificationManager.showApodNotification(apod)
        }
        return Result.success()
    }
}