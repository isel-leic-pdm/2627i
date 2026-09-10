package pt.isel.lei.pdm.counter

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun `CounterModel increments as expected`() {
        //  Arrange
        val orig = CounterModel(123)
        //  Act
        val inc = orig.increment()
        //  Assert
        assertEquals(inc.count, orig.count + 1)
    }
}