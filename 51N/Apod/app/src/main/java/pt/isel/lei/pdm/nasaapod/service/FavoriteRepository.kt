package pt.isel.lei.pdm.nasaapod.service

import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    val favorite: Flow<String>
    suspend fun setFavorite(date: String)
    suspend fun clearFavorite()
}