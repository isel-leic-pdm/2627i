package pt.isel.lei.pdm.nasaapod.mock

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.service.BookmarksRepository

class MockBookmarksRepository : BookmarksRepository {

    private val bookmarkList = mutableListOf<ApodImage>()

    override val bookmarks: Flow<List<ApodImage>>
        field = MutableStateFlow(emptyList())

    override fun isBookmarked(image: ApodImage): Flow<Boolean> =
        bookmarks.map { it.contains(image) }

    override suspend fun add(image: ApodImage) {
        bookmarkList.add(image)
        bookmarks.emit(bookmarkList.toList())
    }

    override suspend fun remove(image: ApodImage) {
        bookmarkList.remove(image)
        bookmarks.emit(bookmarkList.toList())
    }
}