package isel.dei.pdm.demos.gamevault.search.platforms

import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo
import isel.dei.pdm.demos.gamevault.ui.common.PlatformListItemMode
import isel.dei.pdm.demos.gamevault.ui.common.PlatformListItemModel

/**
 * Represents the addition status of a platform in the search list.
 */
enum class PlatformAddStatus {
    NOT_ADDED,
    ADDING,
    ADDED
}

/**
 * Represents the UI state for a platform item in the platform search list.
 *
 * @property platform The platform information obtained from the search service.
 * @property addStatus The status of adding the platform to the user's collection.
 */
data class PlatformSearchItemUiState(
    val platform: PlatformInfo,
    val addStatus: PlatformAddStatus = PlatformAddStatus.NOT_ADDED
) {
    val isAdded: Boolean
        get() = addStatus == PlatformAddStatus.ADDED

    val isAdding: Boolean
        get() = addStatus == PlatformAddStatus.ADDING

    fun toListItemModel(): PlatformListItemModel {
        val mode = when (addStatus) {
            PlatformAddStatus.NOT_ADDED -> PlatformListItemMode.ADDABLE
            PlatformAddStatus.ADDING -> PlatformListItemMode.ADDING
            PlatformAddStatus.ADDED -> PlatformListItemMode.ADDED
        }
        return PlatformListItemModel(
            id = platform.id,
            abbreviation = platform.abbreviation,
            name = platform.name,
            logoUri = platform.logoUri,
            mode = mode
        )
    }
}
