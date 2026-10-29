package isel.dei.pdm.demos.gamevault.search.platforms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakePlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakePlatformSearchService

/**
 * Activity responsible for displaying the search platforms screen.
 */
class SearchForPlatformActivity : ComponentActivity() {

    private val viewModel: SearchForPlatformViewModel by viewModels {
        SearchForPlatformViewModel.factory(
            searchService = FakePlatformSearchService(),
            repository = FakePlatformCollectionRepository()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}