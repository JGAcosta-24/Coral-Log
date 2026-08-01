package com.corallog.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corallog.R
import com.corallog.data.CyclePhase
import com.corallog.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

/**
 * Main Home screen for Coral Log (Sprint 2).
 * Displays cycle countdown, current phase, and typical symptoms.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center), 
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is HomeUiState.Success -> {
                HomeContent(state)
            }
        }
    }
}

@Composable
private fun HomeContent(state: HomeUiState.Success) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Status Title
        Text(
            text = stringResource(R.string.status_title),
            style = MaterialTheme.typography.headlineSmall,
            color = OnSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        // Days Countdown Card (HU-04)
        CountdownCard(state.daysStatus, state.predictedDate)

        // Phase Card (HU-05)
        PhaseCard(state.currentPhase)

        // Symptoms Section (HU-06)
        if (state.phaseSymptoms.isNotEmpty()) {
            SymptomsSection(state.phaseSymptoms)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun CountdownCard(status: DaysStatus, predictedDate: LocalDate?) {
    val backgroundColor = when (status) {
        is DaysStatus.Delay -> MaterialTheme.colorScheme.errorContainer
        else -> SurfaceContainerLow
    }
    
    val contentColor = when (status) {
        is DaysStatus.Delay -> MaterialTheme.colorScheme.onErrorContainer
        else -> OnSurface
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val text = when (status) {
                is DaysStatus.Remaining -> stringResource(R.string.days_remaining, status.days)
                is DaysStatus.Today -> stringResource(R.string.period_today)
                is DaysStatus.Delay -> stringResource(R.string.delay_warning, status.days)
                is DaysStatus.NoData -> stringResource(R.string.no_data_home)
            }

            if (status is DaysStatus.Remaining || status is DaysStatus.Delay) {
                val dayCount = if (status is DaysStatus.Remaining) status.days else (status as DaysStatus.Delay).days
                Text(
                    text = dayCount.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                    color = contentColor,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
            } else if (status is DaysStatus.Today) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            if (predictedDate != null && status !is DaysStatus.NoData) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.next_period_prediction, predictedDate.format(dateFormatter)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PhaseCard(phase: CyclePhase) {
    val phaseColor = when (phase) {
        CyclePhase.MENSTRUAL -> LocalPhaseColors.current.menstrual
        CyclePhase.FOLICULAR -> LocalPhaseColors.current.folicular
        CyclePhase.OVULACION -> LocalPhaseColors.current.ovulacion
        CyclePhase.LUTEA -> LocalPhaseColors.current.lutea
        CyclePhase.NONE -> Color.Gray.copy(alpha = 0.2f)
    }

    val phaseName = when (phase) {
        CyclePhase.MENSTRUAL -> stringResource(R.string.phase_menstrual)
        CyclePhase.FOLICULAR -> stringResource(R.string.phase_follicular)
        CyclePhase.OVULACION -> stringResource(R.string.phase_ovulation)
        CyclePhase.LUTEA -> stringResource(R.string.phase_luteal)
        CyclePhase.NONE -> "-"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(phaseColor)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = stringResource(R.string.current_phase_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )
                Text(
                    text = phaseName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SymptomsSection(symptomResIds: List<Int>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.symptoms_title),
            style = MaterialTheme.typography.titleMedium,
            color = OnSurface,
            fontWeight = FontWeight.Bold
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            symptomResIds.forEach { resId ->
                SymptomChip(text = stringResource(resId))
            }
        }
    }
}

@Composable
private fun SymptomChip(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceContainerHigh
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurface
            )
        }
    }
}
