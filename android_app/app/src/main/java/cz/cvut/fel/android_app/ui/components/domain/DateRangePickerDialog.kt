package cz.cvut.fel.android_app.ui.components.domain

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cz.cvut.fel.android_app.R
import cz.cvut.fel.android_app.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FromDatePickerDialog(
    initialDate: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (date: Long?) -> Unit
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initialDate)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.selectedDateMillis) }) {
                Text(stringResource(R.string.action_apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    ) {
        DatePicker(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingS)
        )
    }
}