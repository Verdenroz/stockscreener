package com.farmingdale.stockscreener.providers

import com.farmingdale.stockscreener.BuildConfig
import com.farmingdale.stockscreener.model.local.ADX
import com.farmingdale.stockscreener.model.local.AROON
import com.farmingdale.stockscreener.model.local.AnalysisType
import com.farmingdale.stockscreener.model.local.BBANDS
import com.farmingdale.stockscreener.model.local.CCI
import com.farmingdale.stockscreener.model.local.EMA
import com.farmingdale.stockscreener.model.local.QuoteData
import com.farmingdale.stockscreener.model.local.RSI
import com.farmingdale.stockscreener.model.local.SMA
import com.farmingdale.stockscreener.model.local.STOCH
import com.farmingdale.stockscreener.model.local.TechnicalAnalysisHistory
import com.farmingdale.stockscreener.model.remote.responses.ADDataResponse
import com.farmingdale.stockscreener.model.remote.responses.ADXDataResponse
import com.farmingdale.stockscreener.model.remote.responses.AROONDataResponse
import com.farmingdale.stockscreener.model.remote.responses.BBANDSDataResponse
import com.farmingdale.stockscreener.model.remote.responses.CCIDataResponse
import com.farmingdale.stockscreener.model.remote.responses.EMADataResponse
import com.farmingdale.stockscreener.model.remote.responses.OBVDataResponse
import com.farmingdale.stockscreener.model.remote.responses.QuoteDataResponse
import com.farmingdale.stockscreener.model.remote.responses.RSIDataResponse
import com.farmingdale.stockscreener.model.remote.responses.SMADataResponse
import com.farmingdale.stockscreener.model.remote.responses.STOCHDataResponse
import com.farmingdale.stockscreener.model.utils.ALPHA_VANTAGE_API_URL
import com.farmingdale.stockscreener.model.utils.executeAsync
import com.farmingdale.stockscreener.providers.base.AlphaVantageAPI
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream

@OptIn(ExperimentalSerializationApi::class)
class ImplAlphaVantageAPI(private val client: OkHttpClient): AlphaVantageAPI {
    companion object {
        private val parser: Json by lazy {
            Json {
                ignoreUnknownKeys = true
            }
        }
    }
    private suspend fun getByteStream(url: HttpUrl): InputStream {
        val request = Request.Builder()
            .url(url)
            .build()
        val call = client.newCall(request)
        val response = call.executeAsync()
        return response.body!!.byteStream()
    }
    override suspend fun getQuote(symbol: String): QuoteData {
        val stream = getByteStream(
            ALPHA_VANTAGE_API_URL.newBuilder().apply {
                addPathSegments("query")
                addQueryParameter("function", "GLOBAL_QUOTE")
                addQueryParameter("symbol", symbol)
                addQueryParameter("apikey", BuildConfig.alphaVantageAPI)
            }.build()
        )

        val quoteDataResponse: QuoteDataResponse

        try {
            quoteDataResponse = parser.decodeFromStream(QuoteDataResponse.serializer(), stream)
        } catch (e: SerializationException) {
            throw RuntimeException("Failed to parse JSON response", e)
        }

        return with(quoteDataResponse.globalQuote) {
            QuoteData(
                symbol = this.symbol,
                open = this.open,
                high = this.high,
                low = this.low,
                price = this.price,
                volume = this.volume,
                latestTradingDay = this.latestTradingDay,
                previousClose = this.previousClose,
                change = this.change,
                changePercent = this.changePercent
            )
        }
    }

    override suspend fun getSMA(
        function: AnalysisType,
        symbol: String,
        interval: String,
        timePeriod: Int,
        seriesType: String
    ): TechnicalAnalysisHistory {
        val stream = getByteStream(
            ALPHA_VANTAGE_API_URL.newBuilder().apply {
                addPathSegments("query")
                addQueryParameter("function", function.name)
                addQueryParameter("symbol", symbol)
                addQueryParameter("interval", interval)
                addQueryParameter("time_period", timePeriod.toString())
                addQueryParameter("series_type", seriesType)
                addQueryParameter("apikey", BuildConfig.alphaVantageAPI)
            }.build()
        )

        val smaResponse: SMADataResponse = parser.decodeFromStream(SMADataResponse.serializer(), stream)

        val analyses = smaResponse.technicalAnalysis.map { (date, analysis) ->
            SMA(
                date = date,
                value = analysis.SMA
            )
        }

        return TechnicalAnalysisHistory(analyses)
    }

