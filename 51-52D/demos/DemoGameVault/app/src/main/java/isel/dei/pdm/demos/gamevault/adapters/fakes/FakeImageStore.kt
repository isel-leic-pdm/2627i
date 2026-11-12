package isel.dei.pdm.demos.gamevault.adapters.fakes

import isel.dei.pdm.demos.gamevault.domain.Uri
import isel.dei.pdm.demos.gamevault.images.ImageStore
import isel.dei.pdm.demos.gamevault.images.ImageUnavailableException

/**
 * A fake, in-memory implementation of the [ImageStore] interface, whose copies are only
 * references (e.g. fake-image://1). We use it to have fine control over the store's behavior
 * while developing the app and in tests.
 *
 * @param storeFails Whether storing copies fails (e.g. to simulate the lack of connectivity).
 */
class FakeImageStore(var storeFails: Boolean = false) : ImageStore {

    /**
     * The URIs of the copies, indexed by the URIs of their sources.
     */
    val copies: Map<Uri, Uri>
        get() = _copies.toMap()

    private val _copies = mutableMapOf<Uri, Uri>()

    override suspend fun store(source: Uri): Uri {
        if (storeFails)
            throw ImageUnavailableException("Simulated failure storing $source")
        return _copies.getOrPut(source) { Uri("fake-image://${_copies.size + 1}") }
    }

    override suspend fun delete(uri: Uri) {
        _copies.entries.removeAll { it.value == uri }
    }
}
