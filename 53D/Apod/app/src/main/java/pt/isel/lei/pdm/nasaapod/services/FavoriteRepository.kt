package pt.isel.lei.pdm.nasaapod.services

import kotlinx.coroutines.flow.Flow
import pt.isel.lei.pdm.nasaapod.domain.ApodImage

interface FavoriteRepository {
    val favorite: Flow<String>
    suspend fun setFavorite(date: String)
    suspend fun clearFavorite()
}