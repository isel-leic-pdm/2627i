package pt.isel.lei.pdm.nasaapod.mock

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pt.isel.lei.pdm.nasaapod.service.FavoriteRepository

class MockFavoriteRepository : FavoriteRepository {
    override val favorite: Flow<String>
        field = MutableStateFlow("")

    override suspend fun setFavorite(date: String) {
        favorite.emit(date)
    }

    override suspend fun clearFavorite() {
        favorite.value = ""
    }
}