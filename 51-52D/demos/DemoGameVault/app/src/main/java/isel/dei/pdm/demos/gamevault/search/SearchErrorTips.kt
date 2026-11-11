package isel.dei.pdm.demos.gamevault.search

import androidx.annotation.StringRes
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.search.NoConnectivityException
import isel.dei.pdm.demos.gamevault.domain.search.RateLimitExceededException
import isel.dei.pdm.demos.gamevault.domain.search.SearchServiceException
import isel.dei.pdm.demos.gamevault.domain.search.ServiceUnavailableException
import isel.dei.pdm.demos.gamevault.domain.search.UnauthenticatedException
import isel.dei.pdm.demos.gamevault.domain.search.UnexpectedServiceException

/**
 * The tip displayed to the user for the given search error, hinting on what can be done about it.
 * Shared by the search screens, as the errors are those of the app's single search provider.
 */
@StringRes
fun SearchServiceException.tipResId(): Int =
    when (this) {
        is NoConnectivityException -> R.string.search_error_tip_no_connectivity
        is UnauthenticatedException -> R.string.search_error_tip_unauthenticated
        is ServiceUnavailableException -> R.string.search_error_tip_service_unavailable
        is RateLimitExceededException -> R.string.search_error_tip_rate_limit_exceeded
        is UnexpectedServiceException -> R.string.search_error_tip_unexpected
    }
