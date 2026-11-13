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
import isel.dei.pdm.demos.gamevault.adapters.igdb.IgdbGameSearchService
import isel.dei.pdm.demos.gamevault.adapters.igdb.IgdbPlatformSearchService
import isel.dei.pdm.demos.gamevault.adapters.igdb.createIgdbHttpClient
import isel.dei.pdm.demos.gamevault.adapters.images.FileImageStore
import isel.dei.pdm.demos.gamevault.adapters.images.HttpImageSource
import isel.dei.pdm.demos.gamevault.adapters.images.SchemeImageSource
import isel.dei.pdm.demos.gamevault.adapters.images.cachingImageLoader
import isel.dei.pdm.demos.gamevault.adapters.images.createImageHttpClient
import isel.dei.pdm.demos.gamevault.adapters.room.GameVaultDatabase
import isel.dei.pdm.demos.gamevault.adapters.room.RoomGameCollectionRepository
import isel.dei.pdm.demos.gamevault.adapters.room.RoomPlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.PlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.search.CredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.search.games.GameSearchService
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformSearchService
import isel.dei.pdm.demos.gamevault.images.ImageLoader
import java.io.File

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

    /**
     * The HTTP client used to download images, with a disk cache in the app's cache directory,
     * which the system may clear when storage is low (see [createImageHttpClient]).
     */
    private val imageHttpClient: HttpClient by lazy {
        createImageHttpClient(cacheDirectory = File(cacheDir, "images"))
    }

    private val httpImageSource: HttpImageSource by lazy {
        HttpImageSource(imageHttpClient)
    }

    /**
     * The store of the local copies of the collection's images, in the app's internal storage.
     *
     * IMPORTANT: The directory (images/) is excluded from cloud backups in res/xml/backup_rules.xml
     * and res/xml/data_extraction_rules.xml, as the copies can be obtained again from their
     * sources, and may exceed Auto Backup's quota (25 MB), in which case nothing would be backed
     * up. If the name changes, those files MUST be updated accordingly.
     */
    override val imageStore: FileImageStore by lazy {
        FileImageStore(directory = File(filesDir, "images"), source = httpImageSource)
    }

    override val imageLoader: ImageLoader by lazy {
        cachingImageLoader(
            source = SchemeImageSource(
                mapOf(
                    "http" to httpImageSource,
                    "https" to httpImageSource,
                    FileImageStore.SCHEME to imageStore
                )
            )
        )
    }

    override val credentialsRepository: CredentialsRepository by lazy {
        // DataStoreCredentialsRepository(dataStore = credentialsDataStore)
        // For convenience, and while the settings are not wired, we use the dev-time credentials
        // from local.properties
        FakeCredentialsRepository.withDevCredentials()
    }

    override val platformSearchService: PlatformSearchService by lazy {
        IgdbPlatformSearchService(api = igdbApi)
    }

    override val gameSearchService: GameSearchService by lazy {
        IgdbGameSearchService(api = igdbApi)
    }

    override val platformCollectionRepository: PlatformCollectionRepository by lazy {
        RoomPlatformCollectionRepository(dao = database.platformDao())
    }

    override val gameCollectionRepository: GameCollectionRepository by lazy {
        RoomGameCollectionRepository(database = database)
    }
}
