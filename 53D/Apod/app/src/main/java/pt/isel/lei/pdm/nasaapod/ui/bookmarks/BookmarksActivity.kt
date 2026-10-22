package pt.isel.lei.pdm.nasaapod.ui.bookmarks

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import pt.isel.lei.pdm.nasaapod.DependencyContainer
import pt.isel.lei.pdm.nasaapod.ui.common.viewModelInit

class BookmarksActivity : ComponentActivity() {

    companion object {
        fun navigate(ctx: Context) {
            val intent = Intent(ctx, BookmarksActivity::class.java)
            ctx.startActivity(intent)
        }
    }

    val vm by viewModels<BookmarksViewModel> {
        viewModelInit {
            BookmarksViewModel((application as DependencyContainer).bookmarksRepository)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookmarksScreen(
                vm,
                backButtonClicked = { finish() }
            )
        }
    }
}
