package pt.isel.lei.pdm.nasaapod.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.domain.database.ApodImageBookmarkDao
import pt.isel.lei.pdm.nasaapod.domain.database.ApodImageEntity
import pt.isel.lei.pdm.nasaapod.service.toApodImage

class RoomBookmarksRepository(
    val bookmarkDao: ApodImageBookmarkDao
) : BookmarksRepository {
    override val bookmarks: Flow<List<ApodImage>> = bookmarkDao.getAll().map {
        it.map { it.toApodImage() }
    }
    override val pendingDownloads: Flow<List<ApodImage>> = bookmarkDao.getPendingDownloads().map {
        it.map { it.toApodImage() }
    }

    override fun isBookmarked(image: ApodImage): Flow<Boolean> {
        return bookmarkDao.isBookmarked(image.date)
    }

    override suspend fun add(image: ApodImage) {
        bookmarkDao.insert(image.toEntity())
    }

    override suspend fun remove(image: ApodImage) {
        bookmarkDao.delete(image.toEntity())
    }

    override suspend fun markAsDownloaded(image: ApodImage) {
        bookmarkDao.update(image.toEntity().copy(downloaded = true))
    }
}

fun ApodImage.toEntity(): ApodImageEntity {
    return ApodImageEntity(
        title = title,
        author = author,
        description = description,
        date = date,
        imageUrl = imageUrl,
        url = url,
        downloaded = false
    )
}

fun ApodImageEntity.toApodImage(): ApodImage {
    return ApodImage(
        title = title,
        author = author,
        description = description,
        date = date,
        imageUrl = imageUrl,
        url = url
    )
}