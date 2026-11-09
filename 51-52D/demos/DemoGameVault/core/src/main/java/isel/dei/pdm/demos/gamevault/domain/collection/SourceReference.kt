package isel.dei.pdm.demos.gamevault.domain.collection

import isel.dei.pdm.demos.gamevault.domain.NonBlankString

/**
 * A reference to the external source from which an element of the vault was obtained (e.g. the
 * IGDB game it was added from). It records provenance only: elements are identified in the vault
 * by their local identifier, and elements inserted manually have no source reference.
 *
 * It enables, for example, recognizing the same element when importing a collection, or
 * refreshing the element's information from its source.
 *
 * @property provider The provider of the information.
 * @property externalId The element's identifier, as assigned by the provider. It is a string,
 * because providers use different kinds of identifiers (e.g. numbers or slugs).
 */
data class SourceReference(
    val provider: Provider,
    val externalId: NonBlankString
) {
    /**
     * The providers from which elements of the vault can be obtained.
     */
    enum class Provider {
        IGDB
    }

    // Note: A convenience constructor accepting a String for externalId is not possible, because
    // NonBlankString is a value class: both constructors would have the same JVM signature.
}
