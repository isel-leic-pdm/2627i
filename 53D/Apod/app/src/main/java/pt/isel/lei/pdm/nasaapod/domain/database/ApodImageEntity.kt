package pt.isel.lei.pdm.nasaapod.domain.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "apod_images")
data class ApodImageEntity(
    val title: String,
    val author: String,
    val description: String,
    @PrimaryKey
    val date: String,
    val imageUrl: String,
    val url: String,
)


