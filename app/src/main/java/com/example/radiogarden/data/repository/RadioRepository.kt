package com.example.radiogarden.data.repository

import com.example.radiogarden.data.local.StationDao
import com.example.radiogarden.data.local.StationEntity
import com.example.radiogarden.data.remote.RadioGardenApi
import com.example.radiogarden.data.remote.dto.PlaceChannelItem
import kotlinx.coroutines.flow.Flow

data class ResolvedStation(
    val channelId: String,
    val name: String,
    val place: String,
    val country: String,
    val streamUrl: String,
    val website: String = "",
)

sealed class ResolveResult {
    data class SingleStation(val station: ResolvedStation) : ResolveResult()
    data class MultipleStations(val stations: List<PlaceChannelItem>, val placeId: String) : ResolveResult()
}

class RadioRepository(
    private val dao: StationDao,
    private val api: RadioGardenApi,
) {

    fun getAllStations(): Flow<List<StationEntity>> = dao.getAllStations()

    suspend fun getByChannelId(channelId: String): StationEntity? = dao.getByChannelId(channelId)

    suspend fun insert(station: StationEntity): Long = dao.insert(station)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun resolveUrl(url: String): ResolveResult {
        val channelId = extractChannelId(url)
        if (channelId != null) {
            return resolveSingleChannel(channelId)
        }

        val placeId = extractPlaceId(url)
        if (placeId != null) {
            val response = api.getPlaceChannels(placeId)
            val items = response.data.content
                .filter { it.itemsType == "channel" }
                .flatMap { it.items }
            return when {
                items.isEmpty() -> error("No channels found at this location")
                items.size == 1 -> {
                    val id = extractChannelIdFromHref(items.first().href)
                        ?: error("Could not parse channel from place")
                    resolveSingleChannel(id)
                }
                else -> ResolveResult.MultipleStations(items, placeId)
            }
        }

        error("Could not parse radio.garden URL. Paste a channel or place link.")
    }

    suspend fun resolveChannelFromItem(item: PlaceChannelItem): ResolvedStation {
        val channelId = extractChannelIdFromHref(item.href)
            ?: error("Could not parse channel ID from: ${item.href}")
        val result = resolveSingleChannel(channelId)
        return (result as ResolveResult.SingleStation).station
    }

    private suspend fun resolveSingleChannel(channelId: String): ResolveResult.SingleStation {
        val metadata = api.getChannelMetadata(channelId)
        val streamUrl = api.resolveStreamUrl(channelId)
        return ResolveResult.SingleStation(
            ResolvedStation(
                channelId = channelId,
                name = metadata.data.title,
                place = metadata.data.place.title,
                country = metadata.data.country.title,
                streamUrl = streamUrl,
                website = metadata.data.website,
            )
        )
    }

    companion object {
        private val CHANNEL_REGEX = Regex("""/listen/[^/]+/([a-zA-Z0-9]+)""")
        private val PLACE_REGEX = Regex("""/visit/[^/]+/([a-zA-Z0-9]+)""")
        private val HREF_CHANNEL_REGEX = Regex("""/listen/([a-zA-Z0-9]+)""")

        fun extractChannelId(url: String): String? =
            CHANNEL_REGEX.find(url)?.groupValues?.get(1)

        fun extractPlaceId(url: String): String? =
            PLACE_REGEX.find(url)?.groupValues?.get(1)

        private fun extractChannelIdFromHref(href: String): String? =
            HREF_CHANNEL_REGEX.find(href)?.groupValues?.get(1)
    }
}
