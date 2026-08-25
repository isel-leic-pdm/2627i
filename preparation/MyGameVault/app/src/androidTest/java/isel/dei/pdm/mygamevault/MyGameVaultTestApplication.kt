package isel.dei.pdm.mygamevault

import android.app.Application
import isel.dei.pdm.mygamevault.ports.CollectionRepository
import isel.dei.pdm.mygamevault.ports.SearchService
import isel.dei.pdm.mygamevault.ports.SecretsRepository
import isel.dei.pdm.mygamevault.adapters.InMemoryCollectionRepository
import isel.dei.pdm.mygamevault.adapters.fakes.FakeSearchService
import isel.dei.pdm.mygamevault.adapters.fakes.FakeSecretsRepository

class MyGameVaultTestApplication : Application(), DependenciesContainer {
    override val searchService: SearchService = FakeSearchService()
    override val secretsRepository: SecretsRepository = FakeSecretsRepository()
    override val collectionRepository: CollectionRepository = InMemoryCollectionRepository()
}
