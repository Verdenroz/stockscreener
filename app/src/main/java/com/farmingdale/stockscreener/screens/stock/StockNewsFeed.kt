package com.farmingdale.stockscreener.screens.stock

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import com.farmingdale.stockscreener.R
import com.farmingdale.stockscreener.model.local.News
import com.farmingdale.stockscreener.utils.DataError
import com.farmingdale.stockscreener.utils.Resource
import com.farmingdale.stockscreener.utils.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun StockNewsFeed(
    snackbarHost: SnackbarHostState,
    news: Resource<ImmutableList<News>, DataError.Network>
) {
    val context = LocalContext.current
    when (news) {

        is Resource.Loading -> {
            StockNewsFeedSkeleton()
        }

        is Resource.Error -> {
            StockNewsFeedSkeleton()

            LaunchedEffect(news.error) {
                snackbarHost.showSnackbar(
                    message = news.error.asUiText().asString(context),
                    actionLabel = UiText.StringResource(R.string.dismiss).asString(context),
                    duration = SnackbarDuration.Short
                )
            }
        }

        is Resource.Success -> {
            if (news.data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .height(300.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.no_news),
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(100))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(
                        items = news.data,
                        key = { news -> news.link },
                    ) { item ->
                        StockNewsItem(news = item)
                    }
                }
            }
        }
    }
}


@Composable
fun StockNewsItem(news: News) {
    val context = LocalContext.current
    ListItem(
        leadingContent = {
            SubcomposeAsyncImage(
                model = news.img,
                contentDescription = stringResource(id = R.string.news_image),
                loading = {
                    Card(
                        modifier = Modifier.fillMaxSize(.33f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        // Loading
                    }
                },
                error = {
                    Card(
                        modifier = Modifier.fillMaxSize(.33f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = stringResource(id = R.string.error_loading_data)
                        )
                    }
                },
                imageLoader = ImageLoader(context),
                modifier = Modifier.fillMaxSize(.33f)
            )
        },
        headlineContent = {
            Text(
                text = news.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Row(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = news.source,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = news.time,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(news.link))
                context.startActivity(intent)
            }
    )
}

@Preview
@Composable
fun Preview() {
    StockNewsFeed(
        snackbarHost = SnackbarHostState(),
        news = Resource.Success(
            persistentListOf(
                News(
                    title = "Title",
                    link = "https://www.google.com",
                    source = "Source",
                    time = "Time",
                    img = "img"
                ),
                News(
                    title = "Title",
                    link = "https://www.google.com",
                    source = "Source",
                    time = "Time",
                    img = "img"
                )
            )
        )
    )
}

@Composable
fun StockNewsFeedSkeleton(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        repeat(5) {
            item(key = it) {
                ListItem(
                    headlineContent = { /*skeleton*/ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color)
                )
            }
        }
    }
}