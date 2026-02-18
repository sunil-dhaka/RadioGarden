package com.example.radiogarden.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChannelResponse(
    val data: ChannelData,
)

@Serializable
data class ChannelData(
    val title: String = "",
    val url: String = "",
    val website: String = "",
    val place: PlaceRef = PlaceRef(),
    val country: CountryRef = CountryRef(),
)

@Serializable
data class PlaceRef(
    val id: String = "",
    val title: String = "",
)

@Serializable
data class CountryRef(
    val id: String = "",
    val title: String = "",
)

@Serializable
data class PlaceChannelsResponse(
    val data: PlaceChannelsData,
)

@Serializable
data class PlaceChannelsData(
    val content: List<PlaceContent> = emptyList(),
)

@Serializable
data class PlaceContent(
    val items: List<PlaceChannelItem> = emptyList(),
    val itemsType: String = "",
    val type: String = "",
)

@Serializable
data class PlaceChannelItem(
    val href: String = "",
    val title: String = "",
    @SerialName("page")
    val page: ChannelPage = ChannelPage(),
)

@Serializable
data class ChannelPage(
    val title: String = "",
    val url: String = "",
)
