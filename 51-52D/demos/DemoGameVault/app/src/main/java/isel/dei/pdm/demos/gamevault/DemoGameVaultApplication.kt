package isel.dei.pdm.demos.gamevault

import android.app.Application
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakeCredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.collection.PlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.search.CredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformSearchService

/**
 * The dependencies container to be used in production.
 */
class DemoGameVaultApplication : DependenciesContainer, Application() {

    override val credentialsRepository: CredentialsRepository by lazy {
        FakeCredentialsRepository()
    }

    override val platformSearchService: PlatformSearchService
        get() = TODO("Not yet implemented")

    override val platformCollectionRepository: PlatformCollectionRepository
        get() = TODO("Not yet implemented")
}