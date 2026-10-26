package pt.isel.lei.pdm.nasaapod.services

import androidx.paging.PagingSource
import androidx.paging.PagingState
import pt.isel.lei.pdm.nasaapod.domain.ApodImage


class ApodImagesPagingSource(
    val service: NasaApodService
) : PagingSource<Int, ApodImage>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ApodImage> {
        return try {
            val page = params.key ?: 1
            val images = service.getApods(perPage = params.loadSize, page = page)

            return LoadResult.Page(
                data = images,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (images.isEmpty()) null else page + 1
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ApodImage>): Int? {
        //returns the key for the current page by seeing using either the prev or next key
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(position)?.nextKey?.minus(1)
        }
    }

}