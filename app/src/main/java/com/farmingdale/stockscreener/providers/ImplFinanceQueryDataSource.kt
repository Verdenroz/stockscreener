package com.farmingdale.stockscreener.providers

import com.farmingdale.stockscreener.BuildConfig
import com.farmingdale.stockscreener.model.local.Analysis
import com.farmingdale.stockscreener.model.local.FullQuoteData
import com.farmingdale.stockscreener.model.local.HistoricalData
import com.farmingdale.stockscreener.model.local.Interval
import com.farmingdale.stockscreener.model.local.MarketIndex
import com.farmingdale.stockscreener.model.local.MarketMover
import com.farmingdale.stockscreener.model.local.MarketSector
import com.farmingdale.stockscreener.model.local.News
import com.farmingdale.stockscreener.model.local.SimpleQuoteData
import com.farmingdale.stockscreener.model.local.TimePeriod
import com.farmingdale.stockscreener.model.remote.AnalysisResponse
import com.farmingdale.stockscreener.model.remote.FullQuoteResponse
import com.farmingdale.stockscreener.model.remote.IndexResponse
import com.farmingdale.stockscreener.model.remote.MarketMoverResponse
import com.farmingdale.stockscreener.model.remote.NewsResponse
import com.farmingdale.stockscreener.model.remote.SectorResponse
import com.farmingdale.stockscreener.model.remote.SimpleQuoteResponse
import com.farmingdale.stockscreener.model.remote.TimeSeriesResponse
import com.farmingdale.stockscreener.providers.base.FinanceQueryDataSource
import com.farmingdale.stockscreener.utils.FINANCE_QUERY_API_URL
import com.farmingdale.stockscreener.utils.HttpException
import com.farmingdale.stockscreener.utils.NetworkException
import com.farmingdale.stockscreener.utils.executeAsync
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalSerializationApi::class)
class ImplFinanceQueryDataSource(private val client: OkHttpClient) : FinanceQueryDataSource {

    companion object {
        private val parser: Json by lazy {
            Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
        }
    }

    private suspend fun getByteStream(url: HttpUrl): InputStream {
        val request = Request.Builder()
            .url(url)
            .addHeader("x-api-key", BuildConfig.financeQueryAPIKey)
            .build()
        val call = client.newCall(request)
        try {
            val response = call.executeAsync()
            if (!response.isSuccessful) {
                throw HttpException(code=response.code)
            }
            return response.body!!.byteStream()
        } catch (e: UnknownHostException) {
            throw NetworkException(e)
        } catch (e: SocketTimeoutException) {
            throw NetworkException(e)
        }
    }

    override suspend fun getQuote(symbol: String): FullQuoteData {
        val stream = getByteStream(
            FINANCE_QUERY_API_URL.newBuilder().apply {
                addPathSegments("quotes")
                addQueryParameter("symbols", symbol)
            }.build()
        )
        val quoteResponseList: List<FullQuoteResponse>

        try {
            quoteResponseList =
                parser.decodeFromStream(ListSerializer(FullQuoteResponse.serializer()), stream)
        } catch (e: SerializationException) {
            throw SerializationException(e)
        }
        return quoteResponseList.map {
            FullQuoteData(
                symbol = it.symbol,
                name = it.name,
                price = it.price,
                postMarketPrice = it.afterHoursPrice,
                change = it.change,
                percentChange = it.percentChange,
                open = it.open,
                high = it.high,
                low = it.low,
                yearHigh = it.yearHigh,
                yearLow = it.yearLow,
                volume = it.volume,
                avgVolume = it.avgVolume,
                marketCap = it.marketCap,
                beta = it.beta,
                eps = it.eps,
                pe = it.pe,
                dividend = it.dividend,
                yield = it.yield,
                netAssets = it.netAssets,
                nav = it.nav,
                expenseRatio = it.expenseRatio,
                category = it.category,
                lastCapitalGain = it.lastCapitalGain,
                morningstarRating = it.morningstarRating,
                morningstarRisk = it.morningstarRiskRating,
                holdingsTurnover = it.holdingsTurnover,
                lastDividend = it.lastDividend,
                inceptionDate = it.inceptionDate,
                exDividend = it.exDividend,
                earningsDate = it.earningsDate,
                sector = it.sector,
                industry = it.industry,
                about = it.about,
                ytdReturn = it.ytdReturn,
                yearReturn = it.yearReturn,
                threeYearReturn = it.threeYearReturn,
                fiveYearReturn = it.fiveYearReturn,
                logo = it.logo
            )
        }.first()
    }

