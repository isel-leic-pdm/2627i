package isel.dei.pdm.demos.gamevault.adapters.igdb

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakeCredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.Secrets
import isel.dei.pdm.demos.gamevault.domain.search.UnexpectedServiceException
import isel.dei.pdm.demos.gamevault.domain.search.games.GameInfo
import isel.dei.pdm.demos.gamevault.domain.search.games.GameInfoDetails
import isel.dei.pdm.demos.gamevault.domain.search.games.GamePlatform
import isel.dei.pdm.demos.gamevault.domain.search.games.PlatformCriterion
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertFailsWith

/**
 * A scripted reply to a request.
 */
private typealias Reply = suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData

/**
 * Tests for [IgdbGameSearchService]: the queries it sends and how it converts the responses.
 * Authentication, retries and the translation of failures are performed by [IgdbApi] and the
 * client it uses, which are covered by [IgdbPlatformSearchServiceTest].
 */
class IgdbGameSearchServiceTest {

    private companion object {
        val NAME = SearchString("Elden")
        val PS5 = byId("167")
        val PC = byId("6")
        val SWITCH = byName("Nintendo Switch", "NSW")

        fun byId(id: String) = PlatformCriterion.ById(NonBlankString(id))

        fun byName(name: String, abbreviation: String) =
            PlatformCriterion.ByName(NonBlankString(name), NonBlankString(abbreviation))

        fun image(size: String, id: String) = "https://images.igdb.com/igdb/image/upload/t_$size/$id.jpg"
    }

    private val secrets = Secrets(clientId = "client-id", clientSecret = "client-secret")

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun json(body: String): Reply = { respond(body, HttpStatusCode.OK, jsonHeaders) }

    /**
     * The requests sent to IGDB.
     */
    private val requests = mutableListOf<HttpRequestData>()

    /**
     * Creates a service whose requests to IGDB are replied to with the given replies, in order.
     * Unexpected requests (i.e. once the replies are exhausted) fail the test.
     */
    private fun newSut(vararg replies: Reply): IgdbGameSearchService {
        val pending = ArrayDeque(replies.toList())
        val engine = MockEngine { request ->
            requests += request
            val reply = pending.removeFirstOrNull() ?: error("Unexpected request to ${request.url}")
            reply(request)
        }
        val credentials = FakeCredentialsRepository(secrets, "stored-token")
        val client = createIgdbHttpClient(engine, credentials, retryDelay = { })
        return IgdbGameSearchService(IgdbApi(client, credentials))
    }

    // Queries

    @Test
    fun search_sends_the_query_to_the_games_endpoint() = runTest {
        // Arrange
        val sut = newSut(json("[]"))

        // Act
        sut.searchGames(NAME, listOf(PS5), category = null)

        // Assert
        val request = requests.single()
        assertEquals("$IGDB_BASE_URL/games", request.url.toString())
        assertEquals(
            gamesQuery(NAME, listOf(PS5), null),
            request.body.toByteArray().decodeToString()
        )
    }

    @Test
    fun query_searches_games_whose_name_contains_the_text() {
        // Act
        val query = gamesQuery(SearchString(" Elden "), listOf(PS5), null)!!

        // Assert
        assertTrue(query.contains("""where name ~ *"Elden"* & ("""))
        assertTrue(query.contains("sort name asc;"))
    }

    @Test
    fun query_escapes_the_search_text() {
        // Act
        val query = gamesQuery(SearchString("""a"b\c"""), listOf(PS5), null)!!

        // Assert
        assertTrue(query.contains("""name ~ *"a\"b\\c"*"""))
    }

    @Test
    fun query_matches_platforms_by_id_and_by_name() {
        // Act
        val query = gamesQuery(NAME, listOf(PS5, SWITCH, PC, PS5), null)!!

        // Assert - Each name is matched exactly, ignoring case, against all names of IGDB's platforms
        val expectedCondition = listOf(
            "platforms = (167,6)",
            """platforms.name ~ "Nintendo Switch"""",
            """platforms.abbreviation ~ "Nintendo Switch"""",
            """platforms.alternative_name ~ "Nintendo Switch"""",
            """platforms.name ~ "NSW"""",
            """platforms.abbreviation ~ "NSW"""",
            """platforms.alternative_name ~ "NSW""""
        ).joinToString(" | ")
        assertTrue(query.contains("& ($expectedCondition)"))
    }

