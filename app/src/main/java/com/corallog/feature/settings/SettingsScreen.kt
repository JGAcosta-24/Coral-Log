package com.corallog.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.corallog.R
import org.koin.androidx.compose.koinViewModel

/**
 * Settings Screen implementation.
 * Allows user to toggle between themes and font families.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is SettingsUiState.Loading -> {
                CircularProgressIndicator()
            }
            is SettingsUiState.Success -> {
                SettingsContent(
                    currentTheme = state.currentTheme,
                    currentThemeSelection = state.currentThemeSelection,
                    currentFont = state.currentFont,
                    onThemeSelected = { viewModel.updateTheme(it) },
                    onThemeSelectionSelected = { viewModel.updateThemeSelection(it) },
                    onFontSelected = { viewModel.updateFont(it) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    currentTheme: String,
    currentThemeSelection: String,
    currentFont: String,
    onThemeSelected: (String) -> Unit,
    onThemeSelectionSelected: (String) -> Unit,
    onFontSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineMedium
        )

        // Display Mode Selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.display_mode),
                style = MaterialTheme.typography.titleMedium
            )
            ThemeSelectionDropdown(currentThemeSelection, onThemeSelectionSelected)
        }

        // Theme Selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.theme),
                style = MaterialTheme.typography.titleMedium
            )
            ThemeDropdown(currentTheme, onThemeSelected)
        }

        // Typography Selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.typography),
                style = MaterialTheme.typography.titleMedium
            )
            FontDropdown(currentFont, onFontSelected)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelectionDropdown(
    currentSelection: String,
    onSelectionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val options = listOf(
        "SYSTEM" to stringResource(R.string.mode_system),
        "LIGHT" to stringResource(R.string.mode_light),
        "DARK" to stringResource(R.string.mode_dark)
    )

    val currentLabel = options.find { it.first == currentSelection }?.second
        ?: stringResource(R.string.mode_system)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            readOnly = true,
            value = currentLabel,
            onValueChange = { },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelectionSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeDropdown(
    currentTheme: String,
    onThemeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val themeOptions = listOf(
        "CORAL" to stringResource(R.string.theme_coral),
        "OCEANO" to stringResource(R.string.theme_ocean),
        "BOSQUE" to stringResource(R.string.theme_forest)
    )

    val currentThemeLabel = themeOptions.find { it.first == currentTheme }?.second 
        ?: stringResource(R.string.theme_coral)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            readOnly = true,
            value = currentThemeLabel,
            onValueChange = { },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            themeOptions.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onThemeSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontDropdown(
    currentFont: String,
    onFontSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val fontOptions = listOf(
        "ROBOTO" to stringResource(R.string.font_roboto),
        "MERRIWEATHER" to stringResource(R.string.font_merriweather),
        "NUNITO" to stringResource(R.string.font_nunito)
    )

    val currentFontLabel = fontOptions.find { it.first == currentFont }?.second 
        ?: stringResource(R.string.font_roboto)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            readOnly = true,
            value = currentFontLabel,
            onValueChange = { },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            fontOptions.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onFontSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}
