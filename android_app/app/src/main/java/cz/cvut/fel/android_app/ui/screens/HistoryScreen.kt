package cz.cvut.fel.android_app.ui.screens

import android.content.Intent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import cz.cvut.fel.android_app.ui.components.base.AppSearchBar
import cz.cvut.fel.android_app.ui.components.base.AppTopBar
import cz.cvut.fel.android_app.ui.components.domain.DeleteConfirmationDialog
import cz.cvut.fel.android_app.ui.components.domain.MeasurementItem
import cz.cvut.fel.android_app.viewmodel.HistoryViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val selectionMode = uiState.selectedIds.isNotEmpty()

    // Load more when near bottom
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            uiState.hasMore && total > 0 && lastVisible >= total - 3
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMore()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.exportContent) {
        uiState.exportContent?.let { content ->
            val names = uiState.exportedMeasurementNames
            val safeBase = names.firstOrNull()
                ?.replace(Regex("[^a-zA-Z0-9_-]"), "_")
                ?.take(40) ?: "measurements"
            val filename = if (names.size == 1) "${safeBase}_export.csv"
                           else "measurements_${names.size}_export.csv"

            val file = File(context.cacheDir, filename)
            file.writeText(content)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            val subject = if (names.size == 1) "Stream Measurement Report: ${names[0]}"
                          else "Stream Measurement Reports (${names.size} measurements)"
            val body = buildString {
                append("Please find attached the stream gauging measurement report")
                if (names.size == 1) append(" for \"${names[0]}\"")
                append(".")
                if (uiState.operatorName.isNotEmpty()) append("\n\nOperator: ${uiState.operatorName}")
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                if (uiState.userEmail.isNotEmpty()) {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(uiState.userEmail))
                }
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                context.startActivity(Intent.createChooser(intent, "Send Report via Email"))
            } catch (_: android.content.ActivityNotFoundException) {
                snackbarHostState.showSnackbar("No email app found")
            }
            viewModel.clearExportContent()
        }
    }

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
        },
        topBar = {
            if (selectionMode) {
                TopAppBar(
                    title = { Text("${uiState.selectedIds.size} Selected") },
                    navigationIcon = {
                        IconButton(onClick = viewModel::clearSelection) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Selection")
                        }
                    },
                    actions = {
                        IconButton(onClick = viewModel::exportSelected) {
                            Icon(Icons.Default.Share, contentDescription = "Export Selected")
                        }
                    }
                )
            } else {
                AppTopBar(
                    title = "Measurement History",
                    onNavigateBack = onNavigateBack
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (!selectionMode) {
                AppSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.measurements, key = { it.id }) { measurement ->
                    val isSelected = uiState.selectedIds.contains(measurement.id)
                    MeasurementItem(
                        measurement = measurement,
                        unit = uiState.preferredUnit,
                        isSelected = isSelected,
                        selectionMode = selectionMode,
                        onClick = {
                            if (selectionMode) viewModel.toggleSelection(measurement.id)
                            else onNavigateToDetails(measurement.id)
                        },
                        onLongClick = { viewModel.toggleSelection(measurement.id) }
                    )
                }

                if (uiState.hasMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}