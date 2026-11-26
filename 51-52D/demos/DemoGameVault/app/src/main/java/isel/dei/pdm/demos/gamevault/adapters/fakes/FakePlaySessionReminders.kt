package isel.dei.pdm.demos.gamevault.adapters.fakes

import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.sessions.PlaySessionReminders

/**
 * A fake implementation of the [PlaySessionReminders] interface, which only records the scheduled
 * reminder. We use it in previews, where nothing is to be scheduled, and in tests.
 */
class FakePlaySessionReminders : PlaySessionReminders {

    /**
     * The entry whose session's reminder is scheduled, or null if none is.
     */
    var scheduled: CollectionEntry? = null
        private set

    override fun schedule(entry: CollectionEntry) {
        if (entry.isSessionActive)
            scheduled = entry
    }

    override fun cancel() {
        scheduled = null
    }
}
