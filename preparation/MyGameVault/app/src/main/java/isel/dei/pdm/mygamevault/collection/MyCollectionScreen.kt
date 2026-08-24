package isel.dei.pdm.mygamevault.collection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

internal const val MY_COLLECTION_SCREEN_TAG = "MyCollectionScreen"

@Composable
fun MyCollectionScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize().testTag(MY_COLLECTION_SCREEN_TAG)
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "My Collection (Coming Soon)",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
