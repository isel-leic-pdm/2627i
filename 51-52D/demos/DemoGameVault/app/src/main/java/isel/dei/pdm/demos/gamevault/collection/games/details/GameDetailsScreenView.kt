package isel.dei.pdm.demos.gamevault.collection.games.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.GameDatesRow
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.GameDatesRowPlaceholder
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.GameDetailsHeader
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.GameDetailsHeaderPlaceholder
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.GameHistory
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.GameHistoryPlaceholder
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.GameNotes
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.GameNotesPlaceholder
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.PlaySessionBar
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.PlaySessionBarPlaceholder
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.PlayStatusPanel
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.PlayStatusPanelPlaceholder
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.Year
import isel.dei.pdm.demos.gamevault.domain.collection.games.Accomplishment
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.ui.common.AsyncImage
import isel.dei.pdm.demos.gamevault.ui.common.Placeholder
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * The app bar of the game details screen: its title, the back navigation and the action that
 * removes the game from the collection. The action is tinted with the theme's error color, as
 * removing the game is destructive. Asking the user to confirm it is up to the caller.
 *
 * @param onBack Callback invoked when the user asks to navigate back.
 * @param onRemove Callback invoked when the user asks to remove the game from the collection.
 * @param modifier Modifier to be applied to the app bar.
 * @param isRemoveEnabled Whether the game can be removed (e.g. not while it is being read). The
 * back navigation is always enabled.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailsTopBar(
    onBack: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    isRemoveEnabled: Boolean = true
) {
    TopAppBar(
        title = { Text(stringResource(R.string.game_details_title)) },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag(GameDetailsScreenViewTestTags.BACK_BUTTON)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.game_details_back_description)
                )
            }
        },
        actions = {
            IconButton(
                onClick = onRemove,
                enabled = isRemoveEnabled,
                modifier = Modifier.testTag(GameDetailsScreenViewTestTags.REMOVE_BUTTON)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.game_details_remove_description),
                    // When disabled, the button's disabled content color is used instead
                    tint = if (isRemoveEnabled) MaterialTheme.colorScheme.error
                    else LocalContentColor.current
                )
            }
        },
        modifier = modifier
    )
}

/**
 * The view of a game of the collection, below the app bar (see [GameDetailsTopBar]):
 * - The scrollable content, with the game's header (see [GameDetailsHeader]), its cover next to
 * its play status (see [PlayStatusPanel]), its history (see [GameHistory]), the user's notes
 * (see [GameNotes]) and its dates (see [GameDatesRow]).
 * - The play session controls (see [PlaySessionBar]), always displayed, below the content.
 *
 * The actions that are not available are disabled, as determined by the domain (see
 * [CollectionEntry.canLeavePlaying] and [CollectionEntry.canBeFinished]): while a play session is
 * active, the game cannot leave the PLAYING state, nor be finished for the first time.
 *
 * The view only emits the user's intents: how each one is carried out (e.g. which dialog is
 * displayed) is up to the caller.
 *
 * @param entry The game's entry in the collection, with the user's notes.
 * @param sessionElapsedTime The time elapsed since the active play session started, or null if
 * no session is active. Keeping it up to date is up to the caller.
 * @param onChangeState Callback invoked when the user asks to change the play state.
 * @param onEditPlayTime Callback invoked when the user asks to edit the time spent playing.
 * @param onAddRun Callback invoked when the user asks to add a completed run.
 * @param onRegisterPlatinum Callback invoked when the user asks to register (or edit) the platinum.
 * @param onEditNotes Callback invoked when the user asks to edit the notes.
 * @param onStartSession Callback invoked when the user asks to start a play session.
 * @param onStopSession Callback invoked when the user asks to stop the active play session.
 * @param modifier Modifier to be applied to the view's layout.
 * @param timeZone The time zone in which the date the game was added on is displayed.
 */
