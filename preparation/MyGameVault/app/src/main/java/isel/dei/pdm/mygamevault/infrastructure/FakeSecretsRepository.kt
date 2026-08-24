package isel.dei.pdm.mygamevault.infrastructure

import isel.dei.pdm.mygamevault.core.Secrets
import isel.dei.pdm.mygamevault.core.SecretsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.milliseconds

/**
 * A fake implementation of [SecretsRepository] that stores values in memory.
 */
open class FakeSecretsRepository : SecretsRepository {

    private val _secrets = MutableStateFlow<Secrets?>(null)
    override val secrets: Flow<Secrets?> = _secrets.asStateFlow()

    override suspend fun saveSecrets(secrets: Secrets): Result<Unit> {
        delay(50.milliseconds) // Force suspension to simulate real persistence
        _secrets.value = secrets
        return Result.success(Unit)
    }
}
