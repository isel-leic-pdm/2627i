package isel.dei.pdm.demos.demo8puzzle.core

/**
 * Represents a node in the search graph used by the puzzle solver.
 */
data class GraphNode(
    val puzzle: Puzzle,
    val move: Tile? = null,
    val parent: GraphNode? = null
) {

    /**
     * Rebuilds the solution path from the current node to the root node.
     *
     * @return the list of tiles representing the solution path, or an empty list if this is
     * the root node.
     */
    fun rebuildSolutionPath(): List<Tile> = buildList {
        var current = this@GraphNode
        while (current.parent != null) {
            add(index = 0, current.move ?: error("Move is null"))
            current = current.parent
        }
    }

    /**
     * Finds the neighboring nodes of the current node. Neighboring nodes are those that are
     * one move away from the current node.
     *
     * @return the list of neighboring nodes
     */
    fun findNeighbors(): List<GraphNode> = puzzle
        .getMoveableTiles()
        .map { tile ->
            GraphNode(
                puzzle = puzzle.move(tile),
                move = tile,
                parent = this
            )
        }
}

/**
 * Finds a solution to the puzzle, if it exists. If the puzzle is solved, the function returns
 * an empty list.
 *
 * @param board the initial state of the puzzle
 * @param onStep a suspending function that is called after each step of the algorithm.
 * @return the list of tiles representing the solution path, or null if no solution is found.
 */
suspend fun findSolutionOrNull(board: Puzzle, onStep: suspend () -> Unit): List<Tile>? {
    val openSet = mutableListOf(GraphNode(board))
    val closedSet = mutableSetOf<Puzzle>()

    while (openSet.isNotEmpty()) {
        val current = openSet.removeAt(index = 0)

        if (!closedSet.add(current.puzzle)) {
            continue
        }

        if (current.puzzle.isSolved()) {
            return current.rebuildSolutionPath()
        }

        current.findNeighbors().forEach { neighbor ->
            if (neighbor.puzzle !in closedSet) {
                openSet.add(neighbor)
            }
        }

        onStep()
    }

    return null
}