    override suspend fun getSimpleQuote(symbol: String): SimpleQuoteData {
        val stream = getByteStream(
            FINANCE_QUERY_API_URL.newBuilder().apply {
                addPathSegments("simple-quotes")
                addQueryParameter("symbols", symbol)
            }.build()
        )
        val quoteResponseList: List<SimpleQuoteResponse>

        try {
            quoteResponseList =
                parser.decodeFromStream(ListSerializer(SimpleQuoteResponse.serializer()), stream)
        } catch (e: SerializationException) {
            throw SerializationException("Failed to parse JSON response", e)
        }
        return quoteResponseList.map {
            SimpleQuoteData(
                symbol = it.symbol,
                name = it.name,
                price = it.price,
                change = it.change,
                percentChange = it.percentChange
            )
        }.first()
    }

    override suspend fun getBulkQuote(symbols: List<String>): ImmutableList<SimpleQuoteData> {
        val symbolList = symbols.joinToString(",")
        val stream = getByteStream(
            FINANCE_QUERY_API_URL.newBuilder().apply {
                addPathSegments("simple-quotes")
                addQueryParameter("symbols", symbolList)
            }.build()
        )
        val quoteResponseList: List<SimpleQuoteResponse>

        try {
            quoteResponseList =
                parser.decodeFromStream(ListSerializer(SimpleQuoteResponse.serializer()), stream)
        } catch (e: SerializationException) {
            throw SerializationException("Failed to parse JSON response", e)
        }
        return quoteResponseList.map {
            SimpleQuoteData(
                symbol = it.symbol,
                name = it.name,
                price = it.price,
                change = it.change,
                percentChange = it.percentChange
            )
        }.toImmutableList()
    }

    override suspend fun getHistoricalData(
        symbol: String,
        time: TimePeriod,
        interval: Interval
    ): ImmutableMap<String, HistoricalData> {
        val stream = getByteStream(
            FINANCE_QUERY_API_URL.newBuilder().apply {
                addPathSegments("historical")
                addQueryParameter("symbol", symbol)
                addQueryParameter("time", time.value)
                addQueryParameter("interval", interval.value)
            }.build()
        )

        val timeSeriesResponse: TimeSeriesResponse

        try {
            timeSeriesResponse = parser.decodeFromStream(TimeSeriesResponse.serializer(), stream)
        } catch (e: SerializationException) {
            throw SerializationException("Failed to parse JSON response", e)
        }

        // Map each date and HistoricalData from TimeSeriesResponse
        return timeSeriesResponse.data.mapKeys { entry ->
            val dateTimeString = entry.key
            val formatterWithoutTime = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val formatter24Hour = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss")
            val formatterWithTime = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")

            val dateTime = if (dateTimeString.contains(" ")) {
                LocalDateTime.parse(dateTimeString, formatter24Hour)
            } else {
                LocalDate.parse(dateTimeString, formatterWithoutTime).atStartOfDay()
            }

            dateTime.format(formatterWithTime)
        }.mapValues { entry ->
            HistoricalData(
                open = entry.value.open,
                high = entry.value.high,
                low = entry.value.low,
                close = entry.value.close,
                volume = entry.value.volume
            )
        }.toImmutableMap()
    }

    override suspend fun getIndices(): ImmutableList<MarketIndex> {
        val stream = getByteStream(
            FINANCE_QUERY_API_URL.newBuilder().apply {
                addPathSegments("indices")
            }.build()
        )
        val indexResponseList: List<IndexResponse>

        try {
            indexResponseList =
                parser.decodeFromStream(ListSerializer(IndexResponse.serializer()), stream)
        } catch (e: SerializationException) {
            throw SerializationException("Failed to parse JSON response", e)
        }
        return indexResponseList.map {
            MarketIndex(
                name = it.name,
                value = it.value,
                change = it.change,
                percentChange = it.percentChange
            )
        }.toImmutableList()
    }

