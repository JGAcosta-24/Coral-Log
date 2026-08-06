package com.corallog.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.corallog.R
import com.corallog.data.CyclePhase
import com.corallog.ui.theme.LocalPhaseColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import org.koin.androidx.compose.koinViewModel

/**
 * Internal state for a calendar day.
 */
data class DayState(
    val dayOfMonth: Int,
    val date: LocalDate,
    val phase: CyclePhase,
    val isToday: Boolean = false,
    val isBleeding: Boolean = false
)

/**
 * Main Calendar screen implementation.
 * Handles display of the interactive calendar and the symptom logging inline (HU-02).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var isDaySelected by remember { mutableStateOf(false) }

    // Dynamic days calculation based on uiState.currentMonth, uiState.cycleStarts and uiState.symptoms
    val dayStates = remember(uiState.currentMonth, uiState.cycleStarts, uiState.symptoms) {
        val daysList = mutableListOf<DayState?>()
        val firstOfMonth = uiState.currentMonth.atDay(1)
        val firstDayOfWeek = firstOfMonth.dayOfWeek.value 
        
        // Monday as first column (1=Mon, 7=Sun)
        repeat(firstDayOfWeek - 1) { daysList.add(null) }

        val daysInMonth = uiState.currentMonth.lengthOfMonth()
        for (day in 1..daysInMonth) {
            val date = uiState.currentMonth.atDay(day)
            
            // O(1) LOOKUP from the pre-calculated phase map
            val phase = uiState.phaseMap[date] ?: CyclePhase.NONE
            val symptom = uiState.symptoms[date.toString()]
            
            daysList.add(
                DayState(
                    dayOfMonth = day,
                    date = date,
                    phase = phase,
                    isToday = date == LocalDate.now(),
                    isBleeding = symptom?.isBleeding ?: false
                )
            )
        }
        daysList
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isDaySelected = false
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CalendarCard(
                currentMonth = uiState.currentMonth,
                onMonthChange = { 
                    viewModel.onMonthChange(it)
                    isDaySelected = false 
                },
                dayStates = dayStates,
                selectedDay = if (isDaySelected) uiState.selectedDate.dayOfMonth else -1,
                onDayClick = { day ->
                    val clickedDate = uiState.currentMonth.atDay(day)
                    if (isDaySelected && uiState.selectedDate == clickedDate) {
                        // Toggle off if clicking the same day
                        isDaySelected = false
                    } else {
                        viewModel.onDateSelected(clickedDate)
                        isDaySelected = true
                    }
                }
            )

            if (!isDaySelected) {
                // Legend for phases
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        LegendItem(color = LocalPhaseColors.current.menstrual, label = stringResource(R.string.phase_menstrual))
                        LegendItem(color = LocalPhaseColors.current.folicular, label = stringResource(R.string.phase_follicular))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        LegendItem(color = LocalPhaseColors.current.ovulacion, label = stringResource(R.string.phase_ovulation))
                        LegendItem(color = LocalPhaseColors.current.lutea, label = stringResource(R.string.phase_luteal))
                    }
                    
                    Text(
                        text = stringResource(R.string.calendar_prediction_disclaimer),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = TextAlign.Justify
                    )
                }
            } else {
                // Inline logging section (HU-02 updated)
                val isFuture = uiState.selectedDate.isAfter(LocalDate.now())
                
                LoggingSectionCard(
                    selectedDate = uiState.selectedDate,
                    isBleeding = uiState.selectedIsBleeding,
                    flowLevel = uiState.selectedFlowLevel,
                    crampIntensity = uiState.selectedCrampIntensity,
                    clotLevel = uiState.selectedClotLevel,
                    hasIllness = uiState.selectedHasIllness,
                    enabled = !isFuture,
                    onUpdateBleeding = { viewModel.onUpdateBleeding(it) },
                    onUpdateFlow = { viewModel.onUpdateFlow(it) },
                    onUpdateCramps = { viewModel.onUpdateCramps(it) },
                    onUpdateClots = { viewModel.onUpdateClots(it) },
                    onToggleIllness = { viewModel.onToggleIllness(it) }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun CalendarCard(
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    dayStates: List<DayState?>,
    selectedDay: Int,
    onDayClick: (Int) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Consume click to prevent dismissal when clicking inside the card background */ }
            .pointerInput(currentMonth) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > 50) {
                            onMonthChange(currentMonth.minusMonths(1))
                        } else if (offsetX < -50) {
                            onMonthChange(currentMonth.plusMonths(1))
                        }
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX += dragAmount
                    }
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = stringResource(R.string.prev_month),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()), 
                            color = MaterialTheme.colorScheme.onSurface, 
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        text = currentMonth.year.toString(), 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = stringResource(R.string.next_month),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            val locale = Locale.getDefault()
            val weekDays = remember(locale) {
                listOf(
                    LocalDate.of(2024, 1, 1), // Monday
                    LocalDate.of(2024, 1, 2),
                    LocalDate.of(2024, 1, 3),
                    LocalDate.of(2024, 1, 4),
                    LocalDate.of(2024, 1, 5),
                    LocalDate.of(2024, 1, 6),
                    LocalDate.of(2024, 1, 7)
                ).map { it.format(DateTimeFormatter.ofPattern("EEE", locale)) }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                weekDays.forEach { dayName ->
                    Text(
                        text = dayName, 
                        modifier = Modifier.weight(1f), 
                        textAlign = TextAlign.Center, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                dayStates.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (col in 0..6) {
                            val dayState = week.getOrNull(col)
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                                if (dayState != null) {
                                    val cellBgColor = when (dayState.phase) {
                                        CyclePhase.MENSTRUAL -> LocalPhaseColors.current.menstrual
                                        CyclePhase.FOLICULAR -> LocalPhaseColors.current.folicular
                                        CyclePhase.OVULACION -> LocalPhaseColors.current.ovulacion
                                        CyclePhase.LUTEA -> LocalPhaseColors.current.lutea
                                        CyclePhase.NONE -> Color.Transparent
                                    }
                                    val textColor = if (dayState.phase == CyclePhase.OVULACION || dayState.phase == CyclePhase.LUTEA) {
                                        MaterialTheme.colorScheme.onTertiary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }

                                    Box(
                                        modifier = Modifier.fillMaxSize().clip(CircleShape).background(cellBgColor)
                                            .clickable { onDayClick(dayState.dayOfMonth) }
                                            .border(
                                                width = if (dayState.dayOfMonth == selectedDay) 2.dp else 0.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = dayState.dayOfMonth.toString(), 
                                                color = textColor, 
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            
                                            // Bleeding Indicator (Dot) - Dynamically themed
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(
                                                        color = if (dayState.isBleeding) LocalPhaseColors.current.menstrual else Color.Transparent,
                                                        shape = CircleShape
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggingSectionCard(
    selectedDate: LocalDate,
    isBleeding: Boolean,
    flowLevel: Int,
    crampIntensity: Int,
    clotLevel: Int,
    hasIllness: Boolean,
    enabled: Boolean = true,
    onUpdateBleeding: (Boolean) -> Unit,
    onUpdateFlow: (Int) -> Unit,
    onUpdateCramps: (Int) -> Unit,
    onUpdateClots: (Int) -> Unit,
    onToggleIllness: (Boolean) -> Unit
) {
    val flowOptions = listOf(
        stringResource(R.string.flow_min),
        stringResource(R.string.flow_light),
        stringResource(R.string.flow_mod),
        stringResource(R.string.flow_high),
        stringResource(R.string.flow_heavy)
    )
    val clotOptions = listOf(
        stringResource(R.string.clot_light),
        stringResource(R.string.clot_mod),
        stringResource(R.string.clot_high)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* Consume click */ }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.registration_for, 
                        selectedDate.dayOfMonth, 
                        selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (!enabled) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (!enabled) {
                Text(
                    text = stringResource(R.string.future_date_warning),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // Checkbox for bleeding
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = isBleeding,
                    onCheckedChange = { if (enabled) onUpdateBleeding(it) },
                    enabled = enabled,
                    colors = CheckboxDefaults.colors(
                        checkedColor = LocalPhaseColors.current.menstrual,
                        checkmarkColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = if (enabled) LocalPhaseColors.current.menstrual else LocalPhaseColors.current.menstrual.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.bleeding_question), 
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Dropdown Symptom Selectors
            SymptomDropdownRow(
                label = stringResource(R.string.flow_label),
                options = flowOptions,
                selectedIndex = flowLevel - 1,
                enabled = isBleeding && enabled,
                onSelectionChange = { index -> onUpdateFlow(index + 1) }
            )

            SymptomDropdownRow(
                label = stringResource(R.string.cramps_label),
                options = flowOptions,
                selectedIndex = crampIntensity - 1,
                enabled = isBleeding && enabled,
                onSelectionChange = { index -> onUpdateCramps(index + 1) }
            )

            SymptomDropdownRow(
                label = stringResource(R.string.clots_label),
                options = clotOptions,
                selectedIndex = clotLevel - 1,
                enabled = isBleeding && enabled,
                onSelectionChange = { index -> onUpdateClots(index + 1) }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Illness Toggle Section - Restricted to bleeding days (HU Polishing)
            if (isBleeding) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.label_illness),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Switch(
                            checked = hasIllness,
                            onCheckedChange = { if (enabled) onToggleIllness(it) },
                            enabled = enabled
                        )
                    }
                    Text(
                        text = stringResource(R.string.desc_illness),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomDropdownRow(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    enabled: Boolean,
    onSelectionChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { if (enabled) expanded = !expanded },
            modifier = Modifier.width(160.dp)
        ) {
            TextField(
                readOnly = true,
                value = if (selectedIndex >= 0) options[selectedIndex] else stringResource(R.string.select_option),
                onValueChange = { },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = if (enabled) MaterialTheme.colorScheme.primary else Color.Transparent,
                    unfocusedIndicatorColor = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                ),
                enabled = enabled,
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                options.forEachIndexed { index, selectionOption ->
                    DropdownMenuItem(
                        text = { Text(text = selectionOption, style = MaterialTheme.typography.bodyMedium) },
                        onClick = {
                            onSelectionChange(index)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Text(
            text = label, 
            style = MaterialTheme.typography.bodyMedium, 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
