package isel.dei.pdm.mygamevault

import android.app.Application
import isel.dei.pdm.mygamevault.ports.CollectionRepository
import isel.dei.pdm.mygamevault.ports.SearchService
import isel.dei.pdm.mygamevault.ports.SecretsRepository
import isel.dei.pdm.mygamevault.adapters.InMemoryCollectionRepository
import isel.dei.pdm.mygamevault.adapters.fakes.FakeSearchService
import isel.dei.pdm.mygamevault.adapters.fakes.FakeSecretsRepository
import isel.dei.pdm.mygamevault.ui.common.ImageLoader
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap

class MyGameVaultTestApplication : Application(), DependenciesContainer {
    override val searchService: SearchService = FakeSearchService()
    override val secretsRepository: SecretsRepository = FakeSecretsRepository()
    override val collectionRepository: CollectionRepository = InMemoryCollectionRepository()
    override val imageLoader: ImageLoader = {
        Result.success(createBitmap(1, 1).asImageBitmap())
    }
}