@Composable
fun GameDetailsScreenView(
    entry: CollectionEntry,
    sessionElapsedTime: Duration?,
    onChangeState: () -> Unit,
    onEditPlayTime: () -> Unit,
    onAddRun: () -> Unit,
    onRegisterPlatinum: () -> Unit,
    onEditNotes: () -> Unit,
    onStartSession: () -> Unit,
    onStopSession: () -> Unit,
    modifier: Modifier = Modifier,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
) {
    GameDetailsLayout(
        header = { GameDetailsHeader(name = entry.game.name, platforms = entry.platforms) },
        cover = { coverModifier ->
            AsyncImage(
                uri = entry.game.coverUri,
                contentDescription = entry.game.name.value,
                contentScale = ContentScale.Crop,
                modifier = coverModifier.testTag(GameDetailsScreenViewTestTags.COVER)
            )
        },
        playStatus = { panelModifier ->
            PlayStatusPanel(
                playStatus = entry.playStatus,
                onChangeState = onChangeState,
                onEditPlayTime = onEditPlayTime,
                onAddRun = onAddRun,
                onRegisterPlatinum = onRegisterPlatinum,
                isStateChangeEnabled = entry.canLeavePlaying,
                isAddRunEnabled = entry.canBeFinished,
                isRegisterPlatinumEnabled = entry.canBeFinished,
                modifier = panelModifier
            )
        },
        history = {
            GameHistory(
                completedRuns = entry.playStatus.completedRuns,
                platinum = entry.playStatus.platinum
            )
        },
        notes = { GameNotes(notes = entry.notes, onEdit = onEditNotes) },
        dates = {
            GameDatesRow(
                releaseYear = entry.game.releaseDate?.let { Year.of(it) },
                addedOn = entry.addedAt.toLocalDateTime(timeZone).date
            )
        },
        sessionBar = { barModifier ->
            PlaySessionBar(
                sessionElapsedTime = sessionElapsedTime,
                onStart = onStartSession,
                onStop = onStopSession,
                modifier = barModifier
            )
        },
        modifier = modifier,
        contentModifier = Modifier.testTag(GameDetailsScreenViewTestTags.CONTENT)
    )
}

/**
 * The view of a game of the collection while it is being read, below the app bar (see
 * [GameDetailsTopBar], whose remove action is then to be disabled): the layout of
 * [GameDetailsScreenView], as it is known in advance, with placeholders instead of the game's
 * information and all actions disabled. Displaying the layout, instead of a progress indicator,
 * makes loading almost unnoticeable, as the game is stored locally and therefore read quickly.
 *
 * Placeholders, rather than default values, are displayed, as defaults (e.g. no completed runs)
 * would be wrong information about the game until it is read. The placeholders are ignored by
 * accessibility services, for which the content is described as being loaded, while the labels
 * and the disabled actions are still announced.
 *
 * @param modifier Modifier to be applied to the view's layout.
 */
@Composable
fun GameDetailsLoadingView(modifier: Modifier = Modifier) {
    val loadingDescription = stringResource(R.string.game_details_loading)
    GameDetailsLayout(
        header = { GameDetailsHeaderPlaceholder() },
        cover = { coverModifier -> Placeholder(modifier = coverModifier) },
        playStatus = { panelModifier -> PlayStatusPanelPlaceholder(modifier = panelModifier) },
        history = { GameHistoryPlaceholder() },
        notes = { GameNotesPlaceholder() },
        dates = { GameDatesRowPlaceholder() },
        sessionBar = { barModifier -> PlaySessionBarPlaceholder(modifier = barModifier) },
        modifier = modifier,
        contentModifier = Modifier
            .semantics { contentDescription = loadingDescription }
            .testTag(GameDetailsScreenViewTestTags.LOADING)
    )
}

/**
 * The layout of the game details, shared by the view of the game and the one displayed while it
 * is being read, so that both are laid out alike: the scrollable content, with the [header], the
 * [cover] next to the [playStatus], the [history], the [notes] and the [dates], above the
 * [sessionBar], which is always displayed. The slots that take a modifier are given the one that
 * places them.
 */
@Composable
private fun GameDetailsLayout(
    header: @Composable () -> Unit,
    cover: @Composable (Modifier) -> Unit,
    playStatus: @Composable (Modifier) -> Unit,
    history: @Composable () -> Unit,
    notes: @Composable () -> Unit,
    dates: @Composable () -> Unit,
    sessionBar: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .then(contentModifier)
        ) {
            header()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                cover(
                    Modifier
                        .weight(0.45f)
                        .aspectRatio(COVER_ASPECT_RATIO)
                        .clip(RoundedCornerShape(12.dp))
                )
                playStatus(Modifier.weight(0.55f))
            }
            history()
            notes()
            dates()
        }
        sessionBar(Modifier.padding(16.dp))
    }
}

