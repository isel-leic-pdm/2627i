package isel.dei.pdm.demos.gamevault.core.domain.search

/**
 * Represents a string that is used in search parameters, guaranteed to contain
 * at least [MIN_NON_BLANK_CHARACTERS] non-blank characters.
 * @property value The underlying string value.
 */
@JvmInline
value class SearchString(val value: String) : CharSequence {
    init {
        require(isValid(value)) {
            "Search string must contain at least $MIN_NON_BLANK_CHARACTERS non-blank characters"
        }
    }

    override val length: Int get() = value.length
    override fun get(index: Int): Char = value[index]
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = value.subSequence(startIndex, endIndex)

    /**
     * Returns the underlying string value.
     */
    operator fun invoke(): String = value

    override fun toString(): String = value

    companion object {
        const val MIN_NON_BLANK_CHARACTERS = 2

        /**
         * Checks whether the given string contains at least [MIN_NON_BLANK_CHARACTERS] non-blank characters.
         */
        fun isValid(value: String): Boolean =
            value.count { !it.isWhitespace() } >= MIN_NON_BLANK_CHARACTERS
    }
}

/**
 * Extension function to convert a [String] to a [SearchString] if it contains at least 2 non-blank characters.
 * @return The [SearchString] or null if the string is invalid.
 */
fun String.toSearchStringOrNull(): SearchString? =
    if (SearchString.isValid(this)) SearchString(this) else null
