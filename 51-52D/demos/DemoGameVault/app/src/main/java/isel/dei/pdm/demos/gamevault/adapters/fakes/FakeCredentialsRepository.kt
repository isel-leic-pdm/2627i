package isel.dei.pdm.demos.gamevault.adapters.fakes

import isel.dei.pdm.demos.gamevault.domain.RecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.search.CredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.search.Secrets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A fake implementation of the [CredentialsRepository] interface.
 * We use it to have fine control on the repository behavior while developing the app.
 */
class FakeCredentialsRepository(
    initialSecrets: Secrets? = null,
    initialAccessToken: String? = null
) : CredentialsRepository {

    private val _secrets = MutableStateFlow<Secrets?>(initialSecrets)
    override val secrets: Flow<Secrets?> = _secrets.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(initialAccessToken)
    override val accessToken: Flow<String?> = _accessToken.asStateFlow()

    override suspend fun saveSecrets(secrets: Secrets): Result<Unit> {
        if (secrets.clientSecret == "u" || secrets.clientId == "u")
            return Result.failure(UnrecoverablePersistenceException("Cannot recover"))
        if (secrets.clientSecret == "r" || secrets.clientId == "r")
            return Result.failure(RecoverablePersistenceException("Cannot recover"))
        _secrets.value = secrets
        return Result.success(Unit)
    }

    override suspend fun saveAccessToken(accessToken: String): Result<Unit> {
        _accessToken.value = accessToken
        return Result.success(Unit)
    }
}
