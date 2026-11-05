package isel.dei.pdm.demos.gamevault

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import isel.dei.pdm.demos.gamevault.adapters.DataStoreCredentialsRepository
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakeCredentialsRepository
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakePlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakePlatformSearchService
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
     *
     * IMPORTANT: The resulting file (datastore/credentials.preferences_pb) is excluded from
     * backups and device transfers in res/xml/backup_rules.xml and res/xml/data_extraction_rules.xml.
     * If the name changes, those files MUST be updated accordingly, otherwise the secrets will be
     * silently included in backups.
     */
    private val credentialsDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "credentials",
        corruptionHandler = DataStoreCredentialsRepository.corruptionHandler
    )

    override val credentialsRepository: CredentialsRepository by lazy {
        // DataStoreCredentialsRepository(dataStore = credentialsDataStore)
        FakeCredentialsRepository()
    }

    override val platformSearchService: PlatformSearchService by lazy {
        FakePlatformSearchService()
    }

    override val platformCollectionRepository: PlatformCollectionRepository by lazy {
        FakePlatformCollectionRepository()
    }
}
