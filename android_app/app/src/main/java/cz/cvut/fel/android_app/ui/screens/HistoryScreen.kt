package cz.cvut.fel.android_app.ui.screens

import android.content.Intent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import cz.cvut.fel.android_app.domain.model.StreamMeasurement
import cz.cvut.fel.android_app.ui.components.base.AppSearchBar
import cz.cvut.fel.android_app.ui.components.base.AppTopBar
import cz.cvut.fel.android_app.ui.components.domain.DeleteConfirmationDialog
import cz.cvut.fel.android_app.ui.components.domain.MeasurementItem
import cz.cvut.fel.android_app.viewmodel.HistoryViewModel
import cz.cvut.fel.android_app.viewmodel.StreamMeasurementViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    measurementViewModel: StreamMeasurementViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var measurementToDelete by remember { mutableStateOf<StreamMeasurement?>(null) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val selectionMode = uiState.selectedIds.isNotEmpty()

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.exportContent) {
        uiState.exportContent?.let { content ->
            val file = File(context.cacheDir, "measurements_export.csv")
            file.writeText(content)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Streamflow Measurements Export")
                putExtra(Intent.EXTRA_TEXT, "Attached is the exported streamflow measurement data.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Export"))
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
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.measurements, key = { it.id }) { measurement ->
                    val isSelected = uiState.selectedIds.contains(measurement.id)
                    MeasurementItem(
                        measurement = measurement,
                        isSelected = isSelected,
                        selectionMode = selectionMode,
                        onExport = { viewModel.exportMeasurement(measurement) },
                        onDelete = { measurementToDelete = measurement },
                        onClick = {
                            if (selectionMode) {
                                viewModel.toggleSelection(measurement.id)
                            } else {
                                onNavigateToDetails(measurement.id)
                            }
                        },
                        onLongClick = {
                            viewModel.toggleSelection(measurement.id)
                        }
                    )
                }
            }
        }
    }

    if (measurementToDelete != null) {
        DeleteConfirmationDialog(
            onDismiss = { measurementToDelete = null },
            onConfirm = {
                measurementToDelete?.let { viewModel.deleteMeasurement(it) }
                measurementToDelete = null
            },
            title = "Delete Measurement",
            message = "Are you sure you want to delete '${measurementToDelete?.name}'? This action cannot be undone."
        )
    }
}
