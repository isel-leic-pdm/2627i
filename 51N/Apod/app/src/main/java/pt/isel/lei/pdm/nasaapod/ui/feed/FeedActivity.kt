package pt.isel.lei.pdm.nasaapod.ui.feed

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import pt.isel.lei.pdm.nasaapod.DependencyContainer
import pt.isel.lei.pdm.nasaapod.ui.bookmarks.BookmarksActivity
import pt.isel.lei.pdm.nasaapod.ui.common.viewModelInit

class FeedActivity : ComponentActivity() {

    companion object {
        fun navigate(ctx: Context) {
            val intent = android.content.Intent(ctx, FeedActivity::class.java)
            ctx.startActivity(intent)
        }
    }

    val vm by viewModels<FeedViewModel> {
        viewModelInit {
            FeedViewModel((application as DependencyContainer).nasaService)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FeedScreen(vm, onBack = { finish() })
        }
    }
}
