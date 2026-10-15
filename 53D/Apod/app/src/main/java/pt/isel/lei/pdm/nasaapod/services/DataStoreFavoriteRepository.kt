package pt.isel.lei.pdm.nasaapod.services

import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreFavoriteRepository(private val dataStore: DataStore<Preferences>) :
    FavoriteRepository {

    val FAV_KEY = stringPreferencesKey("myfav")

    override val favorite: Flow<String> = dataStore.data.map { preferences ->
        preferences[FAV_KEY] ?: ""
    }

    override suspend fun setFavorite(date: String) {
        dataStore.edit {
            it[FAV_KEY] = date
        }
    }

    override suspend fun clearFavorite() {
        dataStore.edit {
            it.remove(FAV_KEY)
        }
    }
}