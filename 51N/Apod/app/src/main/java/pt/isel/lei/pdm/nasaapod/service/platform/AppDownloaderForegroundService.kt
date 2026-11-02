package pt.isel.lei.pdm.nasaapod.service.platform

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.isel.lei.pdm.nasaapod.DependencyContainer
import pt.isel.lei.pdm.nasaapod.NasaApodApplication
import pt.isel.lei.pdm.nasaapod.R
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.service.BookmarksRepository
import java.io.File
import java.net.HttpURLConnection
import java.net.URL


class AppDownloaderForegroundService : Service() {

    companion object {
        private const val EXTRA_IMAGES = "EXTRA_IMAGES"

        fun startService(ctx: Context, vararg images: ApodImage) {
            val intent = Intent(
                ctx,
                AppDownloaderForegroundService::class.java
            )


            intent.putParcelableArrayListExtra(
                EXTRA_IMAGES,
                ArrayList(images.toList())
            )

            ContextCompat.startForegroundService(
                ctx,
                intent
            )
        }
    }
    private lateinit var repository: BookmarksRepository

    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var downloadJob: Job? = null

    private val notificationManager by lazy {
        (application as NasaApodApplication).appNotificationManager
    }


    override fun onCreate() {
        super.onCreate()

        repository = (application as DependencyContainer).bookmarksRepository

        notificationManager.createForegroundServiceNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        // Must enter foreground promptly.
        startForeground(
            AppNotificationManager.FS_NOTIFICATION_ID,
            notificationManager.createForegroundServiceNotification(
                getString(R.string.fs_prep),
                0,
                0
            )
        )

        // Avoid starting two batches simultaneously.
        if (downloadJob?.isActive == true) {
            return START_NOT_STICKY
        }

        downloadJob = serviceScope.launch {
            try {

                val images = intent?.getParcelableArrayListExtra<ApodImage>(EXTRA_IMAGES)

                downloadPendingBookmarks(images)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                notificationManager.updateForegroundServiceNotification(
                    getString(
                        R.string.fs_download_failed,
                        e.message ?: getString(R.string.app_unknown_error)
                    ),
                    0,
                    0,
                    ongoing = false
                )
            } finally {
                //stopForeground(STOP_FOREGROUND_REMOVE)
                stopForeground(STOP_FOREGROUND_DETACH)
                stopSelfResult(startId)
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun downloadPendingBookmarks(images: ArrayList<ApodImage>?) {

        var pending: List<ApodImage>

        if (images != null && images.count() > 0) {
            Log.d("ApodDownloadForegroundService", "Processing ${images.count()} intent images")
            pending = images
        } else {
            // Take one snapshot so failed downloads aren't retried indefinitely.
            pending = repository.pendingDownloads.first()
            Log.d(
                "ApodDownloadForegroundService",
                "Processing ${pending.count()} repository images"
            )
        }

        if (pending.isEmpty()) {
            notificationManager.updateForegroundServiceNotification(
                getString(R.string.fs_all_downloaded),
                0,
                0,
                ongoing = false
            )
            return
        }

        var completed = 0
        var failed = 0

        pending.forEachIndexed { index, image ->
            try {
                val localPath = downloadImage(image)

                //to see the notification
                delay(1000)

                // Only mark the image after its file has been saved.
                repository.markAsDownloaded(image)

                completed++
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                failed++
            }


            notificationManager.updateForegroundServiceNotification(
                message = getString(R.string.fs_processed_format, index + 1, pending.size) +
                        if (failed > 0) " ${getString(R.string.fs_failed_format)}" else "",
                progress = index + 1,
                total = pending.size,
                ongoing = index + 1 < pending.size
            )
        }

        val message = when {
            failed == 0 ->
                getString(R.string.fs_downloaded_format, completed)
            else ->
                getString(R.string.fs_some_failed_format, completed, failed)
        }

        notificationManager.updateForegroundServiceNotification(
            message = message,
            progress = pending.size,
            total = pending.size,
            ongoing = false
        )
    }

    private suspend fun downloadImage(
        image: ApodImage
    ): String = withContext(Dispatchers.IO) {
        val directory = File(filesDir, "apod-images")

        if (!directory.exists() && !directory.mkdirs()) {
            error("Could not create image directory")
        }

        val destination = File(directory, "${image.date}.jpg")
        val temporary = File(directory, "${image.date}.tmp")

        val connection = (URL(image.imageUrl)
            .openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            requestMethod = "GET"
        }

        try {
            connection.connect()

            check(connection.responseCode in 200..299) {
                "HTTP error ${connection.responseCode}"
            }

            connection.inputStream.use { input ->
                temporary.outputStream().buffered().use { output ->
                    input.copyTo(output)
                }
            }

            check(temporary.length() > 0L) {
                "Downloaded file is empty"
            }

            if (destination.exists() && !destination.delete()) {
                error("Could not replace existing image")
            }

            check(temporary.renameTo(destination)) {
                "Could not finalize downloaded image"
            }

            destination.absolutePath
        } catch (e: Exception) {
            temporary.delete()
            throw e
        } finally {
            connection.disconnect()
        }
    }


    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }


}