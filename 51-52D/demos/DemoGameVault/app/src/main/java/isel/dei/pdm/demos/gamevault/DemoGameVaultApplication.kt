package isel.dei.pdm.demos.gamevault

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import isel.dei.pdm.demos.gamevault.adapters.datastore.DataStoreCredentialsRepository
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakeCredentialsRepository
import isel.dei.pdm.demos.gamevault.adapters.igdb.IgdbApi
import isel.dei.pdm.demos.gamevault.adapters.igdb.IgdbPlatformSearchService
import isel.dei.pdm.demos.gamevault.adapters.igdb.createIgdbHttpClient
import isel.dei.pdm.demos.gamevault.adapters.room.GameVaultDatabase
import isel.dei.pdm.demos.gamevault.adapters.room.RoomPlatformCollectionRepository
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

    /**
     * The database where the user's collection is stored. Room requires a single instance per
     * database file, which holds because there is a single application instance per process.
     */
    private val database: GameVaultDatabase by lazy {
        Room.databaseBuilder(this, GameVaultDatabase::class.java, GameVaultDatabase.NAME).build()
    }

    /**
     * The HTTP client used to access IGDB. It is shared by all IGDB services, so that they also
     * share the access token and the connection pool.
     */
    private val igdbHttpClient: HttpClient by lazy {
        createIgdbHttpClient(engine = OkHttp.create(), credentials = credentialsRepository)
    }

    private val igdbApi: IgdbApi by lazy {
        IgdbApi(client = igdbHttpClient, credentials = credentialsRepository)
    }

    override val credentialsRepository: CredentialsRepository by lazy {
        // DataStoreCredentialsRepository(dataStore = credentialsDataStore)
        // While the settings are not wired, the dev-time credentials from local.properties are used
        FakeCredentialsRepository.withDevCredentials()
    }

    override val platformSearchService: PlatformSearchService by lazy {
        IgdbPlatformSearchService(api = igdbApi)
    }

    override val platformCollectionRepository: PlatformCollectionRepository by lazy {
        RoomPlatformCollectionRepository(dao = database.platformDao())
    }
}
