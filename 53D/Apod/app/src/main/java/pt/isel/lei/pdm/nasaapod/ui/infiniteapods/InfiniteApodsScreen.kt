package pt.isel.lei.pdm.nasaapod.ui.infiniteapods

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.delay
import pt.isel.lei.pdm.nasaapod.R
import pt.isel.lei.pdm.nasaapod.ui.common.ApodImageView
import pt.isel.lei.pdm.nasaapod.ui.common.ScreenBase

@Composable
fun InfiniteApodsScreen(
    vm: InfiniteApodsViewModel,
    onBack: () -> Unit
) = ScreenBase(onBack = onBack) {

    val screenState = vm.screenState
        .collectAsStateWithLifecycle().value

    when (val state = screenState) {
        is InfiniteApodsScreenState.Idle -> {
            Text(text = stringResource(R.string.app_no_image))
        }

        is InfiniteApodsScreenState.Error -> {
            Text(text = state.error.toString())
        }

        is InfiniteApodsScreenState.Content -> {
            val images = state.images

            if (state.isLoading && state.images.count() == 0) {
                CircularProgressIndicator()
                return@ScreenBase
            }


            val listState = rememberLazyListState()


            val shouldLoadMore = remember {
                derivedStateOf {
                    val layoutInfo = listState.layoutInfo
                    val totalItemsNumber = layoutInfo.totalItemsCount
                    val lastVisibleItemIndex =
                        layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

                    // Trigger when the user is within 2 items of the end, and we aren't already loading
                    lastVisibleItemIndex >= (totalItemsNumber - 2) && totalItemsNumber > 0
                }
            }

            LaunchedEffect(shouldLoadMore.value) {
                if (shouldLoadMore.value && state.isLoading == false)
                    vm.loadmore()
            }

            Column() {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f)
                ) {
                    items(images) { image ->
                        ApodImageView(image)
                    }

                    if (state.isLoading) {
                        item {
                            CircularProgressIndicator()
                        }
                    }
                }
                /*
                Button(onClick = { vm.loadmore() }) {
                    Text(text = stringResource(R.string.load_more))
                }*/
            }

        }

        is InfiniteApodsScreenState.AutoContent ->{
            val images = state.images.collectAsLazyPagingItems()
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