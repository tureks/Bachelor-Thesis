package cz.cvut.fel.android_app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import cz.cvut.fel.android_app.R
import cz.cvut.fel.android_app.domain.model.StreamMeasurement
import cz.cvut.fel.android_app.ui.components.ExportShareConfig
import cz.cvut.fel.android_app.ui.components.ExportShareEffect
import cz.cvut.fel.android_app.ui.components.base.AppSearchBar
import cz.cvut.fel.android_app.ui.components.base.AppTopBar
import cz.cvut.fel.android_app.ui.components.domain.FromDatePickerDialog
import cz.cvut.fel.android_app.ui.components.domain.DeleteConfirmationDialog
import cz.cvut.fel.android_app.ui.components.domain.MeasurementItem
import cz.cvut.fel.android_app.ui.theme.Dimens
import cz.cvut.fel.android_app.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val listState = rememberLazyListState()

    val selectionMode = uiState.selectedIds.isNotEmpty()

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

    val exportConfig = uiState.exportContent?.let { content ->
        ExportShareConfig(
            content = content,
            measurementNames = uiState.exportedMeasurementNames,
            userEmail = uiState.userEmail,
            operatorName = uiState.operatorName
        )
    }
    ExportShareEffect(
        config = exportConfig,
        snackbarHostState = snackbarHostState,
        onDone = { viewModel.clearExportContent() }
    )

    var showDatePicker by remember { mutableStateOf(false) }
    var measurementToDelete by remember { mutableStateOf<StreamMeasurement?>(null) }

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
        },
        topBar = {
            if (selectionMode) {
                TopAppBar(
                    title = { Text(stringResource(R.string.label_selected_count, uiState.selectedIds.size)) },
                    navigationIcon = {
                        IconButton(onClick = viewModel::clearSelection) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_cancel))
                        }
                    },
                    actions = {
                        IconButton(onClick = viewModel::exportSelected) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.action_export))
                        }
                    }
                )
            } else {
                AppTopBar(
                    title = stringResource(R.string.screen_history),
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
                DateFilterRow(
                    dateRange = uiState.dateRange,
                    onPickerOpen = { showDatePicker = true },
                    onClear = { viewModel.clearDateRange() }
                )
            }

            if (uiState.measurements.isEmpty() && !uiState.hasMore) {
                EmptyHistoryState(
                    isFiltered = uiState.searchQuery.isNotBlank() || uiState.dateRange.isActive,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Dimens.paddingM),
                    verticalArrangement = Arrangement.spacedBy(Dimens.itemSpacing)
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
                                    .padding(Dimens.paddingM),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(Dimens.iconSizeLarge))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        FromDatePickerDialog(
            initialDate = uiState.dateRange.from,
            onDismiss = { showDatePicker = false },
            onConfirm = { date ->
                viewModel.setFromDate(date)
                showDatePicker = false
            }
        )
    }

    measurementToDelete?.let { m ->
        DeleteConfirmationDialog(
            title = stringResource(R.string.dialog_delete_title),
            message = stringResource(R.string.dialog_delete_message, m.name),
            onDismiss = { measurementToDelete = null },
            onConfirm = {
                viewModel.deleteMeasurement(m)
                measurementToDelete = null
            }
        )
    }
}

@Composable
private fun DateFilterRow(
    dateRange: cz.cvut.fel.android_app.viewmodel.DateRange,
    onPickerOpen: () -> Unit,
    onClear: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Dimens.paddingM, vertical = Dimens.paddingXs),
        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val fromText = dateRange.from?.let { dateFormat.format(Date(it)) }
        FilterChip(
            selected = dateRange.isActive,
            onClick = onPickerOpen,
            label = {
                Text(
                    if (fromText != null) stringResource(R.string.filter_since, fromText)
                    else stringResource(R.string.filter_from_date)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconSizeMedium)
                )
            },
            trailingIcon = if (dateRange.isActive) {
                {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.filter_clear_date),
                        modifier = Modifier
                            .size(Dimens.iconSizeMedium)
                            .clickable(onClick = onClear)
                    )
                }
            } else null
        )
    }
}

@Composable
private fun EmptyHistoryState(isFiltered: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isFiltered) "No results" else stringResource(R.string.history_empty),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Dimens.paddingS))
            Text(
                text = if (isFiltered) "Try adjusting your search or date filter"
                       else stringResource(R.string.history_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}