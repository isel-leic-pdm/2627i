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
import androidx.compose.runtime.remember
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
 * @param onQueryChange Callback to be invoked when the search query changes.
 * @param onQueryCleared Callback to be invoked when the search query is cleared.
 * @param modifier Modifier to be applied to the search box.
 * @param debounceDelay Delay to apply before invoking the composable's callbacks.
 */
@Composable
fun SearchBox(
    onQueryChange: (SearchString) -> Unit,
    onQueryCleared: () -> Unit,
    modifier: Modifier = Modifier,
    debounceDelay: Duration = DEBOUNCE_DELAY,
) {
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            delay(debounceDelay)
            searchQuery.toSearchStringOrNull()?.let { searchString ->
                onQueryChange(searchString)
            }
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
                        onQueryCleared()
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
