package cz.cvut.fel.android_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.ui.components.base.AppTextField
import cz.cvut.fel.android_app.ui.components.base.AppTopBar
import cz.cvut.fel.android_app.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: UserViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.profile

    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(user) {
        if (user != null) {
            firstName = user.firstName
            lastName = user.lastName
            email = user.email
        }
    }

    val profileDirty = user == null ||
            firstName != user.firstName ||
            lastName != user.lastName ||
            email != user.email
    val canSaveProfile = profileDirty && firstName.isNotBlank() && lastName.isNotBlank() && email.isNotBlank()

    var sliderPosition by rememberSaveable { mutableStateOf(user?.singlePointHeight?.toFloat() ?: 0.6f) }
    LaunchedEffect(user?.singlePointHeight) {
        sliderPosition = user?.singlePointHeight?.toFloat() ?: 0.6f
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Settings",
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsSection(title = "Profile") {
                AppTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = "First Name",
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                AppTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = "Last Name",
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                AppTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                Button(
                    onClick = { viewModel.updateProfile(firstName, lastName, email) },
                    enabled = canSaveProfile,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (user == null) "Get Started" else "Save Profile")
                }
            }

            SettingsSection(title = "Display Units") {
                val units = listOf(MeasurementUnit.HYDROMETRIC, MeasurementUnit.METRIC)
                val unitLabels = listOf("Hydrometric", "Metric")
                val selected = user?.preferredUnit ?: MeasurementUnit.HYDROMETRIC

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    units.forEachIndexed { index, unit ->
                        SegmentedButton(
                            selected = selected == unit,
                            onClick = { viewModel.updatePreferredUnit(unit) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = units.size)
                        ) {
                            Text(unitLabels[index])
                        }
                    }
                }
                Text(
                    text = if (selected == MeasurementUnit.HYDROMETRIC)
                        "Widths in cm, depths in cm, flow in l/s"
                    else
                        "Widths in m, depths in m, flow in m³/s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SettingsSection(title = "Velocity Capture") {
                val isMultipoint = user?.multipointMeasurement ?: true

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Multi-point measurement", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = if (isMultipoint) "Measure at multiple depths per segment"
                            else "Measure at a single depth per segment",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isMultipoint,
                        onCheckedChange = { viewModel.updateMeasurementMode(it) }
                    )
                }

                if (!isMultipoint) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Measurement height", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = String.format(java.util.Locale.US, "%.0f%% depth", sliderPosition * 100),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        onValueChangeFinished = { viewModel.updateSinglePointHeight(sliderPosition.toDouble()) },
                        valueRange = 0.1f..0.9f,
                        steps = 15,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "ISO standard: 60% from surface",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            SettingsSection(title = "Developer") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Developer mode", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "Bypass device and probe connection requirements",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = user?.developerMode ?: false,
                        onCheckedChange = { viewModel.updateDeveloperMode(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}