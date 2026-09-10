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
    fun `Counter does increment by 1`() {
        //  arrange
        val value = CounterInfo(123)
        //  act
        val incrementedCounter = value.increment()
        //  assert
        assertEquals(incrementedCounter.counter, value.counter + 1)
    }
}