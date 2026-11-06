package isel.dei.pdm.demos.gamevault.adapters.fakes

import isel.dei.pdm.demos.gamevault.BuildConfig
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.search.CredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.search.Secrets
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds

/**
 * A fake implementation of the [CredentialsRepository] interface.
 * We use it to have fine control on the repository behavior while developing the app.
 *
 * Failures are reported in the same way as in the real implementation (the data store one), that
 * is, by throwing an [UnrecoverablePersistenceException] wrapping an [IOException].
 *
 * @param initialSecrets The secrets initially stored.
 * @param initialAccessToken The access token initially stored.
 * @param loadFails Whether loading the secrets fails.
 * Saving fails whenever the client ID or the client secret is "f".
 */
class FakeCredentialsRepository(
    initialSecrets: Secrets? = null,
    initialAccessToken: String? = null,
    private val loadFails: Boolean = false
) : CredentialsRepository {

    private val _secrets = MutableStateFlow<Secrets?>(initialSecrets)
    override val secrets: Flow<Secrets?> = flow {
        if (loadFails) {
            throw UnrecoverablePersistenceException(
                "Failed to read credentials",
                IOException("Simulated load failure")
            )
        }
        emitAll(_secrets)
    }

    private val _accessToken = MutableStateFlow<String?>(initialAccessToken)
    override val accessToken: Flow<String?> = _accessToken.asStateFlow()

    override suspend fun saveSecrets(secrets: Secrets) {
        delay(1000.milliseconds) // simulate a delay
        if (secrets.clientSecret == "f" || secrets.clientId == "f")
            throw UnrecoverablePersistenceException(
                "Failed to save secrets",
                IOException("Simulated save failure")
            )
        _secrets.value = secrets
    }

    override suspend fun saveAccessToken(accessToken: String) {
        delay(200.milliseconds) // simulate a delay
        _accessToken.value = accessToken
    }

    companion object {
        /**
         * Creates a repository initialized with the dev-time credentials defined in local.properties
         * (igdb.client.id, igdb.client.secret and, optionally, igdb.access.token). They are only
         * available in debug builds: in release builds, the repository is created empty.
         * If the access token is absent, the IGDB client obtains one using the secrets.
         */
        fun withDevCredentials(): FakeCredentialsRepository = FakeCredentialsRepository(
            initialSecrets = Secrets(BuildConfig.IGDB_CLIENT_ID, BuildConfig.IGDB_CLIENT_SECRET)
                .takeIf { it.clientId.isNotBlank() && it.clientSecret.isNotBlank() },
            initialAccessToken = BuildConfig.IGDB_ACCESS_TOKEN.ifBlank { null }
        )
    }
}
