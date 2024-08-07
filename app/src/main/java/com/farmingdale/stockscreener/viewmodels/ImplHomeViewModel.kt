package com.farmingdale.stockscreener.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.farmingdale.stockscreener.model.local.MarketIndex
import com.farmingdale.stockscreener.model.local.MarketMover
import com.farmingdale.stockscreener.model.local.MarketSector
import com.farmingdale.stockscreener.model.local.News
import com.farmingdale.stockscreener.repos.ImplFinanceQueryRepository.Companion.get
import com.farmingdale.stockscreener.repos.ImplWatchlistRepository.Companion.get
import com.farmingdale.stockscreener.repos.base.FinanceQueryRepository
import com.farmingdale.stockscreener.repos.base.WatchlistRepository
import com.farmingdale.stockscreener.utils.DataError
import com.farmingdale.stockscreener.utils.NetworkConnectionManager
import com.farmingdale.stockscreener.utils.NetworkConnectionManagerImpl.Companion.get
import com.farmingdale.stockscreener.utils.Resource
import com.farmingdale.stockscreener.viewmodels.base.HomeViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ImplHomeViewModel(application: Application) : HomeViewModel(application) {
    private val financeQueryRepo = FinanceQueryRepository.get()
    private val watchlistRepo = WatchlistRepository.get(application)
    private val connectionManager = NetworkConnectionManager.get(application)

    override val isNetworkConnected: StateFlow<Boolean> = connectionManager.isNetworkConnectedFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        true
    )

    override val news: StateFlow<Resource<ImmutableList<News>, DataError.Network>> = financeQueryRepo.headlines.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        Resource.Loading(true)
    )

    override val indices: StateFlow<Resource<ImmutableList<MarketIndex>, DataError.Network>> = financeQueryRepo.indices.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        Resource.Loading(true)
    )

    override val sectors: StateFlow<Resource<ImmutableList<MarketSector>, DataError.Network>> =
        financeQueryRepo.sectors.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            Resource.Loading(true)
        )

    override val actives: StateFlow<Resource<ImmutableList<MarketMover>, DataError.Network>> = financeQueryRepo.actives.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        Resource.Loading(true)
    )

    override val losers: StateFlow<Resource<ImmutableList<MarketMover>, DataError.Network>> = financeQueryRepo.losers.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        Resource.Loading(true)
    )

    override val gainers: StateFlow<Resource<ImmutableList<MarketMover>, DataError.Network>> = financeQueryRepo.gainers.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        Resource.Loading(true)
    )

    override fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val refreshDataDeferred = async { financeQueryRepo.refreshMarketData() }
            val refreshNewsDeferred = async { financeQueryRepo.refreshNews() }
            val refreshWatchlistDeferred = async { watchlistRepo.refreshWatchList() }
            val refreshSectorsDeferred = async { financeQueryRepo.refreshSectors() }

            refreshDataDeferred.await()
            refreshNewsDeferred.await()
            refreshWatchlistDeferred.await()
            refreshSectorsDeferred.await()
        }
    }
}