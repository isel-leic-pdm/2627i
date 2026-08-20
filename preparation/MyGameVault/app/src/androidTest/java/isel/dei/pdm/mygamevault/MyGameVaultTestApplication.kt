package isel.dei.pdm.mygamevault

import android.app.Application
import isel.dei.pdm.mygamevault.core.SearchService
import isel.dei.pdm.mygamevault.infrastructure.FakeSearchService

class MyGameVaultTestApplication : Application(), DependenciesContainer {
    override val searchService: SearchService = FakeSearchService()
}
