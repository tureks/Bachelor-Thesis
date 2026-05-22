package cz.cvut.fel.android_app.ui.components.base

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class AppNotificationType { Success, Error }

data class AppSnackbarVisuals(
    override val message: String,
    val type: AppNotificationType,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short
) : SnackbarVisuals

suspend fun SnackbarHostState.showSuccess(message: String) {
    showSnackbar(AppSnackbarVisuals(message = message, type = AppNotificationType.Success))
}

suspend fun SnackbarHostState.showError(message: String) {
    showSnackbar(AppSnackbarVisuals(message = message, type = AppNotificationType.Error))
}

@Composable
fun AppNotificationHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp)
    ) { data ->
        val type = (data.visuals as? AppSnackbarVisuals)?.type ?: AppNotificationType.Error
        Snackbar(
            snackbarData = data,
            shape = MaterialTheme.shapes.medium,
            containerColor = if (type == AppNotificationType.Success)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer,
            contentColor = if (type == AppNotificationType.Success)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onErrorContainer
        )
    }
}