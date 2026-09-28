package isel.dei.pdm.demos.demo8puzzle.core

import org.junit.Test
import org.junit.Assert.assertEquals

class TileTest {
    @Test
    fun `Tile value must be between min and max`() {
        assertEquals(MIN_TILE_VALUE, Tile(MIN_TILE_VALUE).value)
        assertEquals(MAX_TILE_VALUE, Tile(MAX_TILE_VALUE).value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Tile value cannot be below min`() {
        Tile(MIN_TILE_VALUE - 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Tile value cannot be above max`() {
        Tile(MAX_TILE_VALUE + 1)
    }
}
