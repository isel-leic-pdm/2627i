package pt.isel.lei.pdm.nasaapod.domain

import android.os.Parcelable
import androidx.room3.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
data class ApodImage(
    val title: String,
    val author: String,
    val description: String,
    val date: String,
    val imageUrl: String,
    val url: String,
) : Parcelable {
}