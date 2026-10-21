package isel.dei.pdm.demos.gamevault.search.platforms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import isel.dei.pdm.demos.gamevault.domain.collection.CollectionRepository
import isel.dei.pdm.demos.gamevault.domain.search.ports.PlatformSearchService

/**
 * The view model for the search platforms screen. The view model is responsible for managing the
 * state of the screen and for interacting with the remaining parts of the application.
 *
 * The screen supports searching for existing platforms and adding them to the user's collection.
 *
 * @param searchService The service used to search for platforms.
 * @param collectionRepo The repository used to store the user's collection of platforms.
 */
class SearchForPlatformViewModel(
    private val searchService: PlatformSearchService,
    private val collectionRepo: CollectionRepository,
) : ViewModel() {

    companion object {
        /**
         * Factory for creating instances of [SearchForPlatformViewModel] with the given dependencies.
         */
        fun factory(
            searchService: PlatformSearchService,
            repository: CollectionRepository
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SearchForPlatformViewModel(searchService, repository) as T
            }
        }
    }

}