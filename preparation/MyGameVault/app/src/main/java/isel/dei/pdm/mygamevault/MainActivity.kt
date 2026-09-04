package isel.dei.pdm.mygamevault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import isel.dei.pdm.mygamevault.ui.AppScaffold
import isel.dei.pdm.mygamevault.ui.common.LocalImageLoader
import isel.dei.pdm.mygamevault.ui.theme.MyGameVaultTheme

class MainActivity : ComponentActivity() {

    private val dependencies by lazy { application as DependenciesContainer }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(LocalImageLoader provides dependencies.imageLoader) {
                MyGameVaultTheme {
                    AppScaffold(dependencies = dependencies)
                }
            }
        }
    }
}