/**
 * The aspect ratio of the games' covers (width / height), as provided by the game search service.
 */
private const val COVER_ASPECT_RATIO = 264f / 374f

// Testing & preview utilities

object GameDetailsScreenViewTestTags {
    const val BACK_BUTTON = "GameDetailsBackButton"
    const val REMOVE_BUTTON = "GameDetailsRemoveButton"
    const val CONTENT = "GameDetailsContent"
    const val LOADING = "GameDetailsLoading"
    const val COVER = "GameDetailsCover"
}

@Suppress("Unused")
class GameDetailsScreenViewPreviews {

    private val eldenRing = Game(1L, "Elden Ring", LocalDate(2022, 2, 25))

    private val platforms = setOf(
        Platform(1L, "PS5", "PlayStation 5"),
        Platform(3L, "PC", "PC (Microsoft Windows)")
    )

    private val addedAt = Instant.parse("2024-03-01T10:00:00Z")

    /**
     * The view as displayed by the screen, below its app bar.
     */
    @Composable
    private fun PreviewOf(
        entry: CollectionEntry,
        sessionElapsedTime: Duration? = null
    ) {
        DemoGameVaultTheme {
            Scaffold(topBar = { GameDetailsTopBar(onBack = { }, onRemove = { }) }) { innerPadding ->
                GameDetailsScreenView(
                    entry = entry,
                    sessionElapsedTime = sessionElapsedTime,
                    onChangeState = { },
                    onEditPlayTime = { },
                    onAddRun = { },
                    onRegisterPlatinum = { },
                    onEditNotes = { },
                    onStartSession = { },
                    onStopSession = { },
                    modifier = Modifier.padding(innerPadding),
                    timeZone = TimeZone.UTC
                )
            }
        }
    }

    @Preview(showBackground = true, showSystemUi = true, name = "Loading")
    @Composable
    fun LoadingPreview() {
        DemoGameVaultTheme {
            Scaffold(
                topBar = { GameDetailsTopBar(onBack = { }, onRemove = { }, isRemoveEnabled = false) }
            ) { innerPadding ->
                GameDetailsLoadingView(modifier = Modifier.padding(innerPadding))
            }
        }
    }

    @Preview(showBackground = true, name = "Top bar")
    @Composable
    fun TopBarPreview() {
        DemoGameVaultTheme {
            GameDetailsTopBar(onBack = { }, onRemove = { })
        }
    }

    @Preview(showBackground = true, showSystemUi = true, name = "Just added")
    @Composable
    fun JustAddedPreview() = PreviewOf(
        entry = CollectionEntry(game = eldenRing, platforms = platforms, addedAt = addedAt)
    )

    @Preview(showBackground = true, showSystemUi = true, name = "Replayed, during a session")
    @Composable
    fun ReplayedDuringSessionPreview() = PreviewOf(
        entry = CollectionEntry(
            game = eldenRing,
            platforms = platforms,
            playStatus = PlayStatus(
                timeSpent = PlayTime(hours = 132, minutes = 45),
                state = PlayStatus.State.PLAYING,
                completedRuns = listOf(
                    Accomplishment(null),
                    Accomplishment(LocalDate(2024, 3, 10)),
                    Accomplishment(LocalDate(2025, 8, 2))
                ),
                platinum = Accomplishment(LocalDate(2025, 8, 2))
            ),
            addedAt = addedAt,
            sessionStartTime = Instant.parse("2026-11-18T21:05:00Z"),
            notes = NonBlankString("Stopped at Malenia. Next run: faith build.")
        ),
        sessionElapsedTime = 72.minutes
    )

    @Preview(showBackground = true, showSystemUi = true, name = "First playthrough, during a session")
    @Composable
    fun FirstPlaythroughDuringSessionPreview() = PreviewOf(
        entry = CollectionEntry(
            game = eldenRing,
            platforms = platforms,
            playStatus = PlayStatus(
                timeSpent = PlayTime(hours = 12),
                state = PlayStatus.State.PLAYING
            ),
            addedAt = addedAt,
            sessionStartTime = Instant.parse("2026-11-18T21:05:00Z")
        ),
        sessionElapsedTime = 30.minutes
    )
}
