package cz.cvut.fel.android_app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cz.cvut.fel.android_app.ui.components.base.AppTopBar

@Composable
fun SettingsScreen() {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Settings"
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text("Settings Placeholder")
        }
    }
}