    override suspend fun getSectors(): ImmutableList<MarketSector> {
        val stream = getByteStream(
            FINANCE_QUERY_API_URL.newBuilder().apply {
                addPathSegments("sectors")
            }.build()
        )
        val sectorResponseList: List<SectorResponse>

        try {
            sectorResponseList =
                parser.decodeFromStream(ListSerializer(SectorResponse.serializer()), stream)
        } catch (e: SerializationException) {
            throw SerializationException("Failed to parse JSON response", e)
        }
        return sectorResponseList.map {
            MarketSector(
                sector = it.sector,
                dayReturn = it.dayReturn,
                ytdReturn = it.ytdReturn,
                yearReturn = it.yearReturn,
                threeYearReturn = it.threeYearReturn,
                fiveYearReturn = it.fiveYearReturn
            )
        }.toImmutableList()
    }

    override suspend fun getSectorBySymbol(symbol: String): MarketSector {
        val stream = getByteStream(
            FINANCE_QUERY_API_URL.newBuilder().apply {
                addPathSegments("sectors")
                addQueryParameter("symbol", symbol)
            }.build()
        )

        val sectorResponseList: List<SectorResponse>

        try {
            sectorResponseList =
                parser.decodeFromStream(ListSerializer(SectorResponse.serializer()), stream)
        } catch (e: SerializationException) {
            throw SerializationException("Failed to parse JSON response", e)
        }
        return sectorResponseList.map {
            MarketSector(
                sector = it.sector,
                dayReturn = it.dayReturn,
                ytdReturn = it.ytdReturn,
                yearReturn = it.yearReturn,
                threeYearReturn = it.threeYearReturn,
                fiveYearReturn = it.fiveYearReturn
            )
        }.first()
    }

    override suspend fun getActives(): ImmutableList<MarketMover> {
        val stream = getByteStream(
            FINANCE_QUERY_API_URL.newBuilder().apply {
                addPathSegments("actives")
            }.build()
        )
        val marketMoverList: List<MarketMoverResponse>

        try {
            marketMoverList =
                parser.decodeFromStream(ListSerializer(MarketMoverResponse.serializer()), stream)
        } catch (e: SerializationException) {
            throw SerializationException("Failed to parse JSON response", e)
        }
        return marketMoverList.map {
            MarketMover(
                symbol = it.symbol,
                name = it.name,
                price = it.price,
                change = it.change,
                percentChange = it.percentChange
            )
        }.toImmutableList()
    }

    override suspend fun getGainers(): ImmutableList<MarketMover> {
        val stream = getByteStream(
            FINANCE_QUERY_API_URL.newBuilder().apply {
                addPathSegments("gainers")
            }.build()
        )

        val marketMoverList: List<MarketMoverResponse>

        try {
            marketMoverList =
                parser.decodeFromStream(ListSerializer(MarketMoverResponse.serializer()), stream)
        } catch (e: SerializationException) {
            throw SerializationException("Failed to parse JSON response", e)
        }

        return marketMoverList.map {
            MarketMover(
                symbol = it.symbol,
                name = it.name,
                price = it.price,
                change = it.change,
                percentChange = it.percentChange
            )
        }.toImmutableList()
    }

    override suspend fun getLosers(): ImmutableList<MarketMover> {
        val stream = getByteStream(
            FINANCE_QUERY_API_URL.newBuilder().apply {
                addPathSegments("losers")
            }.build()
        )

        val marketMoverList: List<MarketMoverResponse>

        try {
            marketMoverList =
                parser.decodeFromStream(ListSerializer(MarketMoverResponse.serializer()), stream)
        } catch (e: SerializationException) {
            throw SerializationException("Failed to parse JSON response", e)
        }

        return marketMoverList.map {
            MarketMover(
                symbol = it.symbol,
                name = it.name,
                price = it.price,
                change = it.change,
                percentChange = it.percentChange
            )
        }.toImmutableList()
    }

