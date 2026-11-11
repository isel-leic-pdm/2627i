package isel.dei.pdm.demos.gamevault.search.platforms

import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo
import isel.dei.pdm.demos.gamevault.ui.common.AddStatus
import isel.dei.pdm.demos.gamevault.ui.common.PlatformListItemMode
import isel.dei.pdm.demos.gamevault.ui.common.PlatformListItemModel
import isel.dei.pdm.demos.gamevault.ui.common.toListItemModel

/**
 * Represents the UI state for a platform item in the platform search list.
 *
 * @property platform The platform information obtained from the search service.
 * @property addStatus The status of adding the platform to the user's collection.
 */
data class PlatformSearchItemUiState(
    val platform: PlatformInfo,
    val addStatus: AddStatus = AddStatus.NOT_ADDED
) {
    /**
     * The key that uniquely identifies the item in the search results. The platform's id is not
     * enough, because all versions of the same platform share it.
     */
    val key: String = "${platform.id}/${platform.version?.value.orEmpty()}"

    /**
     * Converts the platform search item to a list item model.
     */
    fun toListItemModel(): PlatformListItemModel = platform.toListItemModel(
        mode = when (addStatus) {
            AddStatus.NOT_ADDED -> PlatformListItemMode.ADDABLE
            AddStatus.ADDING -> PlatformListItemMode.ADDING
            AddStatus.ADDED -> PlatformListItemMode.ADDED
        }
    )
}
