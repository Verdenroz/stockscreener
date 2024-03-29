package com.farmingdale.stockscreener.views

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.imageLoader
import coil.request.ImageRequest
import com.farmingdale.stockscreener.R
import com.farmingdale.stockscreener.model.local.news.Article
import com.farmingdale.stockscreener.model.local.news.Category
import com.farmingdale.stockscreener.model.local.news.News

@Composable
fun NewsFeed(
    news: News?,
    preferredCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    refresh: () -> Unit,
) {
    var isNewsSettingsOpen by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.news),
                style = MaterialTheme.typography.titleSmall
            )
            IconButton(onClick = { isNewsSettingsOpen = true }) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = stringResource(id = R.string.news_settings)
                )
            }
        }
        if (news?.articles.isNullOrEmpty()) {
            ErrorCard(refresh = refresh)
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = {
                    news?.articles?.forEach { article ->
                        item {
                            ContentCard(article)
                        }
                    }
                }
            )
        }
        if (isNewsSettingsOpen) {
            Dialog(onDismissRequest = { isNewsSettingsOpen = false }) {
                NewsSettingsDialog(
                    onDismissRequest = { isNewsSettingsOpen = false },
                    categories = Category.entries,
                    preferredCategory = rememberSaveable {
                        mutableStateOf(
                            preferredCategory ?: Category.BUSINESS
                        )
                    },
                    onCategorySelected = onCategorySelected,
                    refresh = refresh
                )
            }
        }
    }

}

@Preview
@Composable
fun PreviewNewsFeed() {
    NewsFeed(
        news = null,
        preferredCategory = Category.GENERAL,
        onCategorySelected = {},
        refresh = {}
    )
}

@Composable
fun ContentCard(
    article: Article?
) {
    val context = LocalContext.current
    var image by remember { mutableStateOf<ImageBitmap?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(article!!.urlToImage) {
        loading = true
        error = false
        val request = ImageRequest.Builder(context)
            .data(article.urlToImage)
            .build()
        val result = (context.imageLoader.execute(request).drawable as? BitmapDrawable)?.bitmap
        image = result?.asImageBitmap()
        loading = false
        error = image == null
    }

    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .size(300.dp, 150.dp)
            .shadow(1.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    val articleUrl = article.url
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl))
                    context.startActivity(intent)
                },
            shape = RoundedCornerShape(10),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 5,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth(.4f)
                        .padding(8.dp)
                )
                when {
                    loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterVertically))
                    }

                    error -> {
                        Icon(Icons.Default.Warning, contentDescription = null)
                    }

                    else -> {
                        Image(
                            bitmap = image!!,
                            contentDescription = null,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewsSettingsDialog(
    onDismissRequest: () -> Unit,
    categories: List<Category>,
    preferredCategory: MutableState<Category>,
    onCategorySelected: (Category) -> Unit,
    refresh: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = stringResource(id = R.string.news_category))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(.5f)
                    ) {
                        items(categories.take(3)) { category ->
                            Row(
                                Modifier
                                    .clickable { preferredCategory.value = category },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy((-6).dp)
                            ) {
                                Box(modifier = Modifier.size(48.dp)) {
                                    RadioButton(
                                        selected = preferredCategory.value == category,
                                        onClick = { preferredCategory.value = category }
                                    )
                                }
                                Text(
                                    text = category.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.weight(.5f)
                    ) {
                        items(categories.takeLast(3)) { category ->
                            Row(
                                Modifier
                                    .clickable { preferredCategory.value = category },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy((-6).dp)
                            ) {
                                Box(modifier = Modifier.size(48.dp)) {
                                    RadioButton(
                                        selected = preferredCategory.value == category,
                                        onClick = { preferredCategory.value = category }
                                    )
                                }
                                Text(
                                    text = category.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(4.dp))
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    TextButton(onClick = {
                        onCategorySelected(preferredCategory.value)
                        refresh()
                        onDismissRequest()
                    }
                    ) {
                        Text(text = stringResource(id = R.string.save))
                    }
                }
            }

        }
    }
}