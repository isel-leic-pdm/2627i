package pt.isel.lei.pdm.nasaapod.services

import kotlinx.coroutines.flow.Flow
import pt.isel.lei.pdm.nasaapod.domain.ApodImage

interface BookmarksRepository {
    val bookmarks: Flow<List<ApodImage>>
    fun isBookmarked(image: ApodImage): Flow<Boolean>
    suspend fun add(image: ApodImage)
    suspend fun remove(image: ApodImage)
}