package com.farmingdale.stockscreener.model.remote.alphavantageResponses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from AlphaVantage API for On Balance Volume (OBV) technical analysis
 */
@Serializable
data class OBVDataResponse(
    @SerialName("Meta Data")
    val metaData: MetaData,

    @SerialName("Technical Analysis: OBV")
    val technicalAnalysis: Map<String, Analysis>
) {
    @Serializable
    data class MetaData(
        @SerialName("1: Symbol")
        val symbol: String,

        @SerialName("2: Indicator")
        val indicator: String,

        @SerialName("3: Last Refreshed")
        val lastRefreshed: String,
    )

    @Serializable
    data class Analysis(
        val OBV: String
    )
}