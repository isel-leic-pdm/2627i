package isel.dei.pdm.mygamevault.domain

/**
 * Represents a string that is guaranteed not to be blank.
 * @property value The underlying string value.
 */
@JvmInline
value class NonBlankString(val value: String) : CharSequence {
    init {
        require(value.isNotBlank()) { "String cannot be blank" }
    }

    override val length: Int get() = value.length
    override fun get(index: Int): Char = value[index]
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = value.subSequence(startIndex, endIndex)

    /**
     * Returns the underlying string value.
     */
    operator fun invoke(): String = value

    override fun toString(): String = value
}

/**
 * Extension function to convert a [String] to a [NonBlankString] if it's not blank.
 * @return The [NonBlankString] or null if the string is blank.
 */
fun String.toNonBlankStringOrNull(): NonBlankString? =
    if (this.isNotBlank()) NonBlankString(this) else null
