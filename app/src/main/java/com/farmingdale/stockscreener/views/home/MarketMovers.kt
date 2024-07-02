package com.farmingdale.stockscreener.views.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.farmingdale.stockscreener.R
import com.farmingdale.stockscreener.model.local.MarketMover
import com.farmingdale.stockscreener.ui.theme.negativeBackgroundColor
import com.farmingdale.stockscreener.ui.theme.negativeTextColor
import com.farmingdale.stockscreener.ui.theme.positiveBackgroundColor
import com.farmingdale.stockscreener.ui.theme.positiveTextColor
import com.farmingdale.stockscreener.utils.DataError
import com.farmingdale.stockscreener.utils.Resource
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MarketMovers(
    listState: LazyListState,
    navController: NavController,
    actives: Resource<List<MarketMover>, DataError.Network>,
    losers: Resource<List<MarketMover>, DataError.Network>,
    gainers: Resource<List<MarketMover>, DataError.Network>,
    refresh: () -> Unit,
) {
    val state = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val tabTitles = listOf(
        stringResource(id = R.string.mostActive),
        stringResource(id = R.string.topGainers),
        stringResource(id = R.string.topLosers)
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 300.dp, max = 600.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TabRow(
            selectedTabIndex = state.currentPage,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[state.currentPage]),
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = state.currentPage == index,
                    onClick = {
                        scope.launch {
                            listState.animateScrollToItem(
                                index = 3,
                            )
                            state.animateScrollToPage(
                                page = index,
                            )
                        }
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
        HorizontalPager(
            state = state,
            modifier = Modifier
                .padding(horizontal = 16.dp)
        ) { page ->
            when (page) {
                0 -> {
                    MarketMoversList(
                        stocks = actives,
                        navController = navController,
                        refresh = refresh
                    )
                }

                1 -> {
                    MarketMoversList(
                        stocks = gainers,
                        navController = navController,
                        refresh = refresh
                    )
                }

                2 -> {
                    MarketMoversList(
                        stocks = losers,
                        navController = navController,
                        refresh = refresh
                    )
                }
            }
        }
    }
}

@Composable
fun MarketMoversList(
    stocks: Resource<List<MarketMover>, DataError.Network>,
    navController: NavController,
    refresh: () -> Unit
) {
    when (stocks) {
        is Resource.Loading -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator()
            }
        }

        is Resource.Error -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ErrorCard(refresh = refresh)
            }
        }

        is Resource.Success -> {
            if (stocks.data.isEmpty()) {
                ErrorCard(refresh = refresh)
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            items = stocks.data,
                            key = { stock -> stock.symbol }
                        ) { stock ->
                            MarketMoverStock(stock = stock, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarketMoverStock(
    stock: MarketMover,
    navController: NavController
) {
    Row(
        modifier = Modifier
            .padding(top = 8.dp)
            .clickable {
                navController.navigate("stock/${stock.symbol}")
            }
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stock.symbol,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(25))
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(4.dp)
            )
            Text(
                text = stock.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Light,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = String.format(Locale.US, "%.2f", stock.price.toDouble()),
            modifier = Modifier
                .weight(1f)
                .wrapContentSize()
                .padding(4.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stock.change,
            color = let { if (stock.change.startsWith("-")) negativeTextColor else positiveTextColor },
            modifier = Modifier
                .weight(1f)
                .wrapContentSize()
                .background(
                    if (stock.change.startsWith("-")) negativeBackgroundColor else positiveBackgroundColor
                )
                .padding(4.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stock.percentChange,
            color = let { if (stock.change.startsWith("-")) negativeTextColor else positiveTextColor },
            modifier = Modifier
                .weight(1f)
                .wrapContentSize()
                .background(
                    if (stock.change.startsWith("-")) negativeBackgroundColor else positiveBackgroundColor
                )
                .padding(4.dp)
        )
    }
}

@Preview
@Composable
fun PreviewMarketMoverStock() {
    MarketMoverStock(
        stock = MarketMover(
            symbol = "AAPL",
            name = "Apple Inc.",
            price = "100.0",
            change = "+100.0",
            percentChange = "+100%"
        ),
        navController = rememberNavController()
    )
}
