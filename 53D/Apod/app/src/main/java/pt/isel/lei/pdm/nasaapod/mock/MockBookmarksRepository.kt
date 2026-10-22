package pt.isel.lei.pdm.nasaapod.mock

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.services.BookmarksRepository

class MockBookmarksRepository : BookmarksRepository {

    private val bookmarksList = mutableListOf<ApodImage>()

    override val bookmarks: Flow<List<ApodImage>>
        field = MutableStateFlow(emptyList())

    override fun isBookmarked(image: ApodImage): Flow<Boolean> =
        bookmarks.map {
            it.contains(image)
        }


    override suspend fun add(image: ApodImage) {
        bookmarksList.add(image)
        bookmarks.emit(bookmarksList.toList())
    }

    override suspend fun remove(image: ApodImage) {
        bookmarksList.remove(image)
        bookmarks.emit(bookmarksList.toList())
    }

    suspend fun xpto() {
        bookmarks.collect {
            println("Collecting $it")
        }
    }
}