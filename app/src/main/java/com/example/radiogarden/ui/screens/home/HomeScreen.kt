package com.example.radiogarden.ui.screens.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.radiogarden.data.local.StationEntity
import com.example.radiogarden.ui.RadioViewModel
import com.example.radiogarden.ui.screens.addstation.AddStationSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: RadioViewModel) {
    val stations by viewModel.stations.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val defaultStationId by viewModel.defaultStationId.collectAsState()
    val addStationState by viewModel.addStationState.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "RadioGarden",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add station")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 88.dp),
        ) {
            item {
                NowPlayingCard(
                    uiState = uiState,
                    onTogglePlayPause = viewModel::togglePlayPause,
                    onStop = viewModel::stop,
                    modifier = Modifier.padding(16.dp),
                )
            }

            if (stations.isNotEmpty()) {
                item {
                    Text(
                        text = "Stations",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                items(stations, key = { it.id }) { station ->
                    StationItem(
                        station = station,
                        isDefault = station.channelId == defaultStationId,
                        isCurrentlyPlaying = station.id == uiState.currentStation?.id,
                        onClick = { viewModel.playStation(station) },
                        onDelete = {
                            handleDelete(station, viewModel, snackbarHostState, scope)
                        },
                        onSetDefault = { viewModel.setDefaultStation(station.channelId) },
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showAddSheet) {
        AddStationSheet(
            state = addStationState,
            onResolveUrl = viewModel::resolveUrl,
            onPickStation = viewModel::addFromPlaceItem,
            onDismiss = {
                showAddSheet = false
                viewModel.clearAddStationState()
            },
        )
    }
}

private fun handleDelete(
    station: StationEntity,
    viewModel: RadioViewModel,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope,
) {
    viewModel.deleteStation(station)
    scope.launch {
        val result = snackbarHostState.showSnackbar(
            message = "${station.name} deleted",
            actionLabel = "Undo",
            duration = SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.undoDelete(station)
        }
    }
}
