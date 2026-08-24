package isel.dei.pdm.mygamevault.core

/**
 * Represents a string that is guaranteed not to be blank.
 * @property value The underlying string value.
 */
@JvmInline
value class NonBlankString(val value: String) {
    init {
        require(value.isNotBlank()) { "String cannot be blank" }
    }

    override fun toString(): String = value
}

/**
 * Extension function to convert a [String] to a [NonBlankString] if it's not blank.
 * @return The [NonBlankString] or null if the string is blank.
 */
fun String.toNonBlankStringOrNull(): NonBlankString? =
    if (this.isNotBlank()) NonBlankString(this) else null
