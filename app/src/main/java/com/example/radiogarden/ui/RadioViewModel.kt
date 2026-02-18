package com.example.radiogarden.ui

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.radiogarden.RadioGardenApp
import com.example.radiogarden.data.local.StationEntity
import com.example.radiogarden.data.preferences.UserPreferences
import com.example.radiogarden.data.remote.dto.PlaceChannelItem
import com.example.radiogarden.data.repository.RadioRepository
import com.example.radiogarden.data.repository.ResolveResult
import com.example.radiogarden.data.repository.ResolvedStation
import java.net.URI
import com.example.radiogarden.playback.RadioPlaybackService
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiState(
    val currentStation: StationEntity? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val error: String? = null,
)

data class AddStationState(
    val isResolving: Boolean = false,
    val error: String? = null,
    val placeStations: List<PlaceChannelItem>? = null,
    val placeId: String? = null,
)

class RadioViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as RadioGardenApp
    private val repository: RadioRepository = app.repository
    private val preferences: UserPreferences = app.preferences

    private var mediaController: MediaController? = null

    val stations: StateFlow<List<StationEntity>> = repository.getAllStations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val defaultStationId: StateFlow<String?> = preferences.defaultStationId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _addStationState = MutableStateFlow(AddStationState())
    val addStationState: StateFlow<AddStationState> = _addStationState.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _uiState.value = _uiState.value.copy(
                isBuffering = playbackState == Player.STATE_BUFFERING,
            )
        }

        override fun onPlayerError(error: PlaybackException) {
            _uiState.value = _uiState.value.copy(
                error = "Playback error: ${error.message}",
                isPlaying = false,
                isBuffering = false,
            )
        }
    }

    init {
        connectToService()
        seedDefaultStation()
    }

    private fun connectToService() {
        val sessionToken = SessionToken(
            getApplication(),
            ComponentName(getApplication(), RadioPlaybackService::class.java)
        )
        val future = MediaController.Builder(getApplication(), sessionToken).buildAsync()
        future.addListener({
            mediaController = future.get()
            mediaController?.addListener(playerListener)
        }, MoreExecutors.directExecutor())
    }

    private fun seedDefaultStation() {
        viewModelScope.launch {
            val isFirst = preferences.isFirstLaunch.first()
            if (!isFirst) return@launch

            val seeds = listOf(
                StationEntity(
                    channelId = "mbAtEPnJ",
                    name = "Radio Aashiqanaa",
                    place = "Kanpur",
                    country = "India",
                    streamUrl = "https://mars.streamerr.co/8154/stream",
                ),
                StationEntity(
                    channelId = "J5OrSNeF",
                    name = "Marwar Radio",
                    place = "Pali",
                    country = "India",
                    streamUrl = "https://stream.zeno.fm/vq6p5vxb4v8uv",
                    website = "https://zeno.fm/radio/marwar-radio/",
                ),
            )
            for (station in seeds) {
                if (repository.getByChannelId(station.channelId) == null) {
                    repository.insert(station)
                }
            }
            preferences.setDefaultStation("mbAtEPnJ")
            preferences.markFirstLaunchDone()
        }
    }

    fun playStation(station: StationEntity) {
        _uiState.value = _uiState.value.copy(
            currentStation = station,
            error = null,
            isBuffering = true,
        )
        val controller = mediaController ?: return
        val mediaItem = MediaItem.Builder()
            .setUri(station.streamUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(station.name)
                    .setArtist("${station.place}, ${station.country}")
                    .build()
            )
            .build()
        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else if (_uiState.value.currentStation != null) {
            controller.play()
        }
    }

    fun stop() {
        mediaController?.stop()
        _uiState.value = _uiState.value.copy(isPlaying = false, isBuffering = false)
    }

    fun setDefaultStation(channelId: String) {
        viewModelScope.launch {
            preferences.setDefaultStation(channelId)
        }
    }

    fun deleteStation(station: StationEntity) {
        viewModelScope.launch {
            repository.deleteById(station.id)
            if (_uiState.value.currentStation?.id == station.id) {
                stop()
                _uiState.value = _uiState.value.copy(currentStation = null)
            }
        }
    }

    fun undoDelete(station: StationEntity) {
        viewModelScope.launch {
            repository.insert(station)
        }
    }

    fun resolveUrl(url: String) {
        viewModelScope.launch {
            _addStationState.value = AddStationState(isResolving = true)
            try {
                when (val result = repository.resolveUrl(url)) {
                    is ResolveResult.SingleStation -> {
                        addResolvedStation(result.station)
                        _addStationState.value = AddStationState()
                    }
                    is ResolveResult.MultipleStations -> {
                        _addStationState.value = AddStationState(
                            placeStations = result.stations,
                            placeId = result.placeId,
                        )
                    }
                    is ResolveResult.DirectStream -> {
                        addDirectStreamStation(result.streamUrl)
                        _addStationState.value = AddStationState()
                    }
                }
            } catch (e: Exception) {
                _addStationState.value = AddStationState(
                    error = e.message ?: "Failed to resolve URL",
                )
            }
        }
    }

    fun addFromPlaceItem(item: PlaceChannelItem) {
        viewModelScope.launch {
            _addStationState.value = _addStationState.value.copy(isResolving = true, error = null)
            try {
                val resolved = repository.resolveChannelFromItem(item)
                addResolvedStation(resolved)
                _addStationState.value = AddStationState()
            } catch (e: Exception) {
                _addStationState.value = _addStationState.value.copy(
                    isResolving = false,
                    error = e.message ?: "Failed to add station",
                )
            }
        }
    }

    private suspend fun addResolvedStation(station: ResolvedStation) {
        repository.insert(
            StationEntity(
                channelId = station.channelId,
                name = station.name,
                place = station.place,
                country = station.country,
                streamUrl = station.streamUrl,
                website = station.website,
            )
        )
    }

    private suspend fun addDirectStreamStation(streamUrl: String) {
        val host = try {
            URI(streamUrl).host ?: streamUrl
        } catch (_: Exception) {
            streamUrl
        }
        repository.insert(
            StationEntity(
                channelId = "stream_${streamUrl.hashCode()}",
                name = host,
                place = "",
                country = "",
                streamUrl = streamUrl,
            )
        )
    }

    fun clearAddStationState() {
        _addStationState.value = AddStationState()
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        super.onCleared()
    }
}
