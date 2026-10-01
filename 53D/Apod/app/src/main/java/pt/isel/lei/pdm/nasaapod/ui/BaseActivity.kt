package pt.isel.lei.pdm.nasaapod.ui

import androidx.activity.ComponentActivity
import pt.isel.lei.pdm.nasaapod.DependencyContainer

open class BaseActivity : ComponentActivity() {

    protected val dependencyContainer: DependencyContainer
        get() = application as DependencyContainer
}