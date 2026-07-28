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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corallog.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
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
    val isToday: Boolean = false
)

/**
 * Enumeration of menstrual cycle phases.
 */
enum class CyclePhase {
    MENSTRUAL, FOLICULAR, OVULACION, LUTEA, NONE
}

/**
 * Main Calendar screen implementation.
 * Handles display of the interactive calendar and the symptom logging inline (HU-02).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var isDaySelected by remember { mutableStateOf(false) }

    // Dynamic days calculation based on uiState.currentMonth and uiState.cycleStarts
    val dayStates = remember(uiState.currentMonth, uiState.cycleStarts) {
        val daysList = mutableListOf<DayState?>()
        val firstOfMonth = uiState.currentMonth.atDay(1)
        val firstDayOfWeek = firstOfMonth.dayOfWeek.value 
        
        // Monday as first column (1=Mon, 7=Sun)
        repeat(firstDayOfWeek - 1) { daysList.add(null) }

        val daysInMonth = uiState.currentMonth.lengthOfMonth()
        for (day in 1..daysInMonth) {
            val date = uiState.currentMonth.atDay(day)
            
            // HU-03: Real phase calculation based on all historical cycle starts
            val phase = CyclePhaseCalculator.calculatePhase(
                currentDate = date,
                cycleStarts = uiState.cycleStarts
            )
            
            daysList.add(
                DayState(
                    dayOfMonth = day,
                    date = date,
                    phase = phase,
                    isToday = date == LocalDate.now()
                )
            )
        }
        daysList
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Coral Log",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Primary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Background
                )
            )
        },
        bottomBar = {
            BottomNavBar(activeTab = "Calendario", onTabSelected = { /* TODO: Navigation */ })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Background)
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
                            LegendItem(color = PhaseMenstrual, label = "Menstrual")
                            LegendItem(color = PhaseFolicular, label = "Folicular")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            LegendItem(color = PhaseOvulacion, label = "Ovulación")
                            LegendItem(color = PhaseLutea, label = "Lútea")
                        }
                    }
                } else {
                    // Inline logging section (HU-02 updated)
                    val currentSymptom = uiState.symptoms[uiState.selectedDate.toString()]
                    LoggingSectionCard(
                        selectedDate = uiState.selectedDate,
                        isBleeding = currentSymptom?.isBleeding ?: false,
                        flowLevel = currentSymptom?.flowLevel ?: 0,
                        crampIntensity = currentSymptom?.crampIntensity ?: 0,
                        clotLevel = currentSymptom?.clotLevel ?: 0,
                        onUpdate = { bleeding, flow, cramps, clots ->
                            viewModel.onSaveSymptom(uiState.selectedDate, bleeding, flow, cramps, clots)
                        }
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
            }
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
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
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
                    Icon(Icons.Default.ChevronLeft, "Mes anterior", tint = OnSurface)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.background(SurfaceContainerHigh, CircleShape).padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Text(text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es")), color = OnSurface, fontWeight = FontWeight.Medium, fontSize = 20.sp)
                    }
                    Text(text = currentMonth.year.toString(), color = OnSurfaceVariant, fontSize = 20.sp)
                }

                IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                    Icon(Icons.Default.ChevronRight, "Mes siguiente", tint = OnSurface)
                }
            }

            val weekDays = listOf("lun", "mar", "mié", "jue", "vie", "sáb", "dom")
            Row(modifier = Modifier.fillMaxWidth()) {
                weekDays.forEach { dayName ->
                    Text(text = dayName, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = OnSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                        CyclePhase.MENSTRUAL -> PhaseMenstrual
                                        CyclePhase.FOLICULAR -> PhaseFolicular
                                        CyclePhase.OVULACION -> PhaseOvulacion
                                        CyclePhase.LUTEA -> PhaseLutea
                                        CyclePhase.NONE -> Color.Transparent
                                    }
                                    val textColor = if (dayState.phase == CyclePhase.OVULACION || dayState.phase == CyclePhase.LUTEA) OnTertiary else OnSurface

                                    Box(
                                        modifier = Modifier.fillMaxSize().clip(CircleShape).background(cellBgColor)
                                            .clickable { onDayClick(dayState.dayOfMonth) }
                                            .border(if (dayState.dayOfMonth == selectedDay) 2.dp else 0.dp, Primary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = dayState.dayOfMonth.toString(), color = textColor, fontWeight = FontWeight.Medium, fontSize = 18.sp)
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
    onUpdate: (Boolean, Int, Int, Int) -> Unit
) {
    val flowOptions = listOf("Mínimo", "Leve", "Moderado", "Alto", "Abundante")
    val clotOptions = listOf("Leve", "Moderado", "Alto")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Consume click to prevent dismissal when clicking inside this card */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Registro para ${selectedDate.dayOfMonth} de ${selectedDate.month.getDisplayName(TextStyle.FULL, Locale("es"))}",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 18.sp,
                color = OnSurface,
                fontFamily = ManropeFontFamily
            )

            // Checkbox for bleeding
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = isBleeding,
                    onCheckedChange = { onUpdate(it, flowLevel, crampIntensity, clotLevel) },
                    colors = CheckboxDefaults.colors(checkedColor = ColorPeriodoRed)
                )
                Icon(Icons.Default.WaterDrop, null, tint = ColorPeriodoRed, modifier = Modifier.size(24.dp))
                Text("¿Hubo sangrado?", color = OnSurface, fontSize = 16.sp)
            }

            // Dropdown Symptom Selectors
            SymptomDropdownRow(
                label = "Flujo",
                options = flowOptions,
                selectedIndex = flowLevel - 1,
                enabled = isBleeding,
                onSelectionChange = { index -> onUpdate(isBleeding, index + 1, crampIntensity, clotLevel) }
            )

            SymptomDropdownRow(
                label = "Cólicos",
                options = flowOptions,
                selectedIndex = crampIntensity - 1,
                enabled = isBleeding,
                onSelectionChange = { index -> onUpdate(isBleeding, flowLevel, index + 1, clotLevel) }
            )

            SymptomDropdownRow(
                label = "Coágulos",
                options = clotOptions,
                selectedIndex = clotLevel - 1,
                enabled = isBleeding,
                onSelectionChange = { index -> onUpdate(isBleeding, flowLevel, crampIntensity, index + 1) }
            )
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
            color = if (enabled) OnSurface else OnSurface.copy(alpha = 0.4f),
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )

        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { if (enabled) expanded = !expanded },
            modifier = Modifier.width(160.dp)
        ) {
            TextField(
                readOnly = true,
                value = if (selectedIndex >= 0) options[selectedIndex] else "Seleccionar",
                onValueChange = { },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = if (enabled) Primary else Color.Transparent,
                    unfocusedIndicatorColor = if (enabled) OnSurfaceVariant else Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    disabledTextColor = OnSurface.copy(alpha = 0.4f)
                ),
                enabled = enabled,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(SurfaceContainerHigh)
            ) {
                options.forEachIndexed { index, selectionOption ->
                    DropdownMenuItem(
                        text = { Text(text = selectionOption, fontSize = 14.sp) },
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
fun BottomNavBar(activeTab: String, onTabSelected: (String) -> Unit) {
    NavigationBar(containerColor = SurfaceContainer, tonalElevation = 8.dp) {
        val navItems = listOf(
            NavItem("Inicio", Icons.Default.Home),
            NavItem("Calendario", Icons.Default.CalendarMonth),
            NavItem("Resumen", Icons.Default.BarChart),
            NavItem("Ajustes", Icons.Default.Settings)
        )

        navItems.forEach { item ->
            val isActive = activeTab == item.label
            NavigationBarItem(
                selected = isActive,
                onClick = { onTabSelected(item.label) },
                icon = { Icon(item.icon, item.label, modifier = Modifier.size(24.dp)) },
                label = { Text(item.label, fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = OnPrimaryContainer,
                    selectedTextColor = Primary,
                    indicatorColor = PrimaryContainer,
                    unselectedIconColor = OnSurfaceVariant,
                    unselectedTextColor = OnSurfaceVariant
                )
            )
        }
    }
}

data class NavItem(val label: String, val icon: ImageVector)

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Text(text = label, fontSize = 14.sp, color = OnSurfaceVariant, fontFamily = ManropeFontFamily)
    }
}
