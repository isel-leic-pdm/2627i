package isel.dei.pdm.demos.demo8puzzle.core

import kotlin.math.abs

const val BOARD_SIDE = 3
const val BOARD_SIZE = BOARD_SIDE * BOARD_SIDE

/**
 * Represents the 8-puzzle board state.
 * The puzzle is immutable and provides both linear and matrix-like access to its tiles.
 * The empty space is represented by null.
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
     */
    constructor(vararg values: Int) : this(
        values.map { if (it == 0) null else Tile(it) }
    )

    override fun iterator(): Iterator<Tile?> = tiles.iterator()

    /**
     * Linear access to the tiles.
     */
    operator fun get(index: Int): Tile? = tiles[index]

    /**
     * Matrix-like access to the tiles.
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

    private fun isAdjacent(idx1: Int, idx2: Int): Boolean {
        val r1 = idx1 / BOARD_SIDE
        val c1 = idx1 % BOARD_SIDE
        val r2 = idx2 / BOARD_SIDE
        val c2 = idx2 % BOARD_SIDE

        return (abs(r1 - r2) == 1 && c1 == c2) || (abs(c1 - c2) == 1 && r1 == r2)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Puzzle) return false
        return tiles == other.tiles
    }

    override fun hashCode(): Int = tiles.hashCode()

    override fun toString(): String = tiles.toString()

    /**
     * Shuffles the puzzle by making random valid moves to ensure it remains solvable.
     */
    fun shuffle(numberOfMoves: Int = 100): Puzzle {
        var current = this
        repeat(numberOfMoves) {
            val nullIndex = current.tiles.indexOf(null)
            val row = nullIndex / BOARD_SIDE
            val col = nullIndex % BOARD_SIDE
            val adjacentTiles = mutableListOf<Tile>()
            if (row > 0) current[row - 1, col]?.let { adjacentTiles.add(it) }
            if (row < BOARD_SIDE - 1) current[row + 1, col]?.let { adjacentTiles.add(it) }
            if (col > 0) current[row, col - 1]?.let { adjacentTiles.add(it) }
            if (col < BOARD_SIDE - 1) current[row, col + 1]?.let { adjacentTiles.add(it) }
            if (adjacentTiles.isNotEmpty()) {
                current = current.move(adjacentTiles.random())
            }
        }
        return if (current.isSolved()) current.shuffle(numberOfMoves) else current
    }
}

val solvedPuzzle = Puzzle(1, 2, 3, 4, 5, 6, 7, 8, 0)

fun Puzzle.isSolved(): Boolean = this == solvedPuzzle
