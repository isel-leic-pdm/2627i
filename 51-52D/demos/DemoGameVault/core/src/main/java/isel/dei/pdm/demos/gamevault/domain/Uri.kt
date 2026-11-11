package isel.dei.pdm.demos.gamevault.domain

/**
 * A reference to a resource (e.g. an image), in the form of a URI. The domain has its own type,
 * instead of using a platform's (e.g. java.net.URI, which is JVM only, or android.net.Uri, which
 * is Android only), so that it does not depend on any platform.
 *
 * Only the minimal syntax is checked: the URI must start with a scheme (e.g. https, or any other,
 * as defined in RFC 3986) and contain no whitespace. Parsing it (e.g. to access its host or path)
 * is left to the platforms that use the resource.
 *
 * Note: It is a regular class, not a value class (like [NonBlankString]), because a value class is
 * compiled as a String: the convenience constructors that accept strings instead of URIs would
 * then have the same JVM signature as the primary ones.
 *
 * @property value The URI, as a string.
 * @throws IllegalArgumentException If the value does not have the minimal syntax of a URI.
 */
data class Uri(val value: String) {
    init {
        require(URI_SYNTAX.matches(value)) { "Not a URI: $value" }
    }

    override fun toString(): String = value
}

/**
 * A scheme (a letter followed by letters, digits, '+', '-' or '.'), a colon, and at least one
 * more character, none of them whitespace.
 */
private val URI_SYNTAX = Regex("""[A-Za-z][A-Za-z0-9+.\-]*:\S+""")

/**
 * Converts this string to a [Uri], if it has the minimal syntax of a URI.
 * @return The [Uri], or null if the string is not a URI.
 */
fun String.toUriOrNull(): Uri? =
    if (URI_SYNTAX.matches(this)) Uri(this) else null
