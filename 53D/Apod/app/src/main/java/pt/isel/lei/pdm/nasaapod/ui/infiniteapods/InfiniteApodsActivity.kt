package pt.isel.lei.pdm.nasaapod.ui.infiniteapods

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pt.isel.lei.pdm.nasaapod.DependencyContainer
import pt.isel.lei.pdm.nasaapod.ui.bookmarks.BookmarksActivity
import pt.isel.lei.pdm.nasaapod.ui.common.viewModelInit

class InfiniteApodsActivity : ComponentActivity() {

    companion object {
        fun navigate(ctx: Context) {
            val intent = Intent(ctx, InfiniteApodsActivity::class.java)
            ctx.startActivity(intent)
        }
    }
    val vm by viewModels<InfiniteApodsViewModel> {
        viewModelInit {
            InfiniteApodsViewModel((application as DependencyContainer).nasaApodService)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InfiniteApodsScreen(
                vm = vm,
                onBack = { finish() }
            )
        }
    }
}
