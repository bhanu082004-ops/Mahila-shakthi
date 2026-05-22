package com.mahilashakti.unnati.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahilashakti.unnati.data.entity.SHGGroup
import com.mahilashakti.unnati.ui.theme.*
import com.mahilashakti.unnati.viewmodel.MainViewModel
import java.util.Calendar

@Composable
fun SetupScreen(viewModel: MainViewModel, onSetupComplete: () -> Unit) {
    var step by remember { mutableStateOf(0) }
    var groupName by remember { mutableStateOf("") }
    var meetingDay by remember { mutableStateOf(1) }
    var weeklyContrib by remember { mutableStateOf("") }
    var secretaryName by remember { mutableStateOf("") }
    var secretaryPhone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("12") }
    var error by remember { mutableStateOf("") }

    val days = listOf("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday")
    val totalSteps = 3

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Primary.copy(alpha = 0.2f), Background))
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Header
            Text("🌸", fontSize = 56.sp)
            Text("Mahila Shakti Unnati", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Primary, textAlign = TextAlign.Center)
            Text("ಮಹಿಳಾ ಶಕ್ತಿ ಉನ್ನತಿ", style = MaterialTheme.typography.titleSmall, color = Secondary)
            Text("SHG Digital Accountant", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)

            // Progress
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(totalSteps) { i ->
                    Box(
                        modifier = Modifier.weight(1f).height(4.dp).background(
                            if (i <= step) Primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            RoundedCornerShape(2.dp)
                        )
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    when (step) {
                        0 -> {
                            Text("Group Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            OutlinedTextField(value = groupName, onValueChange = { groupName = it; error = "" }, label = { Text("Group Name *") }, leadingIcon = { Icon(Icons.Default.Group, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = weeklyContrib, onValueChange = { weeklyContrib = it; error = "" }, label = { Text("Weekly Contribution (₹) *") }, leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                            OutlinedTextField(value = interestRate, onValueChange = { interestRate = it; error = "" }, label = { Text("Default Annual Interest Rate (%)") }, leadingIcon = { Icon(Icons.Default.Percent, null) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)

                            Text("Meeting Day *", style = MaterialTheme.typography.labelLarge)
                            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                days.forEachIndexed { index, day ->
                                    FilterChip(
                                        selected = meetingDay == index + 1,
                                        onClick = { meetingDay = index + 1 },
                                        label = { Text(day.take(3)) }
                                    )
                                }
                            }
                        }
                        1 -> {
                            Text("Secretary Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            OutlinedTextField(value = secretaryName, onValueChange = { secretaryName = it; error = "" }, label = { Text("Secretary Name *") }, leadingIcon = { Icon(Icons.Default.Person, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = secretaryPhone, onValueChange = { if (it.length <= 10) { secretaryPhone = it; error = "" } }, label = { Text("Phone Number *") }, leadingIcon = { Icon(Icons.Default.Phone, null) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
                        }
                        2 -> {
                            Text("Set Secretary PIN", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("This PIN protects admin actions like issuing loans and editing members.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                            OutlinedTextField(value = pin, onValueChange = { if (it.length <= 4) { pin = it; error = "" } }, label = { Text("4-digit PIN *") }, leadingIcon = { Icon(Icons.Default.Lock, null) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), visualTransformation = PasswordVisualTransformation(), singleLine = true)
                            OutlinedTextField(value = confirmPin, onValueChange = { if (it.length <= 4) { confirmPin = it; error = "" } }, label = { Text("Confirm PIN *") }, leadingIcon = { Icon(Icons.Default.LockOpen, null) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), visualTransformation = PasswordVisualTransformation(), singleLine = true)
                        }
                    }

                    if (error.isNotEmpty()) {
                        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (step > 0) {
                            OutlinedButton(onClick = { step--; error = "" }, modifier = Modifier.weight(1f)) { Text("Back") }
                        }
                        Button(
                            onClick = {
                                when (step) {
                                    0 -> {
                                        if (groupName.isBlank()) { error = "Group name is required"; return@Button }
                                        if (weeklyContrib.isBlank() || weeklyContrib.toDoubleOrNull() == null) { error = "Valid weekly contribution required"; return@Button }
                                        step++
                                    }
                                    1 -> {
                                        if (secretaryName.isBlank()) { error = "Secretary name required"; return@Button }
                                        if (secretaryPhone.length != 10) { error = "Valid 10-digit phone required"; return@Button }
                                        step++
                                    }
                                    2 -> {
                                        if (pin.length != 4) { error = "PIN must be 4 digits"; return@Button }
                                        if (pin != confirmPin) { error = "PINs do not match"; return@Button }
                                        val group = SHGGroup(
                                            groupName = groupName.trim(),
                                            meetingDay = meetingDay,
                                            weeklyContribution = weeklyContrib.toDouble(),
                                            formationDate = System.currentTimeMillis(),
                                            secretaryPin = pin,
                                            defaultInterestRate = interestRate.toDoubleOrNull() ?: 12.0
                                        )
                                        viewModel.saveGroup(group) {
                                            // Also add secretary as member
                                            viewModel.addMember(
                                                com.mahilashakti.unnati.data.entity.Member(
                                                    groupId = 0, // will be updated
                                                    name = secretaryName.trim(),
                                                    phone = secretaryPhone,
                                                    joinDate = System.currentTimeMillis(),
                                                    role = "SECRETARY",
                                                    creditScore = 50
                                                )
                                            ) { _, _ -> }
                                            onSetupComplete()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(if (step > 0) 1f else 1f)
                        ) {
                            Text(if (step == 2) "Create Group 🎉" else "Next →")
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
