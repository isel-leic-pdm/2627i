package pt.isel.lei.pdm.nasaapod.ui.infiniteapods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.services.ApodImagesPagingSource
import pt.isel.lei.pdm.nasaapod.services.NasaApodService

sealed interface InfiniteApodsScreenState {
    companion object {
        const val PER_PAGE_IMAGES = 10
    }

    object Idle : InfiniteApodsScreenState
    class Error(val error: Exception) : InfiniteApodsScreenState
    class Content(
        val images: List<ApodImage>,
        val isLoading: Boolean,
        val page: Int = 1,
        val perPage: Int = PER_PAGE_IMAGES
    ) :
        InfiniteApodsScreenState

    class AutoContent(
        val images: Flow<PagingData<ApodImage>>
    ) :
        InfiniteApodsScreenState
}

class InfiniteApodsViewModel(
    private val service: NasaApodService
) : ViewModel() {


    val screenState: StateFlow<InfiniteApodsScreenState>
        field = MutableStateFlow<InfiniteApodsScreenState>(InfiniteApodsScreenState.Idle)

    private val imagesPager = Pager(
        config = PagingConfig(
            pageSize = 10,
            initialLoadSize = 10,
            prefetchDistance = 3
        ),
        pagingSourceFactory = {
            ApodImagesPagingSource(service)
        }
    ).flow.cachedIn(viewModelScope)


    init {
        initialRequestManual()
        screenState.value = InfiniteApodsScreenState.AutoContent(imagesPager)
    }

    private fun initialRequestManual() {
        viewModelScope.launch {
            screenState.value = InfiniteApodsScreenState.Content(emptyList(), true)
            try {
                screenState.value = InfiniteApodsScreenState.Content(
                    service.getApods(perPage = InfiniteApodsScreenState.PER_PAGE_IMAGES),
                    false
                )
            } catch (e: Exception) {
                screenState.value = InfiniteApodsScreenState.Error(e)
            }
        }
    }

    fun loadmore() {
        val state = screenState.value as InfiniteApodsScreenState.Content

        if (state == null)
            return

        if (state.isLoading)
            return

        screenState.value = InfiniteApodsScreenState.Content(state.images, true)

        viewModelScope.launch {
            try {
                val page = state.page + 1
                val perPage = state.perPage
                val images = service.getApods(page = page, perPage = perPage)

                screenState.value = InfiniteApodsScreenState.Content(
                    state.images + images,
                    false,
                    page,
                    perPage
                )

            } catch (e: Exception) {
                screenState.value = InfiniteApodsScreenState.Error(e)
            }
        }
    }
}