package com.corallog.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.corallog.R
import org.koin.androidx.compose.koinViewModel

/**
 * Settings Screen implementation.
 * Allows user to toggle between different visual themes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings)) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is SettingsUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is SettingsUiState.Success -> {
                    SettingsContent(
                        currentTheme = state.currentTheme,
                        onThemeSelected = { viewModel.updateTheme(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsContent(
    currentTheme: String,
    onThemeSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.theme),
            style = MaterialTheme.typography.titleLarge
        )

        ThemeOption(
            label = stringResource(R.string.theme_coral),
            selected = currentTheme == "CORAL",
            onClick = { onThemeSelected("CORAL") }
        )

        ThemeOption(
            label = stringResource(R.string.theme_ocean),
            selected = currentTheme == "OCEANO",
            onClick = { onThemeSelected("OCEANO") }
        )
    }
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
