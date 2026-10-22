package pt.isel.lei.pdm.nasaapod.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.domain.database.ApodImageBookmarkDao
import pt.isel.lei.pdm.nasaapod.domain.database.ApodImageEntity

class RoomBookmarkRepository(
    val dao: ApodImageBookmarkDao
) : BookmarksRepository {
    override val bookmarks: Flow<List<ApodImage>> = dao.getAll().map {
        it.map { it.toApodImage() }
    }

    override fun isBookmarked(image: ApodImage): Flow<Boolean> {
        return dao.isBookmarked(image.date)
    }

    override suspend fun add(image: ApodImage) {
        dao.insert(image.toEntity())
    }

    override suspend fun remove(image: ApodImage) {
        dao.delete(image.toEntity())
    }
}

fun ApodImage.toEntity() = ApodImageEntity(
    title = title,
    author = author,
    description = description,
    date = date,
    imageUrl = imageUrl,
    url = url
)

fun ApodImageEntity.toApodImage() = ApodImage(
    title = title,
    author = author,
    description = description,
    date = date,
    imageUrl = imageUrl,
    url = url
)