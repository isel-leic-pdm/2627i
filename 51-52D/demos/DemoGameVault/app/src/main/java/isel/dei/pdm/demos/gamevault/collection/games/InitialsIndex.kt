package isel.dei.pdm.demos.gamevault.collection.games

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * The width of the initials index. Listings displaying it reserve this width at their end, so
 * that the index does not cover their items.
 */
val InitialsIndexWidth = 24.dp

/**
 * The height of each initial of the index, when there is room for it. Otherwise (e.g. in
 * landscape), the initials share the available height.
 */
private val InitialHeight = 18.dp

/**
 * A vertical index of initials (e.g. A, C, H, #), placed along the edge of a listing, to jump to
 * the section of an initial. The user may tap an initial, or drag along the index to go through
 * the initials, as in a contacts list: [onInitialSelected] is invoked whenever the initial under
 * the finger changes, but not repeatedly for the same initial while dragging.
 *
 * Only the given initials are displayed, so selecting any of them always leads somewhere. For
 * accessibility, each initial is also an individual button, labelled for jumping to its section.
 *
 * The index takes the height its initials need, unless it is not available (e.g. in landscape),
 * in which case the initials share the available height.
 *
 * @param initials The initials to be displayed, in the order they are to be displayed.
 * @param onInitialSelected Callback invoked when the user selects an initial.
 * @param modifier Modifier to be applied to the index.
 */
@Composable
fun InitialsIndex(
    initials: List<Char>,
    onInitialSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    // The gesture detector outlives recompositions, so it must use the latest callback
    val currentOnInitialSelected by rememberUpdatedState(onInitialSelected)

    Column(
        modifier = modifier
            .width(InitialsIndexWidth)
            .heightIn(max = InitialHeight * initials.size)
            .testTag(InitialsIndexTestTags.INDEX)
            .pointerInput(initials) {
                awaitEachGesture {
                    // The initial under the finger, by its vertical position. If the finger is
                    // dragged, the initial under the last position is selected
                    var lastSelected: Char? = null
                    fun selectAt(y: Float) {
                        if (initials.isEmpty()) return
                        val index = (y / size.height * initials.size)
                            .toInt()
                            .coerceIn(0, initials.lastIndex)
                        val initial = initials[index]
                        if (initial != lastSelected) {
                            lastSelected = initial
                            currentOnInitialSelected(initial)
                        }
                    }

                    // Get finger down
                    awaitFirstDown().let {
                        it.consume()
                        selectAt(it.position.y)
                    }

                    // Get finger dragged
                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            if (change.pressed) {
                                selectAt(change.position.y)
                                change.consume()
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        initials.forEach { initial ->
            val jumpLabel = stringResource(R.string.collection_index_jump_label, initial.toString())
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(InitialsIndexWidth)
                    .weight(1f)
                    .testTag(InitialsIndexTestTags.INITIAL)
                    // A single element for accessibility services: the initial and its action
                    .semantics(mergeDescendants = true) {
                        role = Role.Button
                        onClick(label = jumpLabel) {
                            currentOnInitialSelected(initial)
                            true
                        }
                    }
            ) {
                Text(
                    text = initial.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Testing & preview utilities

object InitialsIndexTestTags {
    const val INDEX = "InitialsIndex"
    const val INITIAL = "InitialsIndexInitial"
}

@Suppress("Unused")
class InitialsIndexPreviews {

    @Preview(showBackground = true, heightDp = 520, name = "All initials and some initials")
    @Composable
    fun InitialsIndexPreview() {
        DemoGameVaultTheme {
            Row(modifier = Modifier.fillMaxHeight()) {
                InitialsIndex(
                    initials = ('A'..'Z').toList() + '#',
                    onInitialSelected = {},
                    modifier = Modifier.padding(8.dp)
                )
                InitialsIndex(
                    initials = listOf('C', 'E', 'H', 'S', 'Z', '#'),
                    onInitialSelected = {},
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
