package com.farmingdale.stockscreener.model.local

import androidx.compose.runtime.Immutable

/**
 * Local data class for market movers
 * @param symbol Stock symbol
 * @param name Stock name
 * @param price Current stock price
 * @param change Change in stock price
 * @param percentChange Percent change in stock price
 */
@Immutable
data class MarketMover(
    val symbol: String,
    val name: String,
    val price: String,
    val change: String,
    val percentChange: String
)
