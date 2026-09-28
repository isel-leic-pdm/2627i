package pt.isel.lei.pdm.counter.ui.crowdtally

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.lei.pdm.counter.domain.CrowdTallyInfo
import pt.isel.lei.pdm.counter.ui.TestTags

@Composable
fun CrowdTallyContent(
    state: CrowdTallyInfo,
    increment: () -> Unit,
    decrement: () -> Unit,
    modifier: Modifier = Modifier.Companion

) {
    Row(
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Button(
            enabled = state.canDecrement,
            onClick = decrement,
            modifier = Modifier
                .width(100.dp)
                .height(100.dp)
                .testTag(TestTags.CrowdTally.DECREMENT_BUTTON)
        ) {
            Text(
                text = "\uD83D\uDC4E",
                fontSize = 50.sp
            )
        }

        Text(
            text = "${state.count}",
            fontSize = 60.sp,
            modifier = Modifier
                .padding(12.dp)
                .testTag(TestTags.CrowdTally.COUNTER_TEXT)

        )
        Button(
            enabled = state.canIncrement,
            onClick = {
                increment()
                Log.d("CrowdTallyScreen", "${state.count}")
            },
            modifier = Modifier
                .width(100.dp)
                .height(100.dp)
                .testTag(TestTags.CrowdTally.INCREMENT_BUTTON)

        ) {
            Text(
                text = "\uD83D\uDC4D",
                fontSize = 50.sp
            )
        }
    }
}

@Composable
@Preview
fun CrowdTallyScreenPreview() {
    CrowdTallyScreen()
}