package cz.cvut.fel.android_app.ui.components

import android.content.Intent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import cz.cvut.fel.android_app.R
import java.io.File

data class ExportShareConfig(
    val content: String,
    val measurementNames: List<String>,
    val userEmail: String,
    val operatorName: String
)

@Composable
fun ExportShareEffect(
    config: ExportShareConfig?,
    snackbarHostState: SnackbarHostState,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val noEmailAppMessage = stringResource(R.string.export_no_email_app)
    val chooserTitle = stringResource(R.string.export_chooser_title)

    LaunchedEffect(config) {
        config ?: return@LaunchedEffect

        val safeBase = config.measurementNames.firstOrNull()
            ?.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            ?.take(40) ?: "measurements"
        val filename = if (config.measurementNames.size == 1) "${safeBase}_export.csv"
                       else "measurements_${config.measurementNames.size}_export.csv"

        val file = File(context.cacheDir, filename)
        file.writeText(config.content)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val subject = if (config.measurementNames.size == 1)
            "Stream Measurement Report: ${config.measurementNames[0]}"
        else
            "Stream Measurement Reports (${config.measurementNames.size} measurements)"

        val body = buildString {
            append("Please find attached the stream gauging measurement report")
            if (config.measurementNames.size == 1) append(" for \"${config.measurementNames[0]}\"")
            append(".")
            if (config.operatorName.isNotEmpty()) append("\n\nOperator: ${config.operatorName}")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            if (config.userEmail.isNotEmpty()) {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(config.userEmail))
            }
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(Intent.createChooser(intent, chooserTitle))
        } catch (_: android.content.ActivityNotFoundException) {
            snackbarHostState.showSnackbar(noEmailAppMessage)
        }

        onDone()
    }
}