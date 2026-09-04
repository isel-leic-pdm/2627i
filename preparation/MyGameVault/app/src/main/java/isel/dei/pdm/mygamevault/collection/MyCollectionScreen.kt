package isel.dei.pdm.mygamevault.collection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import isel.dei.pdm.mygamevault.R
import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.ui.common.FatalErrorView

@Composable
fun MyCollectionScreen(
    viewModel: MyCollectionViewModel,
    onEntrySelected: (CollectionEntry) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.error != null) {
        FatalErrorView(
            message = stringResource(R.string.error_storage_access),
            buttonText = stringResource(R.string.retry_label),
            onButtonClicked = { viewModel.onFilterChange(state.filter) } // Retry
        )
    } else {
        MyCollectionScreenView(
            state = state,
            onEntrySelected = onEntrySelected,
            onFilterChange = viewModel::onFilterChange,
        )
    }
}
