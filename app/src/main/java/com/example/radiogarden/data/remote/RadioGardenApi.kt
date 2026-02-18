package com.example.radiogarden.data.remote

import com.example.radiogarden.data.remote.dto.ChannelResponse
import com.example.radiogarden.data.remote.dto.PlaceChannelsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class RadioGardenApi(private val client: OkHttpClient) {

    private val json = Json { ignoreUnknownKeys = true }

    private val noRedirectClient: OkHttpClient by lazy {
        client.newBuilder().followRedirects(false).build()
    }

    suspend fun getChannelMetadata(channelId: String): ChannelResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://radio.garden/api/ara/content/channel/$channelId")
            .header("User-Agent", USER_AGENT)
            .build()
        val body = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("API error: ${response.code}")
            response.body?.string() ?: error("Empty response")
        }
        json.decodeFromString<ChannelResponse>(body)
    }

    suspend fun resolveStreamUrl(channelId: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://radio.garden/api/ara/content/listen/$channelId/channel.mp3")
            .header("User-Agent", USER_AGENT)
            .build()
        noRedirectClient.newCall(request).execute().use { response ->
            if (response.isRedirect) {
                response.header("Location") ?: error("No redirect location")
            } else if (response.isSuccessful) {
                // Some channels return the stream directly
                response.request.url.toString()
            } else {
                error("Stream resolution failed: ${response.code}")
            }
        }
    }

    suspend fun getPlaceChannels(placeId: String): PlaceChannelsResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://radio.garden/api/ara/content/page/$placeId/channels")
            .header("User-Agent", USER_AGENT)
            .build()
        val body = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("API error: ${response.code}")
            response.body?.string() ?: error("Empty response")
        }
        json.decodeFromString<PlaceChannelsResponse>(body)
    }

    companion object {
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
    }
}
