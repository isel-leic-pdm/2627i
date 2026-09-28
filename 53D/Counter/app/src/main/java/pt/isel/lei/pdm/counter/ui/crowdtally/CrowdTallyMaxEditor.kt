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
import androidx.compose.ui.Modifier.Companion
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import org.w3c.dom.Text
import pt.isel.lei.pdm.counter.domain.CrowdTallyInfo
import pt.isel.lei.pdm.counter.domain.changeMax

@Composable
fun CrowdTallyMaxEditor(
    arg: CrowdTallyInfo,
    onNewMax: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d("CrowdTallyMaxEditor", "Recomposition")
    var state by rememberSaveable { mutableStateOf(arg.maxCounter) }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        TextField(
            value = "${state}",
            onValueChange = { newNumber ->
                val number = newNumber.toIntOrNull()
                if (number != null) {
                    Log.d("TextField", "$newNumber")
                    state = number
                }
            },
            textStyle = TextStyle(
                fontSize = 100.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.testTag(TestTags.CrowdTally.EditorTextBox)
        )

        Button(
            onClick = {
                onNewMax(state)
            },
            modifier = Modifier.testTag(TestTags.CrowdTally.EditorSaveButton)
        ) {
            Text("Save")
        }
    }

}