package pt.isel.lei.pdm.counter.ui.crowdtally

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import pt.isel.lei.pdm.counter.domain.CrowdTallyInfo

@Composable
fun CrowdTallyView(
    state: CrowdTallyInfo,
    increment: () -> Unit,
    decrement: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {

        Button(
            onClick = decrement,
            enabled = state.canDecrement
        )
        {
            Text("\uD83D\uDC47")
        }

        Text(
            text = state.counter.toString(),
            modifier = Modifier.testTag(TestTags.CrowdTally.CounterText)
        )

        Button(
            onClick = increment,
            enabled = state.canIncrement,
            modifier = Modifier.testTag(TestTags.CrowdTally.IncrementButton)
        )
        {
            Text("☝\uFE0F")
        }
    }
}