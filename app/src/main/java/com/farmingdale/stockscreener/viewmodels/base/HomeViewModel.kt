package com.farmingdale.stockscreener.viewmodels.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.farmingdale.stockscreener.model.local.MarketIndex
import com.farmingdale.stockscreener.model.local.MarketMover
import com.farmingdale.stockscreener.model.local.MarketSector
import com.farmingdale.stockscreener.model.local.News
import com.farmingdale.stockscreener.utils.DataError
import com.farmingdale.stockscreener.utils.Resource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow

abstract class HomeViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * Check if the device is connected to the internet
     */
    abstract val isNetworkConnected: StateFlow<Boolean>

    /**
     * Current [News] headlines displayed on the home screen
     */
    abstract val news: StateFlow<Resource<ImmutableList<News>, DataError.Network>>

    /**
     * The list of market indices as [MarketIndex]
     */
    abstract val indices: StateFlow<Resource<ImmutableList<MarketIndex>, DataError.Network>>

    /**
     * List of market sectors as [MarketSector]
     */
    abstract val sectors: StateFlow<Resource<ImmutableList<MarketSector>, DataError.Network>>

    /**
     * List of active stocks as [MarketMover]
     */
    abstract val actives: StateFlow<Resource<ImmutableList<MarketMover>, DataError.Network>>

    /**
     * List of losing stocks as [MarketMover]
     */
    abstract val gainers: StateFlow<Resource<ImmutableList<MarketMover>, DataError.Network>>

    /**
     * List of gaining stocks as [MarketMover]
     */
    abstract val losers: StateFlow<Resource<ImmutableList<MarketMover>, DataError.Network>>

    /**
     * Refresh the home screen with new data
     */
    abstract fun refresh()

}