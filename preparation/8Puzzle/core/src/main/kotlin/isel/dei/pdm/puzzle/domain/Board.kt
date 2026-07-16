package isel.dei.pdm.puzzle.domain

import kotlin.math.abs

/**
 * Represents the 8-puzzle board.
 * The board is a 3x3 grid with tiles numbered 1 to 8 and one blank space.
 */
class Board private constructor(private val tiles: List<Int>) {

    /**
     * Represents a coordinate in the puzzle board.
     * @property row The row index.
     * @property col The column index.
     */
    data class Coordinate(val row: Int, val col: Int) {
        init {
            require(row in 0 until BOARD_SIDE) { "Row must be between 0 and ${BOARD_SIDE - 1}" }
            require(col in 0 until BOARD_SIDE) { "Column must be between 0 and ${BOARD_SIDE - 1}" }
        }

        /**
         * Checks if this coordinate is adjacent to another coordinate (not including diagonals).
         * @param other The other coordinate to check.
         * @return True if adjacent, false otherwise.
         */
        fun isAdjacentTo(other: Coordinate): Boolean {
            val rowDiff = abs(this.row - other.row)
            val colDiff = abs(this.col - other.col)
            return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1)
        }
    }

    companion object {
        const val BOARD_SIDE = 3
        private const val BLANK = 0
        private val SOLVED_TILES = (1 until BOARD_SIDE * BOARD_SIDE).toList() + BLANK
        
        /**
         * The range of tiles in the puzzle (excluding the blank space).
         */
        val TILE_RANGE = 1 until BOARD_SIDE * BOARD_SIDE

        /**
         * The solved state of the board.
         */
        val SOLVED = Board(SOLVED_TILES)

        /**
         * Creates a board with the given tiles. 
         * This is internal to the module to allow tests to set up specific states,
         * while keeping the public API clean of the internal '0 as blank' representation.
         */
        internal operator fun invoke(tiles: List<Int>) = Board(tiles)

        /**
         * Creates a random solvable board.
         */
        fun createRandom(): Board {
            var shuffled: List<Int>
            do {
                shuffled = SOLVED_TILES.shuffled()
            } while (!isSolvable(shuffled))
            return Board(shuffled)
        }

        /**
         * An 8-puzzle is solvable if the number of inversions is even.
         * For a detailed explanation of the solvability rule, see:
         * https://www.geeksforgeeks.org/check-instance-8-puzzle-solvable/
         */
        private fun isSolvable(tiles: List<Int>): Boolean {
            val list = tiles.filter { it != BLANK }
            return list.countInversions() % 2 == 0
        }
    }

    /**
     * Checks if the board is in the solved state.
     */
    val isSolved: Boolean
        get() = tiles == SOLVED_TILES

    /**
     * Gets the tile at the specified coordinate.
     * @return The tile value, or null if the space is blank.
     */
    fun getTileAt(coordinate: Coordinate): Int? {
        val tile = tiles[coordinate.row * BOARD_SIDE + coordinate.col]
        return if (tile == BLANK) null else tile
    }

    /**
     * Gets the coordinate of a specific tile.
     */
    fun getCoordinateOf(tile: Int): Coordinate {
        val index = tiles.indexOf(tile)
        return Coordinate(index / BOARD_SIDE, index % BOARD_SIDE)
    }

    /**
     * Gets the coordinate of the blank space.
     */
    private fun getBlankCoordinate(): Coordinate {
        val index = tiles.indexOf(BLANK)
        return Coordinate(index / BOARD_SIDE, index % BOARD_SIDE)
    }

    /**
     * Moves a tile if it is adjacent to the blank space.
     * @param tile The tile value to move.
     * @return A new Board with the tile moved, or the same Board if the move is invalid.
     */
    fun move(tile: Int): Board {
        if (tile !in 1 until BOARD_SIDE * BOARD_SIDE) return this
        
        val tileCoord = getCoordinateOf(tile)
        val blankCoord = getBlankCoordinate()

        return if (tileCoord.isAdjacentTo(blankCoord)) {
            val tileIndex = tiles.indexOf(tile)
            val blankIndex = tiles.indexOf(BLANK)
            Board(tiles.swap(tileIndex, blankIndex))
        } else {
            this
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Board) return false
        return tiles == other.tiles
    }

    override fun hashCode(): Int = tiles.hashCode()

    override fun toString(): String = "Board(tiles=$tiles)"
}
