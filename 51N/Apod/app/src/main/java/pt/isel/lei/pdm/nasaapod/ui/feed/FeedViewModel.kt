package pt.isel.lei.pdm.nasaapod.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.service.ApodImagesPagingSource
import pt.isel.lei.pdm.nasaapod.service.NasaApodService

sealed interface FeedScreenState {
    object Idle : FeedScreenState
    object Loading : FeedScreenState
    data class Error(val exception: Exception) : FeedScreenState
    data class Content(
        val images: List<ApodImage>,
        val page: Int = 1,
        val perPage: Int = 10,
        val isLoading: Boolean = false
    ) : FeedScreenState

    data class AutoContent(
        val images: Flow<PagingData<ApodImage>>
    ) : FeedScreenState
}

class FeedViewModel(val nasaService: NasaApodService) : ViewModel() {

    val screenState: StateFlow<FeedScreenState>
        field = MutableStateFlow<FeedScreenState>(
            FeedScreenState.Idle
        )


    val images = Pager(
        config = PagingConfig(
            pageSize = 10,
            initialLoadSize = 10,
            prefetchDistance = 3
        ),
        pagingSourceFactory = {
            ApodImagesPagingSource(nasaService)
        }
    ).flow.cachedIn(viewModelScope)

    init {
        //loadImages()
        screenState.value = FeedScreenState.AutoContent(images)
    }

    private fun loadImages() {
        screenState.value = FeedScreenState.Loading

        viewModelScope.launch {
            try {
                screenState.value = FeedScreenState.Content(nasaService.getImages())
            } catch (e: Exception) {
                screenState.value = FeedScreenState.Error(e)
            }
        }

    }

    fun loadMore() {
        viewModelScope.launch {
            try {
                val state = (screenState.value as FeedScreenState.Content)
                screenState.value =
                    FeedScreenState.Content(state.images, page = state.page, isLoading = true)

                val moreImages = nasaService.getImages(state.page + 1, state.perPage)
                screenState.value =
                    FeedScreenState.Content(state.images + moreImages, page = state.page + 1)
            } catch (e: Exception) {
                screenState.value = FeedScreenState.Error(e)
            }
        }
    }
    /*
    private fun loadImages() {
        val f = flow<List<ApodImage>> {
            screenState.value = FeedScreenState.Loading
            try {
                emit(nasaService.getImages())
            } catch (e: Exception) {
                screenState.value = FeedScreenState.Error(e)
            }
        }

        screenState.value = FeedScreenState.Content(f)
    }*/
}