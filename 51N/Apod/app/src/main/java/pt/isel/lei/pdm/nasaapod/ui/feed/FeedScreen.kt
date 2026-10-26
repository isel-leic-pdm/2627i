package pt.isel.lei.pdm.nasaapod.ui.feed

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.delay
import pt.isel.lei.pdm.nasaapod.R
import pt.isel.lei.pdm.nasaapod.ui.common.ApodImageView
import pt.isel.lei.pdm.nasaapod.ui.common.NavigationActions
import pt.isel.lei.pdm.nasaapod.ui.common.ScreenBase

@Composable
fun FeedScreen(vm: FeedViewModel, onBack: () -> Unit) = ScreenBase(
    actions = NavigationActions(
        backAction = onBack
    )
) {
    val viewState = vm.screenState.collectAsStateWithLifecycle().value

    Log.d("FeedScreen", viewState.toString())
    when (val state = viewState) {
        is FeedScreenState.Idle -> {
            Text(text = stringResource(R.string.no_content))
        }

        is FeedScreenState.Loading -> {
            CircularProgressIndicator()
        }

        is FeedScreenState.Error -> {
            Text(text = state.exception.toString())

        }

        is FeedScreenState.Content -> {
            val images = state.images

            val listState = rememberLazyListState()


            val shouldLoadMore = remember {
                derivedStateOf {
                    val layoutInfo = listState.layoutInfo
                    val totalItemsNumber = layoutInfo.totalItemsCount
                    val lastVisibleItemIndex =
                        layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

                    // Trigger when the user is within 2 items of the end, and we aren't already loading
                    lastVisibleItemIndex >= (totalItemsNumber - 3) && totalItemsNumber > 0
                }
            }

            // Observe changes to shouldLoadMore and trigger the callback
            LaunchedEffect(shouldLoadMore.value) {
                if (shouldLoadMore.value && state.isLoading == false) {
                    Log.d("Load", "Lore more")
                    vm.loadMore()
                }
            }

            Column() {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f)
                ) {
                    items(images) { image ->
                        ApodImageView(image)
                    }
                    if (state.isLoading)
                        item {
                            CircularProgressIndicator()
                        }

                }
                /*
                if (state.isLoading == false)
                    Button(onClick = { vm.loadMore() }) {
                        Text(text = stringResource(R.string.load_more))
                    }*/
            }

        }

        is FeedScreenState.AutoContent ->{
            val images = viewState.images.collectAsLazyPagingItems()
            LazyColumn() {
                items(images.itemCount) { index ->
                    val image = images[index]!!
                    ApodImageView(image)
                }

                if (images.loadState.refresh is LoadState.Loading ||
                    images.loadState.append is LoadState.Loading
                ) {
                    item {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}