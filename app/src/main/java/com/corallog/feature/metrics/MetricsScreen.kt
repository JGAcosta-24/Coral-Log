package com.corallog.feature.metrics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.corallog.R
import com.corallog.ui.theme.LocalPhaseColors
import org.koin.androidx.compose.koinViewModel

/**
 * Main screen for cycle metrics and data insights.
 */
@Composable
fun MetricsScreen(
    viewModel: MetricsViewModel = koinViewModel(),
    onNavigateToCalendar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        } else if (!uiState.hasEnoughData) {
            EmptyStateView(onNavigateToCalendar)
        } else {
            MetricsContent(uiState)
        }
    }
}

@Composable
private fun MetricsContent(state: MetricsUiState) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = stringResource(R.string.metrics_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = stringResource(R.string.metrics_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        MetricCard(
            title = stringResource(R.string.cycle_duration_title),
            value = state.averageCycleDuration?.let { stringResource(R.string.cycle_duration_value, it) } ?: "--",
            description = stringResource(R.string.cycle_duration_desc),
            icon = Icons.Default.Update,
            iconColor = LocalPhaseColors.current.folicular
        )

        MetricCard(
            title = stringResource(R.string.flow_level_title),
            value = state.dominantFlowLevelRes?.let { stringResource(it) } ?: stringResource(R.string.unknown),
            description = stringResource(R.string.flow_level_desc),
            icon = Icons.Default.WaterDrop,
            iconColor = LocalPhaseColors.current.menstrual
        )

        MetricCard(
            title = stringResource(R.string.clots_title),
            value = state.dominantClotLevelRes?.let { stringResource(it) } ?: stringResource(R.string.unknown),
            description = stringResource(R.string.clots_desc),
            icon = Icons.Default.Opacity,
            iconColor = MaterialTheme.colorScheme.error // proxy para PeriodoRed
        )

        MetricCard(
            title = stringResource(R.string.cramps_intensity_title),
            value = state.averageCrampLevelRes?.let { stringResource(it) } ?: stringResource(R.string.unknown),
            description = stringResource(R.string.cramps_intensity_desc),
            icon = Icons.Default.Bolt,
            iconColor = LocalPhaseColors.current.ovulacion
        )
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    description: String,
    icon: ImageVector,
    iconColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyStateView(onNavigateToCalendar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Analytics,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.surfaceContainerHigh
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.insufficient_data_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.insufficient_data_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onNavigateToCalendar,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = stringResource(R.string.go_to_calendar), color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}