    override suspend fun getEMA(
        function: AnalysisType,
        symbol: String,
        interval: String,
        timePeriod: Int,
        seriesType: String
    ): TechnicalAnalysisHistory {
        val stream = getByteStream(
            ALPHA_VANTAGE_API_URL.newBuilder().apply {
                addPathSegments("query")
                addQueryParameter("function", function.name)
                addQueryParameter("symbol", symbol)
                addQueryParameter("interval", interval)
                addQueryParameter("time_period", timePeriod.toString())
                addQueryParameter("series_type", seriesType)
                addQueryParameter("apikey", BuildConfig.alphaVantageAPI)
            }.build()
        )

        val emaResponse: EMADataResponse = parser.decodeFromStream(EMADataResponse.serializer(), stream)

        val analyses = emaResponse.technicalAnalysis.map { (date, analysis) ->
            EMA(
                date = date,
                value = analysis.EMA
            )
        }

        return TechnicalAnalysisHistory(analyses)
    }

    override suspend fun getSTOCH(
        function: AnalysisType,
        symbol: String,
        interval: String,
        fastKPeriod: Int?,
        slowKPeriod: Int?,
        slowDPeriod: Int?,
        slowKMAType: Int?,
        slowDMAType: Int?
    ): TechnicalAnalysisHistory {
        val stream = getByteStream(
            ALPHA_VANTAGE_API_URL.newBuilder().apply {
                addQueryParameter("function", function.name)
                addQueryParameter("symbol", symbol)
                addQueryParameter("interval", interval)
                fastKPeriod?.let { addQueryParameter("fastkperiod", it.toString()) }
                slowKPeriod?.let { addQueryParameter("slowkperiod", it.toString()) }
                slowDPeriod?.let { addQueryParameter("slowdperiod", it.toString()) }
                slowKMAType?.let { addQueryParameter("slowkmatype", it.toString()) }
                slowDMAType?.let { addQueryParameter("slowdmatype", it.toString()) }
                addQueryParameter("apikey", BuildConfig.alphaVantageAPI)
            }.build()
        )

        val stochResponse: STOCHDataResponse = parser.decodeFromStream(STOCHDataResponse.serializer(), stream)

        val analyses = stochResponse.technicalAnalysis.map { (date, analysis) ->
            STOCH(
                date = date,
                value = "",
                slowD = analysis.SlowD,
                slowK = analysis.SlowK,
            )
        }

        return TechnicalAnalysisHistory(analyses)
    }

    override suspend fun getRSI(
        function: AnalysisType,
        symbol: String,
        interval: String,
        timePeriod: Int,
        seriesType: String
    ): TechnicalAnalysisHistory {
        val stream = getByteStream(
            ALPHA_VANTAGE_API_URL.newBuilder().apply {
                addPathSegments("query")
                addQueryParameter("function", function.name)
                addQueryParameter("symbol", symbol)
                addQueryParameter("interval", interval)
                addQueryParameter("time_period", timePeriod.toString())
                addQueryParameter("series_type", seriesType)
                addQueryParameter("apikey", BuildConfig.alphaVantageAPI)
            }.build()
        )

        val rsiResponse: RSIDataResponse = parser.decodeFromStream(RSIDataResponse.serializer(), stream)

        val analyses = rsiResponse.technicalAnalysis.map { (date, analysis) ->
            RSI(
                date = date,
                value = analysis.RSI
            )
        }

        return TechnicalAnalysisHistory(analyses)
    }

    override suspend fun getADX(
        function: AnalysisType,
        symbol: String,
        interval: String,
        timePeriod: Int
    ): TechnicalAnalysisHistory {
        val stream = getByteStream(
            ALPHA_VANTAGE_API_URL.newBuilder().apply {
                addPathSegments("query")
                addQueryParameter("function", function.name)
                addQueryParameter("symbol", symbol)
                addQueryParameter("interval", interval)
                addQueryParameter("time_period", timePeriod.toString())
                addQueryParameter("apikey", BuildConfig.alphaVantageAPI)
            }.build()
        )

        val adxResponse: ADXDataResponse = parser.decodeFromStream(ADXDataResponse.serializer(), stream)

        val analyses = adxResponse.technicalAnalysis.map { (date, analysis) ->
            ADX(
                date = date,
                value =  analysis.ADX
            )
        }

        return TechnicalAnalysisHistory(analyses)
    }

