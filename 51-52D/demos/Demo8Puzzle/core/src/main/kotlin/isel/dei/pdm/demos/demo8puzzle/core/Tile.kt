package isel.dei.pdm.demos.demo8puzzle.core

const val MIN_TILE_VALUE = 1
const val MAX_TILE_VALUE = BOARD_SIZE - 1

/**
 * Represents a tile in the 8-puzzle.
 * Tiles have values between [MIN_TILE_VALUE] and [MAX_TILE_VALUE].
 */
@JvmInline
value class Tile(val value: Int) {
    init {
        require(value in MIN_TILE_VALUE..MAX_TILE_VALUE) {
            "Tile value must be between $MIN_TILE_VALUE and $MAX_TILE_VALUE"
        }
    }

    override fun toString(): String = value.toString()
}
