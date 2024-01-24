package com.farmingdale.stockscreener.model.local

data class QuoteData(
    val symbol: String,
    val open: String,
    val high: String,
    val low: String,
    val price: String,
    val volume: Int,
    val latestTradingDay: String,
    val previousClose: String,
    val change: String,
    val changePercent: String
)