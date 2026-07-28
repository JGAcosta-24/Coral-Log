package com.corallog.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
    val cycleDay: Int,
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

    // Dynamic days calculation based on uiState.currentMonth
    val dayStates = remember(uiState.currentMonth) {
        val daysList = mutableListOf<DayState?>()
        val firstOfMonth = uiState.currentMonth.atDay(1)
        val firstDayOfWeek = firstOfMonth.dayOfWeek.value 
        
        repeat(firstDayOfWeek - 1) { daysList.add(null) }

        val daysInMonth = uiState.currentMonth.lengthOfMonth()
        for (day in 1..daysInMonth) {
            val date = uiState.currentMonth.atDay(day)
            
            // Temporary phase logic (HU-03 Integration pending)
            val phase = when (day) {
                in 1..7 -> CyclePhase.MENSTRUAL
                in 8..15 -> CyclePhase.FOLICULAR
                in 16..18 -> CyclePhase.OVULACION
                in 19..28 -> CyclePhase.LUTEA
                else -> CyclePhase.NONE
            }
            
            daysList.add(
                DayState(
                    dayOfMonth = day,
                    cycleDay = day, 
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
                        fontSize = 24.sp, // Restaurado tamaño original
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Background)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 5.dp), // Padding estándar para dar aire
            verticalArrangement = Arrangement.spacedBy(24.dp) // Espaciado balanceado
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
                    viewModel.onDateSelected(uiState.currentMonth.atDay(day))
                    isDaySelected = true
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
                // Inline logging section
                val currentSymptom = uiState.symptoms[uiState.selectedDate.toString()]
                LoggingSectionCard(
                    selectedDate = uiState.selectedDate,
                    isBleeding = currentSymptom?.isBleeding ?: false,
                    flowLevel = currentSymptom?.flowLevel ?: 0,
                    crampIntensity = currentSymptom?.crampIntensity ?: 0,
                    onUpdate = { bleeding, flow, cramps ->
                        viewModel.onSaveSymptom(uiState.selectedDate, bleeding, flow, cramps)
                    }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f)) // Empuja el contenido hacia arriba sin dejar hueco al final
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(24.dp), // Restaurado padding generoso
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
                    Box(modifier = Modifier.background(SurfaceContainerHigh, CircleShape).padding(horizontal = 24.dp, vertical = 4.dp)) {
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

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { // Restaurado espacio entre filas
                dayStates.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (col in 0..6) {
                            val dayState = week.getOrNull(col)
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) { // Aspecto 1:1
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

@Composable
fun LoggingSectionCard(
    selectedDate: LocalDate,
    isBleeding: Boolean,
    flowLevel: Int,
    crampIntensity: Int,
    onUpdate: (Boolean, Int, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Registro para ${selectedDate.dayOfMonth} de ${selectedDate.month.getDisplayName(TextStyle.FULL, Locale("es"))}",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 18.sp,
                color = OnSurface,
                fontFamily = ManropeFontFamily
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.WaterDrop, null, tint = ColorPeriodoRed, modifier = Modifier.size(24.dp))
                    Text("¿Hubo sangrado?", color = OnSurface, fontSize = 16.sp)
                }
                Switch(
                    checked = isBleeding, 
                    onCheckedChange = { onUpdate(it, flowLevel, crampIntensity) }, 
                    colors = SwitchDefaults.colors(checkedThumbColor = ColorPeriodoRed)
                )
            }

            // Flow Level - Always visible, disabled/grey if not bleeding
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Nivel de Flujo", color = if (isBleeding) OnSurfaceVariant else OnSurfaceVariant.copy(alpha = 0.4f), fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(3) { i ->
                        val level = i + 1
                        FilterChip(
                            enabled = isBleeding,
                            selected = flowLevel == level,
                            onClick = { onUpdate(isBleeding, level, crampIntensity) },
                            label = { Text("Nivel $level") }
                        )
                    }
                }
            }

            // Cramp Intensity - Always visible, disabled/grey if not bleeding
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Intensidad de Cólicos", color = if (isBleeding) OnSurfaceVariant else OnSurfaceVariant.copy(alpha = 0.4f), fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(3) { i ->
                        val level = i + 1
                        FilterChip(
                            enabled = isBleeding,
                            selected = crampIntensity == level,
                            onClick = { onUpdate(isBleeding, flowLevel, level) },
                            label = { Text("Nivel $level") }
                        )
                    }
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
