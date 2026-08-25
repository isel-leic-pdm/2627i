package isel.dei.pdm.mygamevault.domain

private val URI_PATTERN = Regex("""^([^:]+):(.+)$""")

/**
 * Represents a Uniform Resource Identifier (URI) in the domain.
 * This wrapper avoids primitive obsession and provides a type-safe way to handle
 * references to resources like game covers. These references may be external or internal
 * (e.g. a URL or a path to a local file)
 */
data class Uri(val value: String) {
    init {
        val match = URI_PATTERN.matchEntire(value)
        require(match != null) { "URI must be in the form <schema>:<schema_specific>" }
        val (schema, ssp) = match.destructured
        require(schema.isNotBlank()) { "URI schema cannot be blank" }
        require(ssp.isNotBlank()) { "URI schema-specific part cannot be blank" }
    }

    override fun toString(): String = value
}
