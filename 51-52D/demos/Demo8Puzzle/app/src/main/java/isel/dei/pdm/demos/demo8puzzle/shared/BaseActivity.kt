package isel.dei.pdm.demos.demo8puzzle.shared

import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Base class for all activities in the app. It is used to log the lifecycle of the activities.
 */
abstract class BaseActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("onCreate() on instance ${hashCode()}")
    }

    override fun onStart() {
        super.onStart()
        log("onStart() on instance ${hashCode()}")
    }

    override fun onStop() {
        super.onStop()
        log("onStop() on instance ${hashCode()}")
    }

    override fun onDestroy() {
        super.onDestroy()
        log("onDestroy() on instance ${hashCode()}")
    }
}