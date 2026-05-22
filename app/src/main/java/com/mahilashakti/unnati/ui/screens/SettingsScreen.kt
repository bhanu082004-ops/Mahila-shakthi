package com.mahilashakti.unnati.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mahilashakti.unnati.ui.components.PinDialog
import com.mahilashakti.unnati.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onReset: () -> Unit
) {
    val group by viewModel.group.collectAsStateWithLifecycle()
    var showEditGroup by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showForgotPin by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    
    var groupName by remember { mutableStateOf(group?.groupName ?: "") }
    var weeklyContrib by remember { mutableStateOf(group?.weeklyContribution?.toString() ?: "") }
    var interestRate by remember { mutableStateOf(group?.defaultInterestRate?.toString() ?: "12") }
    var newPin by remember { mutableStateOf(group?.secretaryPin ?: "") }
    
    val snackState = remember { SnackbarHostState() }
    var snackMsg by remember { mutableStateOf("") }

    LaunchedEffect(group) {
        groupName = group?.groupName ?: ""
        weeklyContrib = group?.weeklyContribution?.toString() ?: ""
        interestRate = group?.defaultInterestRate?.toString() ?: "12"
        newPin = group?.secretaryPin ?: ""
    }
    LaunchedEffect(snackMsg) {
        if (snackMsg.isNotEmpty()) { snackState.showSnackbar(snackMsg); snackMsg = "" }
    }

    if (showPinDialog) {
        PinDialog(
            onDismiss = { showPinDialog = false },
            onConfirm = { pin ->
                if (viewModel.verifyPin(pin)) {
                    showPinDialog = false
                    showEditGroup = true
                } else {
                    snackMsg = "Incorrect PIN"
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Application?") },
            text = { Text("This will permanently delete all group data and settings. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetApp {
                            showResetDialog = false
                            onReset()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Confirm Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        snackbarHost = { SnackbarHost(snackState) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Group info card
            Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(3.dp)) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Group Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    SettingRow("Group Name", group?.groupName ?: "-", Icons.Default.Group)
                    SettingRow("Weekly Contribution", "₹${group?.weeklyContribution ?: 0}", Icons.Default.CurrencyRupee)
                    SettingRow("Default Interest Rate", "${group?.defaultInterestRate ?: 12}% p.a.", Icons.Default.Percent)
                    SettingRow("Meeting Day", listOf("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday").getOrNull((group?.meetingDay ?: 1) - 1) ?: "-", Icons.Default.CalendarMonth)

                    Spacer(Modifier.height(8.dp))
                    
                    Button(
                        onClick = { showPinDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Group Details (PIN required)")
                    }

                    // Recovery Option for Developer
                    TextButton(
                        onClick = { showForgotPin = !showForgotPin },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (showForgotPin) "Hide Forgotten PIN" else "Forgot PIN? (Show Recovery)")
                    }

                    if (showForgotPin) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Your Current PIN is: ${group?.secretaryPin ?: "Not set"}",
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (showEditGroup) {
                Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(3.dp)) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Edit Group Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        
                        OutlinedTextField(value = groupName, onValueChange = { groupName = it }, label = { Text("Group Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        
                        OutlinedTextField(value = weeklyContrib, onValueChange = { weeklyContrib = it }, label = { Text("Weekly Contribution (₹)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                        
                        OutlinedTextField(value = interestRate, onValueChange = { interestRate = it }, label = { Text("Default Interest Rate (%)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                        
                        OutlinedTextField(
                            value = newPin, 
                            onValueChange = { if (it.length <= 4) newPin = it }, 
                            label = { Text("Change Secretary PIN (4 digits)") }, 
                            modifier = Modifier.fillMaxWidth(), 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { showEditGroup = false }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                            Button(
                                onClick = {
                                    if (newPin.length != 4) {
                                        snackMsg = "PIN must be 4 digits"
                                        return@Button
                                    }
                                    group?.let {
                                        viewModel.updateGroup(it.copy(
                                            groupName = groupName.ifBlank { it.groupName },
                                            weeklyContribution = weeklyContrib.toDoubleOrNull() ?: it.weeklyContribution,
                                            defaultInterestRate = interestRate.toDoubleOrNull() ?: it.defaultInterestRate,
                                            secretaryPin = newPin
                                        ))
                                    }
                                    showEditGroup = false
                                    snackMsg = "Group details and PIN updated!"
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("Save") }
                        }
                    }
                }
            }

            // Simple Reset Button
            Button(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.DeleteForever, null)
                Spacer(Modifier.width(8.dp))
                Text("Reset Application")
            }

            // About card
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Mahila Shakti Unnati v1.0", style = MaterialTheme.typography.bodyMedium)
                    Text("SHG Digital Ledger for Women's Financial Empowerment", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    Text("Built with Kotlin + Jetpack Compose + Room DB + Gemini AI", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
private fun SettingRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}
