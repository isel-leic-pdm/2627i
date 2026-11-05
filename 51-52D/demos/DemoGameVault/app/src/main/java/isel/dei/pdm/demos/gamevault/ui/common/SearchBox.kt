package isel.dei.pdm.demos.gamevault.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.toSearchStringOrNull
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Default debounce delay for the search box.
 */
private val DEBOUNCE_DELAY = 300.milliseconds

/**
 * Composable for the search boxes used in the app. The callbacks are debounced to avoid
 * multiple calls while the user is typing.
 *
 * Events are only emitted when they change what was last emitted:
 * - [onQueryChange] is emitted when the text holds a valid [SearchString] (leading and trailing
 * whitespace is ignored) that differs from the last emitted query.
 * - [onQueryCleared] is emitted when the last emitted event was a query and the text stops
 * holding a valid [SearchString] (e.g. because the user deleted it), or the clear button is
 * pressed. The latter is emitted immediately, without debounce.
 *
 * The last emitted event survives configuration changes and process death, so it is not emitted
 * again when the composable is recreated. Callers that need to survive the same events (e.g.
 * view models) must therefore preserve the current query themselves (e.g. in a SavedStateHandle).
 * Retrying the same query must also be provided by the caller, because retyping it emits nothing.
 *
 * If the search box leaves the composition (e.g. because the screen displays something else in
 * its place), its state is lost. When it comes back, the caller can restore the current query
 * through [initialQuery].
 *
 * @param onQueryChange Callback to be invoked when the search query changes.
 * @param onQueryCleared Callback to be invoked when the search query is cleared.
 * @param modifier Modifier to be applied to the search box.
 * @param initialQuery The query displayed when the search box enters the composition, if any. It
 * is considered as already emitted, so it is not emitted again. Later changes are ignored.
 * @param debounceDelay Delay to apply before invoking the composable's callbacks.
 */
@Composable
fun SearchBox(
    onQueryChange: (SearchString) -> Unit,
    onQueryCleared: () -> Unit,
    modifier: Modifier = Modifier,
    initialQuery: SearchString? = null,
    debounceDelay: Duration = DEBOUNCE_DELAY,
) {
    var searchQuery by rememberSaveable { mutableStateOf(initialQuery?.value.orEmpty()) }

    // The last emitted query, or null if the last emitted event was a clear (or none was emitted).
    // Trimmed, as emitted queries are, so that the initial query is not considered a new one
    var lastEmitted by rememberSaveable { mutableStateOf(initialQuery?.value?.trim()) }

    LaunchedEffect(searchQuery) {
        val searchString = searchQuery.trim().toSearchStringOrNull()
        if (searchString != null) {
            if (searchString.value != lastEmitted) {
                delay(debounceDelay)
                lastEmitted = searchString.value
                onQueryChange(searchString)
            }
        } else if (lastEmitted != null) {
            delay(debounceDelay)
            lastEmitted = null
            onQueryCleared()
        }
    }

    OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag(SearchBoxTestTags.INPUT),
        placeholder = { Text(stringResource(R.string.search_box_placeholder)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search_box_placeholder)
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = {
                        searchQuery = ""
                        if (lastEmitted != null) {
                            lastEmitted = null
                            onQueryCleared()
                        }
                    },
                    modifier = Modifier.testTag(SearchBoxTestTags.CLEAR_BUTTON)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear"
                    )
                }
            }
        },
        shape = MaterialTheme.shapes.medium,
        singleLine = true
    )
}

// Testing & preview utilities

object SearchBoxTestTags {
    const val INPUT = "search_box_input"
    const val CLEAR_BUTTON = "search_box_clear_button"
}

@Suppress("Unused")
class SearchBoxPreviews {
    @Preview(showBackground = true)
    @Composable
    fun Preview() {
        DemoGameVaultTheme {
            SearchBox(
                onQueryChange = {},
                onQueryCleared = {}
            )
        }
    }
}