    override suspend fun getCCI(
        function: AnalysisType,
        symbol: String,
        interval: String,
        timePeriod: Int
    ): TechnicalAnalysisHistory {
        val stream = getByteStream(
            ALPHA_VANTAGE_API_URL.newBuilder().apply {
                addQueryParameter("function", function.name)
                addQueryParameter("symbol", symbol)
                addQueryParameter("interval", interval)
                addQueryParameter("time_period", timePeriod.toString())
                addQueryParameter("apikey", BuildConfig.alphaVantageAPI)
            }.build()
        )

        val cciResponse: CCIDataResponse = parser.decodeFromStream(CCIDataResponse.serializer(), stream)

        val analyses = cciResponse.technicalAnalysis.map { (date, analysis) ->
            CCI(
                date = date,
                value =  analysis.CCI
            )
        }

        return TechnicalAnalysisHistory(analyses)
    }

    override suspend fun getAROON(
        function: AnalysisType,
        symbol: String,
        interval: String,
        timePeriod: Int
    ): TechnicalAnalysisHistory {
        val stream = getByteStream(
            ALPHA_VANTAGE_API_URL.newBuilder().apply {
                addQueryParameter("function", function.name)
                addQueryParameter("symbol", symbol)
                addQueryParameter("interval", interval)
                addQueryParameter("time_period", timePeriod.toString())
                addQueryParameter("apikey", BuildConfig.alphaVantageAPI)
            }.build()
        )

        val aroonResponse: AROONDataResponse = parser.decodeFromStream(AROONDataResponse.serializer(), stream)

        val analyses = aroonResponse.technicalAnalysis.map { (date, analysis) ->
            AROON(
                date = date,
                value =  "",
                aroonDown = analysis.aroonDown,
                arronUp = analysis.aroonUp
            )
        }

        return TechnicalAnalysisHistory(analyses)
    }

    override suspend fun getBBANDS(
        function: AnalysisType,
        symbol: String,
        interval: String,
        timePeriod: Int,
        seriesType: String,
        nbDevUp: Int?,
        nbDevDown: Int?,
        matype: Int?
    ): TechnicalAnalysisHistory {
        val stream = getByteStream(
            ALPHA_VANTAGE_API_URL.newBuilder().apply {
                addPathSegments("query")
                addQueryParameter("function", function.name)
                addQueryParameter("symbol", symbol)
                addQueryParameter("interval", interval)
                addQueryParameter("time_period", timePeriod.toString())
                addQueryParameter("series_type", seriesType)
                nbDevUp?.let { addQueryParameter("nbdevup", it.toString()) }
                nbDevDown?.let { addQueryParameter("nbdevdn", it.toString()) }
                addQueryParameter("apikey", BuildConfig.alphaVantageAPI)
            }.build()
        )
        val bbandsResponse: BBANDSDataResponse = parser.decodeFromStream(BBANDSDataResponse.serializer(), stream)

        val analyses = bbandsResponse.technicalAnalysis.map { (date, analysis) ->
            BBANDS(
                date = date,
                value =  "",
                realUpperBand = analysis.realUpperBand,
                realMiddleBand = analysis.realMiddleBand,
                realLowerBand = analysis.realLowerBand
            )
        }

        return TechnicalAnalysisHistory(analyses)
    }

    override suspend fun getAD(
        function: AnalysisType,
        symbol: String,
        interval: String
    ): TechnicalAnalysisHistory {
        val stream = getByteStream(
            ALPHA_VANTAGE_API_URL.newBuilder().apply {
                addQueryParameter("function", function.name)
                addQueryParameter("symbol", symbol)
                addQueryParameter("interval", interval)
                addQueryParameter("apikey", BuildConfig.alphaVantageAPI)
            }.build()
        )

        val adResponse: ADDataResponse = parser.decodeFromStream(ADDataResponse.serializer(), stream)

        val analyses = adResponse.technicalAnalysis.map { (date, analysis) ->
            CCI(
                date = date,
                value =  analysis.AD
            )
        }

        return TechnicalAnalysisHistory(analyses)
    }

    override suspend fun getOBV(
        function: AnalysisType,
        symbol: String,
        interval: String
    ): TechnicalAnalysisHistory {
        val stream = getByteStream(
            ALPHA_VANTAGE_API_URL.newBuilder().apply {
                addPathSegments("query")
                addQueryParameter("function", function.name)
                addQueryParameter("symbol", symbol)
                addQueryParameter("interval", interval)
                addQueryParameter("apikey", BuildConfig.alphaVantageAPI)
            }.build()
        )
        val obvResponse: OBVDataResponse = parser.decodeFromStream(OBVDataResponse.serializer(), stream)

        val analyses = obvResponse.technicalAnalysis.map { (date, analysis) ->
            CCI(
                date = date,
                value =  analysis.OBV
            )
        }

        return TechnicalAnalysisHistory(analyses)
    }


}