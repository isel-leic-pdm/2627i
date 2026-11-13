package isel.dei.pdm.demos.gamevault.adapters.room

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.NewPlatform
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomPlatformCollectionRepositoryTest {

    private lateinit var database: GameVaultDatabase
    private lateinit var sut: RoomPlatformCollectionRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            GameVaultDatabase::class.java
        ).build()
        sut = RoomPlatformCollectionRepository(database.platformDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    private val ps5 = NewPlatform(
        abbreviation = "PS5",
        name = "PlayStation 5",
        logoUri = "https://example.com/ps5.png",
        ownedSince = 2021
    )
    private val ps5Pro = NewPlatform(abbreviation = "PS5", name = "PlayStation 5", version = "Pro")
    private val switch = NewPlatform(abbreviation = "Switch", name = "Nintendo Switch")

    // Adding and reading

    @Test
    fun get_all_when_empty_returns_empty_list() = runTest {
        // Act
        val platforms = sut.getAll()

        // Assert
        assertTrue(platforms.isEmpty())
    }

    @Test
    fun add_platform_persists_all_its_data() = runTest {
        // Act
        val id = sut.add(ps5)

        // Assert
        val expected = Platform(
            id = id,
            abbreviation = "PS5",
            name = "PlayStation 5",
            logoUri = "https://example.com/ps5.png",
            ownedSince = 2021
        )
        assertEquals(expected, sut.getById(id))
    }

    @Test
    fun add_platform_with_version_persists_the_version() = runTest {
        // Act
        val id = sut.add(ps5Pro)

        // Assert
        assertEquals("Pro", sut.getById(id)?.version?.value)
    }

    @Test
    fun get_by_id_of_unknown_platform_returns_null() = runTest {
        // Act
        val platform = sut.getById(42)

        // Assert
        assertNull(platform)
    }

    @Test
    fun get_all_returns_platforms_sorted_by_name_and_version_ignoring_case() = runTest {
        // Arrange
        val xbox = NewPlatform(abbreviation = "XSX", name = "xbox Series X")
        sut.add(xbox)
        sut.add(ps5Pro)
        sut.add(switch)
        sut.add(ps5)

        // Act
        val names = sut.getAll().map { "${it.name}/${it.version?.value.orEmpty()}" }

        // Assert
        assertEquals(
            listOf("Nintendo Switch/", "PlayStation 5/", "PlayStation 5/Pro", "xbox Series X/"),
            names
        )
    }

    // Uniqueness

    @Test
    fun add_existing_platform_returns_its_id_and_does_not_duplicate_it() = runTest {
        // Arrange
        val id = sut.add(switch)

        // Act
        val secondId = sut.add(switch)

        // Assert
        assertEquals(id, secondId)
        assertEquals(1, sut.getAll().size)
    }

    @Test
    fun add_existing_platform_ignoring_case_returns_its_id() = runTest {
        // Arrange
        val id = sut.add(ps5Pro)

        // Act
        val secondId = sut.add(
            NewPlatform(abbreviation = "ps5", name = "PLAYSTATION 5", version = "pro")
        )

        // Assert
        assertEquals(id, secondId)
        assertEquals(1, sut.getAll().size)
    }

    @Test
    fun add_existing_platform_does_not_change_its_data() = runTest {
        // Arrange
        val id = sut.add(ps5)

        // Act
        sut.add(NewPlatform(abbreviation = "PS5", name = "PlayStation 5", ownedSince = 2024))

        // Assert
        assertEquals(2021, sut.getById(id)?.ownedSince?.value)
    }

    @Test
    fun add_platform_differing_only_in_version_creates_a_new_one() = runTest {
        // Arrange
        val id = sut.add(ps5)

        // Act
        val proId = sut.add(ps5Pro)

        // Assert
        assertNotEquals(id, proId)
        assertEquals(2, sut.getAll().size)
    }

    @Test
    fun add_platform_differing_only_in_abbreviation_creates_a_new_one() = runTest {
        // Arrange
        val id = sut.add(switch)

        // Act
        val otherId = sut.add(NewPlatform(abbreviation = "NSW", name = "Nintendo Switch"))

        // Assert
        assertNotEquals(id, otherId)
        assertEquals(2, sut.getAll().size)
    }

    // Observing

    @Test
    fun platforms_flow_emits_the_collection_after_each_change() = runTest {
        // Arrange
        val emissions = Channel<List<Platform>>(Channel.UNLIMITED)
        backgroundScope.launch { sut.platforms.collect { emissions.send(it) } }
        assertTrue(emissions.receive().isEmpty())

        // Act & Assert
        sut.add(switch)
        assertEquals(listOf("Nintendo Switch"), emissions.receive().map { it.name.value })

        sut.add(ps5)
        assertEquals(
            listOf("Nintendo Switch", "PlayStation 5"),
            emissions.receive().map { it.name.value }
        )
    }

    // Error translation

    @Test
    fun corrupted_stored_data_is_reported_as_unrecoverable() = runTest {
        // Arrange
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO platforms (abbreviation, name, version) VALUES ('X', '  ', '')"
        )

        // Act & Assert
        try {
            sut.getAll()
            fail("Expected UnrecoverablePersistenceException")
        } catch (_: UnrecoverablePersistenceException) {
        }
    }

    @Test
    fun database_errors_are_reported_as_unrecoverable() = runTest {
        // Arrange
        database.openHelper.writableDatabase.execSQL("DROP TABLE platforms")

        // Act & Assert
        try {
            sut.add(switch)
            fail("Expected UnrecoverablePersistenceException")
        } catch (_: UnrecoverablePersistenceException) {
        }
    }
}
