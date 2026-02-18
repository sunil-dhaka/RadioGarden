package com.example.radiogarden.ui.screens.addstation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.radiogarden.data.remote.dto.PlaceChannelItem
import com.example.radiogarden.ui.AddStationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStationSheet(
    state: AddStationState,
    onResolveUrl: (String) -> Unit,
    onPickStation: (PlaceChannelItem) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = "Add Station",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (state.placeStations != null) {
                PlaceStationPicker(
                    stations = state.placeStations,
                    isResolving = state.isResolving,
                    onPick = onPickStation,
                )
            } else {
                UrlInput(
                    isResolving = state.isResolving,
                    error = state.error,
                    onResolve = onResolveUrl,
                )
            }
        }
    }
}

@Composable
private fun UrlInput(
    isResolving: Boolean,
    error: String?,
    onResolve: (String) -> Unit,
) {
    var url by rememberSaveable { mutableStateOf("") }

    Text(
        text = "Paste a radio.garden link or a direct stream URL",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    )
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = url,
        onValueChange = { url = it },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("https://radio.garden/listen/...") },
        singleLine = true,
        trailingIcon = {
            if (isResolving) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(8.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                IconButton(
                    onClick = { onResolve(url.trim()) },
                    enabled = url.isNotBlank(),
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        enabled = !isResolving,
    )
}

@Composable
private fun PlaceStationPicker(
    stations: List<PlaceChannelItem>,
    isResolving: Boolean,
    onPick: (PlaceChannelItem) -> Unit,
) {
    Text(
        text = "Multiple stations found. Pick one:",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    )
    Spacer(modifier = Modifier.height(12.dp))

    if (isResolving) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }

    LazyColumn {
        items(stations, key = { it.page.url }) { item ->
            ListItem(
                headlineContent = { Text(item.page.title) },
                leadingContent = {
                    Icon(
                        Icons.Default.Radio,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                modifier = Modifier.clickable(enabled = !isResolving) { onPick(item) },
            )
        }
    }
}
