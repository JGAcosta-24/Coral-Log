package com.corallog.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corallog.R
import com.corallog.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = koinViewModel(),
    onFinished: () -> Unit
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    // Use a state for the picker to manage selection
    val dateRangePickerState = rememberDateRangePickerState()

    val termsLabel = stringResource(R.string.terms_of_use)
    val privacyLabel = stringResource(R.string.privacy_policy)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            // App Logo in top right corner
            Image(
                painter = painterResource(id = R.drawable.ts_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.TopEnd)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = stringResource(R.string.onboarding_welcome),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.onboarding_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Question 1: Last Period Range
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.last_period_question),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
                        val displayText = when {
                            startDate != null && endDate != null -> {
                                "${startDate!!.format(formatter)} - ${endDate!!.format(formatter)}"
                            }
                            startDate != null -> {
                                startDate!!.format(formatter)
                            }
                            else -> stringResource(R.string.select_date)
                        }
                        Text(text = displayText)
                    }
                }

                // Terms and Privacy Checkbox
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { termsAccepted = !termsAccepted },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { termsAccepted = it },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = stringResource(R.string.terms_and_privacy_consent),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 48.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = termsLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                showTermsDialog = true
                            }
                        )
                        Text(
                            text = privacyLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                showPrivacyDialog = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (startDate != null && endDate != null) {
                            viewModel.completeOnboarding(
                                startDate = startDate!!,
                                endDate = endDate!!
                            )
                            onFinished()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = startDate != null && endDate != null && termsAccepted
                ) {
                    Text(
                        text = stringResource(R.string.start_tracking),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (showDatePicker) {
                // Calculation of days in real-time for validation
                val startMillis = dateRangePickerState.selectedStartDateMillis
                val endMillis = dateRangePickerState.selectedEndDateMillis
                val selectedDays = if (startMillis != null && endMillis != null) {
                    ((endMillis - startMillis) / (24 * 60 * 60 * 1000)).toInt() + 1
                } else 0
                
                val isValidRange = selectedDays in 2..10

                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                dateRangePickerState.selectedStartDateMillis?.let { start ->
                                    startDate = Instant.ofEpochMilli(start)
                                        .atZone(ZoneOffset.UTC)
                                        .toLocalDate()
                                }
                                dateRangePickerState.selectedEndDateMillis?.let { end ->
                                    endDate = Instant.ofEpochMilli(end)
                                        .atZone(ZoneOffset.UTC)
                                        .toLocalDate()
                                }
                                showDatePicker = false
                            },
                            enabled = isValidRange
                        ) {
                            Text("OK", color = if (isValidRange) MaterialTheme.colorScheme.primary else Color.Gray)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DateRangePicker(
                        state = dateRangePickerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp),
                        dateFormatter = remember { DatePickerDefaults.dateFormatter(
                            yearSelectionSkeleton = "yMMM",
                            selectedDateSkeleton = "MMMddy",
                            selectedDateDescriptionSkeleton = "MMMddy"
                        ) },
                        title = {
                            Column(modifier = Modifier.padding(start = 24.dp, top = 16.dp)) {
                                Text(
                                    text = "Select range",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (startMillis != null && endMillis != null && !isValidRange) {
                                    Text(
                                        text = "Please select 2 to 10 days",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        },
                        headline = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val startText = startMillis?.let {
                                    Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                                        .format(DateTimeFormatter.ofPattern("MMM dd"))
                                } ?: "Start"
                                
                                val endText = endMillis?.let {
                                    Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                                        .format(DateTimeFormatter.ofPattern("MMM dd"))
                                } ?: "End"
                                
                                Text(
                                    text = startText,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "-",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = endText,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    if (showTermsDialog) {
        LegalContentDialog(
            title = stringResource(R.string.terms_of_use),
            content = stringResource(R.string.terms_of_use_content),
            onDismiss = { showTermsDialog = false }
        )
    }

    if (showPrivacyDialog) {
        LegalContentDialog(
            title = stringResource(R.string.privacy_policy),
            content = stringResource(R.string.privacy_policy_content),
            onDismiss = { showPrivacyDialog = false }
        )
    }
}

@Composable
fun LegalContentDialog(
    title: String,
    content: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.8f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = content.replace("<b>", "").replace("</b>", ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close), color = MaterialTheme.colorScheme.primary)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}
