package com.example.videodownloader.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowDownward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.videodownloader.domain.model.DownloadStatus
import com.example.videodownloader.domain.model.DownloadTask
import com.example.videodownloader.domain.model.Quality
import com.example.videodownloader.domain.model.VideoInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val url by viewModel.url.collectAsState()
    val isExtracting by viewModel.isExtracting.collectAsState()
    val error by viewModel.error.collectAsState()
    val videoInfo by viewModel.videoInfo.collectAsState()
    val downloads by viewModel.downloads.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video Downloader") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            UrlInputSection(
                url = url,
                onUrlChange = viewModel::onUrlChange,
                isExtracting = isExtracting,
                onExtractClick = viewModel::extractVideo,
                error = error
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Downloads", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            if (downloads.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No downloads yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(downloads) { download ->
                        DownloadItem(download)
                    }
                }
            }
        }

        videoInfo?.let { info ->
            QualitySelectionSheet(
                videoInfo = info,
                onQualitySelected = { viewModel.startDownload(it) },
                onDismiss = viewModel::dismissVideoInfo
            )
        }
    }
}

@Composable
fun UrlInputSection(
    url: String,
    onUrlChange: (String) -> Unit,
    isExtracting: Boolean,
    onExtractClick: () -> Unit,
    error: String?
) {
    Column {
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Video URL") },
            singleLine = true,
            trailingIcon = {
                if (url.isNotEmpty()) {
                    IconButton(onClick = { onUrlChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear URL")
                    }
                }
            },
            isError = error != null
        )

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onExtractClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isExtracting && url.isNotBlank()
        ) {
            if (isExtracting) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Text("Extracting...")
            } else {
                Text("Extract Video")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualitySelectionSheet(
    videoInfo: VideoInfo,
    onQualitySelected: (Quality) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = videoInfo.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Select Quality", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(videoInfo.qualities) { quality ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onQualitySelected(quality) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${quality.resolution} (${quality.format})")
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowDownward,
                            contentDescription = "Download"
                        )
                    }
                    Divider()
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DownloadItem(task: DownloadTask) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${task.quality} • ${task.status.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when (task.status) {
                        DownloadStatus.COMPLETED -> Color(0xFF4CAF50)
                        DownloadStatus.FAILED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                if (task.status == DownloadStatus.DOWNLOADING) {
                    Text(
                        text = "${task.progress}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (task.status == DownloadStatus.DOWNLOADING || task.status == DownloadStatus.PENDING) {
                Spacer(modifier = Modifier.height(8.dp))
                if (task.status == DownloadStatus.PENDING) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LinearProgressIndicator(
                        progress = if (task.progress > 0) task.progress / 100f else 0f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
