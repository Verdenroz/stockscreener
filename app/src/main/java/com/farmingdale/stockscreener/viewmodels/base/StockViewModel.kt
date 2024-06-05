package com.farmingdale.stockscreener.viewmodels.base

import androidx.lifecycle.ViewModel
import com.farmingdale.stockscreener.model.local.HistoricalData
import com.farmingdale.stockscreener.model.local.Interval
import com.farmingdale.stockscreener.model.local.News
import com.farmingdale.stockscreener.model.local.TimePeriod
import kotlinx.coroutines.flow.StateFlow

abstract class StockViewModel : ViewModel() {

    abstract val timeSeries: StateFlow<Map<String, HistoricalData>>

    abstract val news: StateFlow<List<News>>

    abstract fun getTimeSeries(symbol: String, timePeriod: TimePeriod, interval: Interval)

    abstract fun getNews(symbol: String)

}