package pt.isel.lei.pdm.nasaapod.services.platform


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import pt.isel.lei.pdm.nasaapod.R
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.ui.details.DetailsActivity

class AppNotificationManager(
    private val ctx: Context,
    private val httpClient: HttpClient
) {
    companion object {
        const val FS_NOTIFICATION_ID = 0xc0ffe
        private const val FS_CHANNEL_ID = "apod_downloads"


        private const val APOD_NOTIFICATION_ID = 0xbeef
        private const val APOD_CHANNEL_ID = "apod_daily"

    }


    private val notificationManager by lazy {
        ctx.getSystemService(NotificationManager::class.java)
    }

    fun createForegroundServiceNotificationChannel() {
        createNotificationChannel(
            FS_CHANNEL_ID,
            ctx.getString(R.string.not_apod_downloads),
            ctx.getString(R.string.not_apod_downloads_desc)
        )
    }

    fun createForegroundServiceNotification(
        message: String,
        progress: Int,
        total: Int,
        ongoing: Boolean = true
    ): Notification {
        return createNotification(
            message,
            ctx.getString(R.string.not_apod_downloads),
            progress,
            total,
            ongoing,
            FS_CHANNEL_ID
        )
    }

    fun updateForegroundServiceNotification(
        message: String,
        progress: Int,
        total: Int,
        ongoing: Boolean = true
    ) {
        updateNotification(
            message,
            ctx.getString(R.string.not_apod_downloads),
            progress,
            total,
            FS_NOTIFICATION_ID,
            FS_CHANNEL_ID,
            ongoing
        )
    }


    suspend fun showApodNotification(img: ApodImage) {
        createNotificationChannel(
            APOD_CHANNEL_ID,
            ctx.getString(R.string.not_apod_daily),
            ctx.getString(R.string.not_apod_daily_desc)
        )

        val pendingIntent = PendingIntent.getActivity(
            ctx,
            0,
            DetailsActivity.getIntent(ctx, img),
            PendingIntent.FLAG_IMMUTABLE
        )

        val bitmap = downloadBitmap(httpClient, img.imageUrl)

        val notificationBuilder = NotificationCompat.Builder(ctx, APOD_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(ctx.getString(R.string.not_apod_daily))
            .setContentText(img.title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if ( bitmap == null)
            notificationBuilder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(ctx.getString(R.string.not_apod_format, img.title))
            )
        else
            notificationBuilder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .bigLargeIcon(null as Bitmap?)
            )


        notificationManager.notify(
            APOD_NOTIFICATION_ID,
            notificationBuilder.build()
        )

    }

    suspend fun downloadBitmap(
        client: HttpClient,
        imageUrl: String
    ): Bitmap? {
        return try {
            val bytes: ByteArray = client.get(imageUrl).body()
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

  private fun createNotificationChannel(
        channelID: String,
        name: String,
        desc: String,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelID,
                name,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = desc
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(
        message: String,
        title: String,
        progress: Int,
        total: Int,
        ongoing: Boolean = true,
        channelID: String,

        ): Notification {
        val builder = NotificationCompat.Builder(
            ctx,
            channelID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setOnlyAlertOnce(true)
            .setOngoing(ongoing)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)

        if (ongoing)
            if (total > 0) {
                builder.setProgress(total, progress, false)
            } else {
                builder.setProgress(0, 0, true)
            }

        return builder.build()
    }

    private fun updateNotification(
        message: String,
        title: String,
        progress: Int,
        total: Int,
        notificationId: Int,
        channelID: String,
        ongoing: Boolean = true
    ) {
        notificationManager.notify(
            notificationId,
            createNotification(message, title, progress, total, ongoing, channelID)
        )
    }
}
