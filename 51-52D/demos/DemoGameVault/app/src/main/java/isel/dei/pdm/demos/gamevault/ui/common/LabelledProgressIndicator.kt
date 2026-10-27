package isel.dei.pdm.demos.gamevault.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

@Composable
fun LabelledProgressIndicator(
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.testTag(LabelledProgressIndicatorTestTags.INDICATOR)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag(LabelledProgressIndicatorTestTags.TEXT)
        )
    }
}

// Testing & preview utilities

object LabelledProgressIndicatorTestTags {
    const val INDICATOR = "LoadingIndicator"
    const val TEXT = "LoadingText"
}

@Preview(showBackground = true)
@Composable
fun LabelledProgressIndicatorPreview() {
    DemoGameVaultTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LabelledProgressIndicator(text = "Loading settings…")
        }
    }
}
