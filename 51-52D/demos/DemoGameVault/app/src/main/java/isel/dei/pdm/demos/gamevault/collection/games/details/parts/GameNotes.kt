package isel.dei.pdm.demos.gamevault.collection.games.details.parts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.ui.common.TextPlaceholder
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * The user's notes about a game (e.g. where they stopped), as displayed on its details screen,
 * below a divider, with the action that edits them.
 *
 * The section is displayed even without notes, with a placeholder instead, so that they can be
 * added. How they are edited (e.g. which dialog is displayed) is up to the caller.
 *
 * @param notes The user's notes, or null if there are none.
 * @param onEdit Callback invoked when the user asks to edit the notes.
 * @param modifier Modifier to be applied to the section's layout.
 */
@Composable
fun GameNotes(
    notes: NonBlankString?,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider()
        NotesTitle(onEdit = onEdit)
        if (notes != null) {
            Text(
                text = notes.value,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.testTag(GameNotesTestTags.NOTES)
            )
        } else {
            Text(
                text = stringResource(R.string.game_details_notes_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag(GameNotesTestTags.EMPTY)
            )
        }
    }
}

/**
 * The notes while the game is being read: the divider and the title, with the edit action
 * disabled, above a placeholder instead of the notes.
 *
 * @param modifier Modifier to be applied to the section's layout.
 */
@Composable
fun GameNotesPlaceholder(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider()
        NotesTitle(onEdit = { }, isEditEnabled = false)
        TextPlaceholder(style = MaterialTheme.typography.bodyLarge, width = 240.dp)
    }
}

/**
 * The title of the section, with the action that edits the notes, at the end.
 */
@Composable
private fun NotesTitle(onEdit: () -> Unit, isEditEnabled: Boolean = true) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.game_details_notes_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onEdit,
            enabled = isEditEnabled,
            modifier = Modifier.testTag(GameNotesTestTags.EDIT_BUTTON)
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(R.string.game_details_notes_edit_description)
            )
        }
    }
}

// Testing & preview utilities

object GameNotesTestTags {
    const val NOTES = "GameNotesNotes"
    const val EMPTY = "GameNotesEmpty"
    const val EDIT_BUTTON = "GameNotesEditButton"
}

@Suppress("Unused")
class GameNotesPreviews {

    @Preview(showBackground = true, name = "With notes")
    @Composable
    fun WithNotesPreview() {
        DemoGameVaultTheme {
            GameNotes(
                notes = NonBlankString("Stopped at Malenia. Next run: faith build."),
                onEdit = { },
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    @Preview(showBackground = true, name = "Without notes")
    @Composable
    fun WithoutNotesPreview() {
        DemoGameVaultTheme {
            GameNotes(notes = null, onEdit = { }, modifier = Modifier.padding(16.dp))
        }
    }
}
