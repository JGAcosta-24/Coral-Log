package com.corallog.feature.metrics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corallog.R
import com.corallog.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricsScreen(
    viewModel: MetricsViewModel = koinViewModel(),
    onNavigateToCalendar: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.metrics_title),
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Primary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Background)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (!uiState.hasEnoughData) {
                EmptyStateView(onNavigateToCalendar)
            } else {
                MetricsContent(uiState)
            }
        }
    }
}

@Composable
fun MetricsContent(state: MetricsUiState) {
    Text(
        text = stringResource(R.string.metrics_subtitle),
        style = MaterialTheme.typography.titleMedium,
        color = OnSurfaceVariant,
        fontFamily = ManropeFontFamily
    )

    MetricCard(
        title = stringResource(R.string.cycle_duration_title),
        value = state.averageCycleDuration?.let { stringResource(R.string.cycle_duration_value, it) } ?: "--",
        description = stringResource(R.string.cycle_duration_desc),
        icon = Icons.Default.Update,
        iconColor = PhaseFolicular
    )

    MetricCard(
        title = stringResource(R.string.flow_level_title),
        value = state.dominantFlowLevelRes?.let { stringResource(it) } ?: stringResource(R.string.unknown),
        description = stringResource(R.string.flow_level_desc),
        icon = Icons.Default.WaterDrop,
        iconColor = PhaseMenstrual
    )

    MetricCard(
        title = stringResource(R.string.clots_title),
        value = state.dominantClotLevelRes?.let { stringResource(it) } ?: stringResource(R.string.unknown),
        description = stringResource(R.string.clots_desc),
        icon = Icons.Default.Opacity,
        iconColor = ColorPeriodoRed
    )

    MetricCard(
        title = stringResource(R.string.cramps_intensity_title),
        value = state.averageCrampLevelRes?.let { stringResource(it) } ?: stringResource(R.string.unknown),
        description = stringResource(R.string.cramps_intensity_desc),
        icon = Icons.Default.Bolt,
        iconColor = PhaseOvulacion
    )
    
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    description: String,
    icon: ImageVector,
    iconColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = iconColor.copy(alpha = 0.2f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = OnSurfaceVariant,
                    fontFamily = ManropeFontFamily
                )
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    fontFamily = ManropeFontFamily
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = OnSurfaceVariant.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(onActionClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Analytics,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = SurfaceVariant
        )
        Text(
            text = stringResource(R.string.insufficient_data_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurface,
            fontFamily = ManropeFontFamily
        )
        Text(
            text = stringResource(R.string.insufficient_data_desc),
            textAlign = TextAlign.Center,
            color = OnSurfaceVariant,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        Button(
            onClick = onActionClick,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.go_to_calendar), color = Color.White)
        }
    }
}
