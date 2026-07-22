package isel.dei.pdm.puzzle.domain

/**
 * Counts the number of inversions in the given list of integers.
 * An inversion is a pair of elements (a, b) such that a appears before b, but a > b.
 * For more details on inversions in sliding puzzles, refer to:
 * https://en.wikipedia.org/wiki/15_puzzle#Solvability
 *
 * @return The total number of inversions.
 */
internal fun List<Int>.countInversions(): Int {
    var inversions = 0
    for (i in indices) {
        for (j in i + 1 until size) {
            if (this[i] > this[j]) inversions++
        }
    }
    return inversions
}

/**
 * Swaps two elements in the list and returns a new list.
 *
 * @param index1 The index of the first element.
 * @param index2 The index of the second element.
 * @return A new list with the elements at index1 and index2 swapped.
 */
internal fun <T> List<T>.swap(index1: Int, index2: Int): List<T> {
    if (index1 == index2) return this
    val result = toMutableList()
    val tmp = result[index1]
    result[index1] = result[index2]
    result[index2] = tmp
    return result.toList()
}
