package com.example.corallog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.corallog.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CoralLogTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CoralLogScreen()
                }
            }
        }
    }
}

data class DayState(
    val dayOfMonth: Int,
    val cycleDay: Int,
    val phase: CyclePhase,
    val isToday: Boolean = false
)

enum class CyclePhase {
    MENSTRUAL, FOLICULAR, OVULACION, LUTEA, NONE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoralLogScreen() {
    var selectedDay by remember { mutableStateOf(3) } // Hoy (Day 3)
    var isPeriodoLogged by remember { mutableStateOf(true) }
    var flujoLevel by remember { mutableStateOf(0) }
    var colicosLevel by remember { mutableStateOf(0) }
    var activeTab by remember { mutableStateOf("Calendario") }

    val scrollState = rememberScrollState()

    // 31 Days of July 2026, starting Wed (3 blank days at start)
    val dayStates = remember {
        val daysList = mutableListOf<DayState?>()
        repeat(3) { daysList.add(null) }

        for (day in 1..31) {
            val phase = when (day) {
                in 1..7 -> CyclePhase.MENSTRUAL
                in 8..15 -> CyclePhase.FOLICULAR
                in 16..18 -> CyclePhase.OVULACION
                in 19..28 -> CyclePhase.LUTEA
                else -> CyclePhase.NONE
            }
            val cycleDay = if (day == 1) 20 else day - 1
            daysList.add(
                DayState(
                    dayOfMonth = day,
                    cycleDay = cycleDay,
                    phase = phase,
                    isToday = day == 3
                )
            )
        }
        daysList
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = "Logo",
                                tint = PrimaryContainer,
                                modifier = Modifier.align(Alignment.Center).size(24.dp)
                            )
                        }
                        Text(
                            text = "Coral Log",
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        },
        bottomBar = {
            BottomNavBar(activeTab = activeTab, onTabSelected = { activeTab = it })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Background)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            CalendarCard(
                dayStates = dayStates,
                selectedDay = selectedDay,
                onDayClick = { selectedDay = it }
            )

            LoggingSection(
                isPeriodoLogged = isPeriodoLogged,
                onPeriodoToggle = { isPeriodoLogged = !isPeriodoLogged },
                flujoLevel = flujoLevel,
                onFlujoSelected = { flujoLevel = it },
                colicosLevel = colicosLevel,
                onColicosSelected = { colicosLevel = it }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun CalendarCard(
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
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.ChevronLeft, "Mes anterior", tint = OnSurface)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(SurfaceContainerHigh, CircleShape)
                            .padding(horizontal = 24.dp, vertical = 4.dp)
                    ) {
                        Text("julio", color = OnSurface, fontWeight = FontWeight.Medium, fontSize = 20.sp)
                    }
                    Text("2026", color = OnSurfaceVariant, fontSize = 20.sp)
                }

                IconButton(onClick = { }) {
                    Icon(Icons.Default.ChevronRight, "Mes siguiente", tint = OnSurface)
                }
            }

            val weekDays = listOf("Sol", "lunes", "martes", "miércoles", "jueves", "vie", "sábado")
            Row(modifier = Modifier.fillMaxWidth()) {
                weekDays.forEach { dayName ->
                    Text(
                        text = dayName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = OnSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val chunks = dayStates.chunked(7)
                chunks.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0..6) {
                            val dayState = week.getOrNull(col)
                            Box(
                                modifier = Modifier.weight(1f).aspectRatio(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (dayState != null) {
                                    val isSelected = dayState.dayOfMonth == selectedDay
                                    val cellBgColor = when (dayState.phase) {
                                        CyclePhase.MENSTRUAL -> PhaseMenstrual
                                        CyclePhase.FOLICULAR -> PhaseFolicular
                                        CyclePhase.OVULACION -> PhaseOvulacion
                                        CyclePhase.LUTEA -> PhaseLutea
                                        CyclePhase.NONE -> Color.Transparent
                                    }

                                    val textColor = when (dayState.phase) {
                                        CyclePhase.MENSTRUAL, CyclePhase.FOLICULAR -> OnSurface
                                        CyclePhase.OVULACION, CyclePhase.LUTEA -> OnTertiary
                                        CyclePhase.NONE -> OnSurface
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(cellBgColor)
                                            .clickable { onDayClick(dayState.dayOfMonth) }
                                            .then(
                                                if (dayState.isToday) {
                                                    Modifier.border(2.dp, PrimaryContainer, CircleShape)
                                                } else Modifier
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "${dayState.dayOfMonth}",
                                                color = textColor,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 18.sp
                                            )
                                            if (dayState.isToday) {
                                                Text("HOY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = textColor)
                                            }
                                        }

                                        Text(
                                            text = "${dayState.cycleDay}",
                                            color = if (dayState.phase == CyclePhase.NONE) OnSurfaceVariant else textColor.copy(alpha = 0.8f),
                                            fontSize = 9.sp,
                                            modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.width(130.dp)
    ) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Text(text = label, fontSize = 14.sp, color = OnSurfaceVariant, fontFamily = ManropeFontFamily)
    }
}

@Composable
fun LoggingSection(
    isPeriodoLogged: Boolean,
    onPeriodoToggle: () -> Unit,
    flujoLevel: Int,
    onFlujoSelected: (Int) -> Unit,
    colicosLevel: Int,
    onColicosSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onPeriodoToggle() }.padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.WaterDrop, "Periodo", tint = ColorPeriodoRed, modifier = Modifier.size(30.dp))
                    Text("período", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = OnSurface)
                    Box(
                        modifier = Modifier.size(24.dp).border(1.dp, Outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("?", fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPeriodoLogged) ColorPeriodoRed else SurfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPeriodoLogged) {
                        Icon(Icons.Default.Check, "Logged", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.WaterDrop, "Flujo", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
                    Text("Flujo", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = OnSurface)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(3) { levelIdx ->
                        val currentLevel = levelIdx + 1
                        val active = flujoLevel >= currentLevel
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(if (active) PrimaryContainer else SurfaceContainerHigh, CircleShape)
                                .border(1.dp, Outline, CircleShape)
                                .clickable { onFlujoSelected(if (flujoLevel == currentLevel) 0 else currentLevel) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(currentLevel) {
                                    Icon(
                                        Icons.Default.WaterDrop,
                                        "Drop",
                                        tint = if (active) Color.White else OnSurfaceVariant,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Bolt, "Cólicos", tint = ColorPeriodoRed, modifier = Modifier.size(24.dp))
                    Text("Cólicos", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = OnSurface)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(3) { levelIdx ->
                        val currentLevel = levelIdx + 1
                        val active = colicosLevel >= currentLevel
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(if (active) ColorPeriodoRed else SurfaceContainerHigh, CircleShape)
                                .border(1.dp, Outline, CircleShape)
                                .clickable { onColicosSelected(if (colicosLevel == currentLevel) 0 else currentLevel) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Bolt,
                                "Pain Bolt",
                                tint = if (active) Color.White else OnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
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
