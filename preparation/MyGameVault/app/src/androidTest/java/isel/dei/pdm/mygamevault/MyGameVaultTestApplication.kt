package isel.dei.pdm.mygamevault

import android.app.Application
import isel.dei.pdm.mygamevault.core.SearchService
import isel.dei.pdm.mygamevault.core.SecretsRepository
import isel.dei.pdm.mygamevault.infrastructure.FakeSearchService
import isel.dei.pdm.mygamevault.infrastructure.FakeSecretsRepository

class MyGameVaultTestApplication : Application(), DependenciesContainer {
    override val searchService: SearchService = FakeSearchService()
    override val secretsRepository: SecretsRepository = FakeSecretsRepository()
}
