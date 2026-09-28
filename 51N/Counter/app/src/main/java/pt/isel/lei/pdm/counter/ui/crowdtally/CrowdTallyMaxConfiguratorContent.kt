package pt.isel.lei.pdm.counter.ui.crowdtally

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import pt.isel.lei.pdm.counter.domain.CrowdTallyInfo
import pt.isel.lei.pdm.counter.ui.TestTags

@Composable
fun CrowdTallyMaxConfiguratorContent(
    arg: CrowdTallyInfo,
    onCapacityChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d("CrowdTallyMaxConfiguratorContent", "Recomposition")

    var newCapacity by rememberSaveable {
        mutableStateOf(arg.capacity)
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        TextField(
            textStyle = TextStyle(
                fontSize = 100.sp,
                textAlign = TextAlign.Center
            ),
            value = "${newCapacity}",
            onValueChange = { newStr ->
                val nr = newStr.toIntOrNull()
                if (nr != null) {
                    newCapacity = newStr.toInt()
                    Log.d("TextField", "${newCapacity}")
                }
            },
            modifier = Modifier.testTag(TestTags.CrowdTally.CONFIGURATOR_VALUE_TEXT)

        )

        Button(onClick = {
            onCapacityChanged(newCapacity)
        },
            modifier = Modifier.testTag(TestTags.CrowdTally.CONFIGURATOR_SAVE_BUTTON)
            ) {
            Text("Save")
        }
    }
}

@Composable
@Preview
fun CrowdTallyMaxConfiguratorContentPreview() {
    CrowdTallyMaxConfiguratorContent(
        CrowdTallyInfo(5, 10),
        {}
    )
}