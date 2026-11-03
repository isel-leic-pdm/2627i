package isel.dei.pdm.demos.gamevault

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import isel.dei.pdm.demos.gamevault.adapters.DataStoreCredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.collection.PlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.search.CredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformSearchService

/**
 * The dependencies container to be used in production.
 */
class DemoGameVaultApplication : DependenciesContainer, Application() {

    /**
     * The data store where the credentials are persisted. DataStore requires a single instance
     * per file, which holds because there is a single application instance per process.
     */
    private val credentialsDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "credentials"
    )

    override val credentialsRepository: CredentialsRepository by lazy {
        DataStoreCredentialsRepository(dataStore = credentialsDataStore)
        //FakeCredentialsRepository()
    }

    override val platformSearchService: PlatformSearchService
        get() = TODO("Not yet implemented")

    override val platformCollectionRepository: PlatformCollectionRepository
        get() = TODO("Not yet implemented")
}
