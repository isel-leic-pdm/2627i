package isel.dei.pdm.demos.demo8puzzle.core

import kotlin.math.abs

const val BOARD_SIDE = 3
const val BOARD_SIZE = BOARD_SIDE * BOARD_SIDE

/**
 * Represents the 8-puzzle board state.
 * The puzzle is immutable and provides both linear and matrix-like access to its tiles.
 * The empty space is represented by null.
 * @param tiles the list of tiles, with the empty space represented by null
 * @throws IllegalArgumentException On instantiation if the list does not have exactly BOARD_SIZE elements
 * @throws IllegalArgumentException On instantiation if the list does not contain each tile from 1 to 8 exactly once
 */
class Puzzle(private val tiles: List<Tile?>) : Iterable<Tile?> {

    init {
        require(tiles.size == BOARD_SIZE) { "Puzzle must have exactly $BOARD_SIZE tiles" }
        require(tiles.filterNotNull().distinct().size == BOARD_SIZE - 1) { 
            "Puzzle must contain each tile from 1 to 8 exactly once" 
        }
        require(tiles.count { it == null } == 1) { "Puzzle must have exactly one empty slot" }
    }

    /**
     * Secondary constructor to create a puzzle from integers, where 0 represents the empty space.
     * It's mostly a convenience constructor, to be used in tests and previews.
     */
    constructor(vararg values: Int) : this(
        values.map { if (it == 0) null else Tile(it) }
    )

    /**
     * Requirement for the Iterable interface.
     */
    override fun iterator(): Iterator<Tile?> = tiles.iterator()

    /**
     * Linear access to the tiles.
     * @param index the linear index (0 <= index < BOARD_SIZE) of the tile
     * @return the tile at the given index, or null if it's the empty space
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    operator fun get(index: Int): Tile? = tiles[index]

    /**
     * Matrix-like access to the tiles.
     * @param row the row index (0 <= row < BOARD_SIDE) of the tile
     * @param col the column index (0 <= col < BOARD_SIDE) of the tile
     * @return the tile at the given row and column, or null if it's the empty space
     * @throws IndexOutOfBoundsException if either the row or the column is out of bounds
     */
    operator fun get(row: Int, col: Int): Tile? {
        require(row in 0 until BOARD_SIDE && col in 0 until BOARD_SIDE) {
            "Coordinates ($row, $col) are out of bounds"
        }
        return tiles[row * BOARD_SIDE + col]
    }

    /**
     * Returns a new Puzzle instance with the given tile moved to the empty space,
     * if the tile is adjacent to the empty space.
     * @throws IllegalArgumentException if the tile is not adjacent to the empty space
     */
    fun move(tile: Tile): Puzzle {
        val tileIndex = tiles.indexOf(tile)
        val blankIndex = tiles.indexOf(null)

        if (!isAdjacent(tileIndex, blankIndex)) {
            return this
        }

        val newTiles = tiles.toMutableList()
        newTiles[blankIndex] = tile
        newTiles[tileIndex] = null
        return Puzzle(newTiles)
    }

    /**
     * Checks whether the given rectangular coordinates are adjacent to the empty space.
     */
    private fun isAdjacent(idx1: Int, idx2: Int): Boolean {
        val r1 = idx1 / BOARD_SIDE
        val c1 = idx1 % BOARD_SIDE
        val r2 = idx2 / BOARD_SIDE
        val c2 = idx2 % BOARD_SIDE

        return (abs(r1 - r2) == 1 && c1 == c2) || (abs(c1 - c2) == 1 && r1 == r2)
    }

    /**
     * Returns the list of tiles that can be moved.
     * @return the list of tiles
     */
    fun getMoveableTiles(): List<Tile> = getMoveableTilesIndexes().mapNotNull { tiles[it] }

    /**
     * Returns the list of linear indices of tiles that can be moved to the empty space
     * @return the list of linear indices
     */
    fun getMoveableTilesIndexes(): List<Int> {
        val blankIndex = tiles.indexOf(null)
        return buildList {
            if (blankIndex - BOARD_SIDE >= 0) add(blankIndex - BOARD_SIDE)          // UP
            if (blankIndex + BOARD_SIDE < BOARD_SIZE) add(blankIndex + BOARD_SIDE)  // DOWN
            if (blankIndex % BOARD_SIDE > 0) add(blankIndex - 1)                    // LEFT
            if (blankIndex % BOARD_SIDE < BOARD_SIDE - 1) add(blankIndex + 1)       // RIGHT
        }
    }

    /**
     * Shuffles the puzzle using a random permutation with parity correction to ensure
     * a uniform distribution over all solvable configurations in O(1) expected time.
     * @see <a href="https://en.wikipedia.org/wiki/15_puzzle#Solvability">15 puzzle Solvability Theory</a>
     * @return the shuffled puzzle
     */
    fun shuffle(): Puzzle {
        val shuffledTiles = tiles.shuffled().toMutableList()
        val candidate = Puzzle(shuffledTiles)

        if (!candidate.isSolvable()) {
            // Swapping any two adjacent non-blank tiles flips the inversion parity
            // from odd to even, converting an unsolvable configuration into a solvable one.
            val firstIndex = shuffledTiles.indexOfFirst { it != null }
            val secondIndex = shuffledTiles.indexOfFirst { it != null && it != shuffledTiles[firstIndex] }
            val temp = shuffledTiles[firstIndex]
            shuffledTiles[firstIndex] = shuffledTiles[secondIndex]
            shuffledTiles[secondIndex] = temp
        }

        val result = Puzzle(shuffledTiles)
        return if (result.isSolved()) result.shuffle() else result
    }

    // Canonical methods

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Puzzle) return false
        return tiles == other.tiles
    }

    override fun hashCode(): Int = tiles.hashCode()

    override fun toString(): String = tiles.toString()
}

/**
 * The solved puzzle
 */
val solvedPuzzle = Puzzle(1, 2, 3, 4, 5, 6, 7, 8, 0)

/**
 * Checks whether the puzzle is solved.
 * @return true if the puzzle is solved, false otherwise
 */
fun Puzzle.isSolved(): Boolean = this == solvedPuzzle

/**
 * Calculates the number of inversions in the puzzle.
 * An inversion is a pair of tiles (a, b) such that a > b and a appears before b
 * in the linear representation of the board, ignoring the empty space (null).
 */
fun Puzzle.inversions(): Int {
    val values = filterNotNull().map { it.value }
    var count = 0
    for (i in values.indices) {
        for (j in i + 1 until values.size) {
            if (values[i] > values[j]) {
                count++
            }
        }
    }
    return count
}

/**
 * Determines whether the puzzle configuration is solvable.
 * For an odd grid width (such as 3x3), a puzzle configuration is solvable if and only if
 * the number of inversions is even.
 *
 * @see <a href="https://en.wikipedia.org/wiki/15_puzzle#Solvability">15 puzzle Solvability Theory</a>
 */
fun Puzzle.isSolvable(): Boolean = inversions() % 2 == 0
