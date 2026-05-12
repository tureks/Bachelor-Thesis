package cz.cvut.fel.android_app.ui.components

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import cz.cvut.fel.android_app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class ExportShareConfig(
    val content: String,
    val measurementNames: List<String>,
    val userEmail: String,
    val operatorName: String
)

/**
 * Launches the system email picker.
 * Calls [onDone] after the intent is dispatched or on failure.
 */
@Composable
fun ExportShareEffect(
    config: ExportShareConfig?,
    snackbarHostState: SnackbarHostState,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val noEmailApp = stringResource(R.string.export_no_email_app)
    val chooserTitle = stringResource(R.string.export_chooser_title)

    LaunchedEffect(config) {
        config ?: return@LaunchedEffect
        val filename = buildFilename(config)
        val file = File(context.cacheDir, filename)
        withContext(Dispatchers.IO) { file.writeText(config.content, Charsets.UTF_8) }
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
            if (config.userEmail.isNotEmpty()) putExtra(Intent.EXTRA_EMAIL, arrayOf(config.userEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(Intent.createChooser(intent, chooserTitle))
        } catch (_: android.content.ActivityNotFoundException) {
            snackbarHostState.showSnackbar(noEmailApp)
        }

        onDone()
    }
}

/**
 * Saves the CSV to the device Downloads folder.
 * Shows a snackbar with the saved filename and calls [onDone] on completion.
 */
@Composable
fun SaveToDeviceEffect(
    config: ExportShareConfig?,
    snackbarHostState: SnackbarHostState,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val savedTemplate = stringResource(R.string.export_saved_to_downloads)

    LaunchedEffect(config) {
        config ?: return@LaunchedEffect
        val filename = buildFilename(config)
        withContext(Dispatchers.IO) { saveToDownloads(context, config.content, filename) }
        snackbarHostState.showSnackbar(savedTemplate.format(filename))
        onDone()
    }
}

internal fun buildFilename(config: ExportShareConfig): String {
    val safeBase = config.measurementNames.firstOrNull()
        ?.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        ?.take(40) ?: "measurements"
    return if (config.measurementNames.size == 1) "${safeBase}_export.csv"
    else "measurements_${config.measurementNames.size}_export.csv"
}

private fun saveToDownloads(context: Context, content: String, filename: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, filename)
            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
        }
        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
            }
        }
    } else {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
        File(dir, filename).writeText(content, Charsets.UTF_8)
    }
}