    override suspend fun getNews(): ImmutableList<News> {
        val stream = getByteStream(
            FINANCE_QUERY_API_URL.newBuilder().apply {
                addPathSegments("news")
            }.build()
        )

        val newsList: List<NewsResponse>

        try {
            newsList =
                parser.decodeFromStream(ListSerializer(NewsResponse.serializer()), stream)
        } catch (e: SerializationException) {
            throw SerializationException("Failed to parse JSON response", e)
        }

        return newsList.shuffled().map {
            News(
                title = it.title,
                link = it.link,
                source = it.source,
                img = it.img,
                time = it.time
            )
        }.toImmutableList()
    }

    override suspend fun getNewsForSymbol(symbol: String): ImmutableList<News> {
        val stream = getByteStream(
            FINANCE_QUERY_API_URL.newBuilder().apply {
                addPathSegments("news")
                addQueryParameter("symbol", symbol)
            }.build()
        )

        val newsList: List<NewsResponse>

        try {
            newsList =
                parser.decodeFromStream(ListSerializer(NewsResponse.serializer()), stream)
        } catch (e: SerializationException) {
            throw SerializationException("Failed to parse JSON response", e)
        }

        return newsList.map {
            News(
                title = it.title,
                link = it.link,
                source = it.source,
                img = it.img,
                time = it.time
            )
        }.toImmutableList()
    }

    override suspend fun getSimilarSymbols(symbol: String): ImmutableList<SimpleQuoteData> {
        val stream = getByteStream(
            FINANCE_QUERY_API_URL.newBuilder().apply {
                addPathSegments("similar-stocks")
                addQueryParameter("symbol", symbol)
            }.build()
        )

        val quoteResponseList: List<SimpleQuoteResponse>

        try {
            quoteResponseList =
                parser.decodeFromStream(ListSerializer(SimpleQuoteResponse.serializer()), stream)
        } catch (e: SerializationException) {
            throw SerializationException("Failed to parse JSON response", e)
        }

        return quoteResponseList.map {
            SimpleQuoteData(
                symbol = it.symbol,
                name = it.name,
                price = it.price,
                change = it.change,
                percentChange = it.percentChange
            )
        }.toImmutableList()
    }

    override suspend fun getTechnicalIndicator(symbol: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getSummaryAnalysis(symbol: String, interval: Interval): Analysis {
        val stream = getByteStream(
            FINANCE_QUERY_API_URL.newBuilder().apply {
                addPathSegments("analysis")
                addQueryParameter("symbol", symbol)
                addQueryParameter("interval", interval.value)
            }.build()
        )

        val analysisResponse: AnalysisResponse

        try {
            analysisResponse = parser.decodeFromStream(AnalysisResponse.serializer(), stream)
        } catch (e: SerializationException) {
            throw SerializationException("Failed to parse JSON response", e)
        }

        return Analysis(
            sma10 = analysisResponse.sma10,
            sma20 = analysisResponse.sma20,
            sma50 = analysisResponse.sma50,
            sma100 = analysisResponse.sma100,
            sma200 = analysisResponse.sma200,
            ema10 = analysisResponse.ema10,
            ema20 = analysisResponse.ema20,
            ema50 = analysisResponse.ema50,
            ema100 = analysisResponse.ema100,
            ema200 = analysisResponse.ema200,
            wma10 = analysisResponse.wma10,
            wma20 = analysisResponse.wma20,
            wma50 = analysisResponse.wma50,
            wma100 = analysisResponse.wma100,
            wma200 = analysisResponse.wma200,
            vwma20 = analysisResponse.vwma20,
            rsi14 = analysisResponse.rsi14,
            srsi14 = analysisResponse.srsi14,
            cci20 = analysisResponse.cci20,
            adx14 = analysisResponse.adx14,
            macd = analysisResponse.macd.toMacd(),
            stoch = analysisResponse.stoch,
            aroon = analysisResponse.aroon.toAroon(),
            bBands = analysisResponse.bBands.toBBands(),
            superTrend = analysisResponse.superTrend.toSuperTrend(),
            ichimokuCloud = analysisResponse.ichimokuCloud.toIchimokuCloud()
        )
    }
}