    @Test
    fun query_escapes_the_platform_names() {
        // Act
        val query = gamesQuery(NAME, listOf(byName("""My "PC"""", "PC")), null)!!

        // Assert
        assertTrue(query.contains("""platforms.name ~ "My \"PC\"""""))
    }

    @Test
    fun query_leaves_out_identifiers_that_are_not_igdb_ones() {
        // Act - An identifier that would change the query if included
        val query = gamesQuery(NAME, listOf(byId("6) | id > (0"), PS5), null)!!

        // Assert
        assertTrue(query.contains("platforms = (167)"))
        assertFalse(query.contains("id > (0"))
    }

    @Test
    fun query_filters_by_the_game_type_of_the_category() {
        val expectedTypes = mapOf(
            GameInfo.Category.MAIN_GAME to 0,
            GameInfo.Category.DLC to 1,
            GameInfo.Category.BUNDLE to 3,
            GameInfo.Category.REMAKE to 8,
            GameInfo.Category.REMASTER to 9
        )

        // Fails if a new category is added without being mapped here
        assertEquals(GameInfo.Category.entries.toSet(), expectedTypes.keys)
        expectedTypes.forEach { (category, type) ->
            assertTrue(gamesQuery(NAME, listOf(PS5), category)!!.contains(") & game_type = $type;"))
        }
        assertFalse(gamesQuery(NAME, listOf(PS5), null)!!.contains("game_type ="))
    }

    @Test
    fun search_for_platforms_that_cannot_be_matched_sends_no_request() = runTest {
        // Arrange
        val sut = newSut()

        // Act
        val games = sut.searchGames(NAME, listOf(byId("not-a-number")), category = null)

        // Assert
        assertEquals(emptyList<GameInfo>(), games)
        assertTrue(requests.isEmpty())
    }

    @Test
    fun search_requires_at_least_one_platform() = runTest {
        assertFailsWith<IllegalArgumentException> {
            newSut().searchGames(NAME, emptyList(), category = null)
        }
    }

    // Results

    @Test
    fun search_converts_the_games() = runTest {
        // Arrange
        val response = """
            [
              { "id": 119133, "name": "Elden Ring", "first_release_date": 1645747200,
                "cover": { "id": 1, "image_id": "co4jni" }, "game_type": 0,
                "platforms": [
                  { "id": 167, "name": "PlayStation 5", "abbreviation": "PS5" },
                  { "id": 6, "name": "PC (Microsoft Windows)", "abbreviation": "PC", "alternative_name": "mswin" }
                ]
              }
            ]
        """.trimIndent()
        val sut = newSut(json(response))

        // Act
        val games = sut.searchGames(NAME, listOf(PS5, PC), category = null)

        // Assert
        assertEquals(
            listOf(
                GameInfo(
                    id = 119133L,
                    name = "Elden Ring",
                    releaseDate = LocalDate.of(2022, 2, 25),
                    coverUri = image("cover_big", "co4jni"),
                    thumbnailUri = image("cover_small", "co4jni"),
                    category = GameInfo.Category.MAIN_GAME,
                    platforms = listOf(
                        GamePlatform(167L, NonBlankString("PlayStation 5"), NonBlankString("PS5")),
                        GamePlatform(
                            6L,
                            NonBlankString("PC (Microsoft Windows)"),
                            NonBlankString("PC"),
                            listOf(NonBlankString("mswin"))
                        )
                    )
                )
            ),
            games
        )
    }

    @Test
    fun search_handles_missing_data() = runTest {
        // Arrange - A port (none of the supported categories), a game without name, and a game
        // with a platform without name
        val response = """
            [
              { "id": 1, "name": "Elden Ring", "game_type": 11 },
              { "id": 2, "name": " " },
              { "id": 3, "name": "Hades", "platforms": [ { "id": 6 }, { "id": 130, "name": "Nintendo Switch" } ] }
            ]
        """.trimIndent()
        val sut = newSut(json(response))

        // Act
        val games = sut.searchGames(NAME, listOf(PC), category = null)

        // Assert
        assertEquals(
            listOf(
                GameInfo(1L, "Elden Ring"),
                GameInfo(3L, "Hades", platforms = listOf(GamePlatform(130L, NonBlankString("Nintendo Switch"))))
            ),
            games
        )
    }

    @Test
    fun malformed_response_is_reported_as_unexpected() = runTest {
        // Arrange
        val sut = newSut(json("""{ "not": "an array" }"""))

        // Act & Assert
        assertFailsWith<UnexpectedServiceException> { sut.searchGames(NAME, listOf(PS5), null) }
    }

    @Test
    fun failures_are_reported_as_search_service_exceptions() = runTest {
        // Arrange
        val sut = newSut({ respond("", HttpStatusCode.BadRequest) })

        // Act & Assert
        assertFailsWith<UnexpectedServiceException> { sut.searchGames(NAME, listOf(PS5), null) }
    }

    // Details

    @Test
    fun fetchGameDetails_queries_the_game_by_id_and_converts_its_details() = runTest {
        // Arrange
        val response = """
            [
              { "id": 119133, "name": "Elden Ring", "summary": "An action RPG.",
                "genres": [ { "id": 12, "name": "Role-playing (RPG)" }, { "id": 31, "name": "Adventure" } ],
                "involved_companies": [
                  { "id": 1, "company": { "id": 10, "name": "FromSoftware" }, "developer": true, "publisher": false },
                  { "id": 2, "company": { "id": 11, "name": "Bandai Namco Entertainment" }, "developer": false, "publisher": true },
                  { "id": 3, "company": { "id": 10, "name": "FromSoftware" }, "developer": false, "publisher": true }
                ]
              }
            ]
        """.trimIndent()
        val sut = newSut(json(response))

        // Act
        val details = sut.fetchGameDetails(119133L)

        // Assert
        assertEquals(gameDetailsQuery(119133L), requests.single().body.toByteArray().decodeToString())
        assertTrue(gameDetailsQuery(119133L).contains("where id = 119133;"))
        assertEquals(
            GameInfoDetails(
                game = GameInfo(119133L, "Elden Ring"),
                description = "An action RPG.",
                developers = listOf("FromSoftware"),
                publishers = listOf("Bandai Namco Entertainment", "FromSoftware"),
                genres = listOf("Role-playing (RPG)", "Adventure")
            ),
            details
        )
    }

    @Test
    fun fetchGameDetails_returns_null_for_an_unknown_game() = runTest {
        // Arrange
        val sut = newSut(json("[]"))

        // Act & Assert
        assertNull(sut.fetchGameDetails(-1L))
    }
}
