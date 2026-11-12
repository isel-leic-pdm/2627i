package isel.dei.pdm.demos.gamevault.adapters.images

import isel.dei.pdm.demos.gamevault.domain.Uri
import isel.dei.pdm.demos.gamevault.images.ImageSource
import isel.dei.pdm.demos.gamevault.images.ImageStore
import isel.dei.pdm.demos.gamevault.images.ImageUnavailableException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.security.MessageDigest

/**
 * An [ImageStore] that keeps the copies as files in the given [directory] (e.g. a directory of
 * the app's internal storage). It is also the [ImageSource] of its copies.
 *
 * The copies are identified by URIs of the [SCHEME] scheme, whose path is the file's name (e.g.
 * vault-image://3f2a…), instead of the files' paths: the paths of the app's storage may differ
 * between devices (e.g. after restoring a backup), and the stored URIs would no longer be valid.
 *
 * Each file is named after the SHA-256 hash of its source URI, so that the same image is copied
 * once, and copies can be named before they are stored (e.g. before the game they belong to is
 * added). As names are restricted to hashes, the store's URIs cannot refer to files outside the
 * [directory] (e.g. vault-image://../secrets).
 *
 * @param directory The directory where the copies are kept. It is created when needed.
 * @param source The source the images to be copied are read from (e.g. the web).
 * @param ioDispatcher The dispatcher where the file system is accessed.
 */
class FileImageStore(
    private val directory: File,
    private val source: ImageSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ImageStore, ImageSource {

    override suspend fun store(source: Uri): Uri {
        if (source.scheme == SCHEME)
            return source
        val name = nameOf(source)
        val copy = Uri("$SCHEME://$name")
        if (withContext(ioDispatcher) { File(directory, name).exists() })
            return copy
        val content = this.source.read(source)
        withContext(ioDispatcher) { write(name, content) }
        return copy
    }

    override suspend fun read(uri: Uri): ByteArray {
        val file = fileOf(uri) ?: throw ImageUnavailableException("Not a copy of this store: $uri")
        return withContext(ioDispatcher) {
            try {
                file.readBytes()
            } catch (e: IOException) {
                throw ImageUnavailableException("The copy could not be read: $uri", e)
            }
        }
    }

    override suspend fun delete(uri: Uri) {
        val file = fileOf(uri) ?: return
        withContext(ioDispatcher) {
            if (file.exists() && !file.delete())
                throw ImageUnavailableException("The copy could not be deleted: $uri")
        }
    }

    /**
     * Writes the given content to the file with the given name. It is written to a temporary
     * file first, which is then renamed, so that a partially written copy is never read (e.g. if
     * the app is killed while writing it).
     */
    private fun write(name: String, content: ByteArray) {
        try {
            directory.mkdirs()
            val temporary = File.createTempFile(name.take(TEMPORARY_PREFIX_LENGTH), ".tmp", directory)
            temporary.writeBytes(content)
            if (!temporary.renameTo(File(directory, name))) {
                temporary.delete()
                throw IOException("The copy could not be renamed: $name")
            }
        } catch (e: IOException) {
            throw ImageUnavailableException("The copy could not be written: $name", e)
        }
    }

    /**
     * The file of the copy with the given URI, or null if it is not one of the store's URIs.
     */
    private fun fileOf(uri: Uri): File? {
        if (uri.scheme != SCHEME)
            return null
        val name = uri.value.substringAfter("://")
        return if (NAME.matches(name)) File(directory, name) else null
    }

    companion object {
        /**
         * The scheme of the store's URIs.
         */
        const val SCHEME = "vault-image"

        /**
         * The names of the copies: the hexadecimal SHA-256 hash of their source.
         */
        private val NAME = Regex("[0-9a-f]{64}")

        private const val TEMPORARY_PREFIX_LENGTH = 8

        /**
         * The name of the copy of the image at the given source.
         */
        private fun nameOf(source: Uri): String =
            MessageDigest.getInstance("SHA-256")
                .digest(source.value.toByteArray())
                .joinToString("") { "%02x".format(it) }
    }
}
