package isel.dei.pdm.demos.gamevault.domain

/**
 * The result of a query whose number of results is limited, instead of paginated.
 * @property items The results, at most as many as the limit.
 * @property hasMore Whether there are more results than the limit, in which case the user should
 * be advised to refine the query.
 */
data class LimitedResult<T>(
    val items: List<T>,
    val hasMore: Boolean
)
