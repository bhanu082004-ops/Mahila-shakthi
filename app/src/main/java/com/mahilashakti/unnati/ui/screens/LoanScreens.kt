package com.mahilashakti.unnati.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mahilashakti.unnati.data.entity.Loan
import com.mahilashakti.unnati.data.entity.LoanWithMember
import com.mahilashakti.unnati.ui.Screen
import com.mahilashakti.unnati.ui.components.*
import com.mahilashakti.unnati.ui.theme.*
import com.mahilashakti.unnati.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllLoansScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val loans by viewModel.allActiveLoans.collectAsStateWithLifecycle()
    val outstanding by viewModel.totalOutstanding.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active Loans", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Primary.copy(0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Outstanding", style = MaterialTheme.typography.labelMedium)
                            Text("₹${String.format("%,.2f", outstanding)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Primary)
                        }
                        Icon(Icons.Default.AccountBalance, null, tint = Primary, modifier = Modifier.size(40.dp))
                    }
                }
            }

            if (loans.isEmpty()) {
                item { EmptyState("No active loans in the group.", Icons.Outlined.AccountBalance) }
            } else {
                items(loans) { loanWithMember ->
                    LoanItem(loanWithMember) {
                        onNavigate(Screen.MemberProfile.createRoute(loanWithMember.loan.memberId))
                    }
                }
            }
        }
    }
}

@Composable
fun LoanItem(loanWithMember: LoanWithMember, onClick: () -> Unit) {
    val loan = loanWithMember.loan
    val member = loanWithMember.member
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MemberAvatar(member.photoUri, member.name, 40.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(member.name, fontWeight = FontWeight.Bold)
                    Text("Issued on ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(loan.issueDate))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
                Text("ACTIVE", style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Bold)
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Outstanding", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("₹${String.format("%,.0f", loan.outstandingBalance)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Monthly EMI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("₹${String.format("%,.0f", loan.monthlyInstalment)}", fontWeight = FontWeight.Bold)
                }
            }

            LinearProgressIndicator(
                progress = { 
                    val paid = loan.totalRepayable - loan.outstandingBalance
                    (paid / loan.totalRepayable).toFloat() 
                },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = PaidGreen,
                trackColor = PaidGreen.copy(0.2f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberProfileScreen(
    memberId: Long,
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val group by viewModel.group.collectAsStateWithLifecycle()
    val allMembers by viewModel.membersWithSavings.collectAsStateWithLifecycle()
    val mws = allMembers.find { it.member.memberId == memberId }
    val member = mws?.member
    val savings = mws?.savings ?: emptyList()

    val totalSavings = savings.filter { it.status == "PAID" }.sumOf { it.amount }
    val eligibility = totalSavings * 2

    var showPinDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    if (showPinDialog) {
        PinDialog(
            onDismiss = { showPinDialog = false },
            onConfirm = { pin ->
                if (viewModel.verifyPin(pin)) {
                    showPinDialog = false
                    when (pendingAction) {
                        "loan" -> onNavigate(Screen.IssueLoan.createRoute(memberId))
                    }
                }
            }
        )
    }

    if (member == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(member.name, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { onNavigate(Screen.AiCoach.createRoute(memberId)) }) {
                        Icon(Icons.Default.AutoAwesome, null, tint = Primary)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Profile header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            MemberAvatar(member.photoUri, member.name, 72.dp)
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(member.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    if (member.role == "SECRETARY") {
                                        Surface(shape = RoundedCornerShape(50), color = Secondary.copy(0.15f)) {
                                            Text("Secretary", Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Secondary)
                                        }
                                    }
                                }
                                Text(member.phone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                                Text("Joined: ${sdf.format(Date(member.joinDate))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                CreditScoreBadge(member.creditScore)
                            }
                        }

                        HorizontalDivider()

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            ProfileStat("Total Savings", "₹${String.format("%,.2f", totalSavings)}", PaidGreen)
                            VerticalDivider(modifier = Modifier.height(48.dp))
                            ProfileStat("Loan Limit", "₹${String.format("%,.2f", eligibility)}", Primary)
                            VerticalDivider(modifier = Modifier.height(48.dp))
                            ProfileStat("Credit Score", "${member.creditScore}/100", if (member.creditScore >= 71) PaidGreen else if (member.creditScore >= 41) PendingAmber else MaterialTheme.colorScheme.error)
                        }

                        // Issue Loan button
                        Button(
                            onClick = {
                                pendingAction = "loan"
                                showPinDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                        ) {
                            Icon(Icons.Default.AccountBalance, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Issue Loan")
                        }
                    }
                }
            }

            // Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Savings History") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Loans") })
                }
            }

            if (selectedTab == 0) {
                if (savings.isEmpty()) {
                    item { EmptyState("No savings entries yet.", Icons.Outlined.Savings) }
                } else {
                    items(savings.sortedByDescending { it.meetingDate }) { entry ->
                        ListItem(
                            headlineContent = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("₹${String.format("%,.2f", entry.amount)}", fontWeight = FontWeight.SemiBold)
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (entry.status == "PAID") PaidGreenContainer else PendingAmberContainer
                                    ) {
                                        Text(
                                            entry.status,
                                            Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (entry.status == "PAID") PaidGreen else PendingAmber,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            },
                            supportingContent = { Text("Week ${entry.weekNumber}, ${entry.year}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline) },
                            trailingContent = { Text(sdf.format(Date(entry.meetingDate)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline) },
                            leadingContent = {
                                Icon(
                                    if (entry.status == "PAID") Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    null,
                                    tint = if (entry.status == "PAID") PaidGreen else PendingAmber
                                )
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            } else {
                item {
                    val activeLoans by viewModel.allActiveLoans.collectAsStateWithLifecycle()
                    val memberLoans = activeLoans.filter { it.loan.memberId == memberId }
                    
                    if (memberLoans.isEmpty()) {
                        EmptyState("No active loans for this member.", Icons.Outlined.AccountBalance)
                    } else {
                        memberLoans.forEach { loanWithMember ->
                            LoanItem(loanWithMember) {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueLoanScreen(
    memberId: Long,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val group by viewModel.group.collectAsStateWithLifecycle()
    val allMembers by viewModel.membersWithSavings.collectAsStateWithLifecycle()
    val mws = allMembers.find { it.member.memberId == memberId }
    val member = mws?.member
    val totalSavings = mws?.savings?.filter { it.status == "PAID" }?.sumOf { it.amount } ?: 0.0
    val eligibility = totalSavings * 2

    var amount by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf(group?.defaultInterestRate?.toString() ?: "12") }
    var tenure by remember { mutableStateOf("6") }
    var purpose by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var showConfirm by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val principal = amount.toDoubleOrNull() ?: 0.0
    val rateD = rate.toDoubleOrNull() ?: 12.0
    val tenureI = tenure.toIntOrNull() ?: 6
    val si = (principal * rateD * (tenureI / 12.0)) / 100.0
    val total = principal + si
    val monthly = if (tenureI > 0) total / tenureI else 0.0

    LaunchedEffect(group) { rate = group?.defaultInterestRate?.toString() ?: "12" }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Confirm Loan Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LoanDetailRow("Principal", "₹${String.format("%,.2f", principal)}")
                    LoanDetailRow("Annual Rate", "$rateD%")
                    LoanDetailRow("Tenure", "$tenureI months")
                    LoanDetailRow("Simple Interest", "₹${String.format("%,.2f", si)}")
                    HorizontalDivider()
                    LoanDetailRow("Total Repayable", "₹${String.format("%,.2f", total)}", bold = true)
                    LoanDetailRow("Monthly Instalment", "₹${String.format("%,.2f", monthly)}", bold = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    showConfirm = false
                    isLoading = true
                    viewModel.issueLoan(memberId, principal, rateD, tenureI, purpose) { success, msg ->
                        isLoading = false
                        if (success) onSuccess() else { error = msg }
                    }
                }) { Text("Issue Loan") }
            },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Edit") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Issue Loan", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Member card
            member?.let {
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MemberAvatar(it.photoUri, it.name, 44.dp)
                        Column {
                            Text(it.name, fontWeight = FontWeight.SemiBold)
                            Text("Loan Eligibility: ₹${String.format("%,.2f", eligibility)}", style = MaterialTheme.typography.bodySmall, color = PaidGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = amount, onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) { amount = it; error = "" } },
                label = { Text("Loan Amount (₹) *") }, leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) },
                modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true
            )
            if (principal > eligibility && principal > 0) {
                Text("⚠️ Amount exceeds eligibility of ₹${String.format("%,.2f", eligibility)}", color = PendingAmber, style = MaterialTheme.typography.bodySmall)
            }

            OutlinedTextField(value = rate, onValueChange = { rate = it }, label = { Text("Annual Interest Rate (%)") }, leadingIcon = { Icon(Icons.Default.Percent, null) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
            OutlinedTextField(value = tenure, onValueChange = { tenure = it }, label = { Text("Tenure (Months)") }, leadingIcon = { Icon(Icons.Default.CalendarMonth, null) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            OutlinedTextField(value = purpose, onValueChange = { purpose = it }, label = { Text("Purpose (optional)") }, leadingIcon = { Icon(Icons.Default.Info, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            // Auto-calc preview
            if (principal > 0) {
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("📊 Loan Calculation", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        LoanDetailRow("Simple Interest (SI = P×R×T/100)", "₹${String.format("%,.2f", si)}")
                        LoanDetailRow("Total Repayable", "₹${String.format("%,.2f", total)}")
                        LoanDetailRow("Monthly Instalment", "₹${String.format("%,.2f", monthly)}", bold = true)
                    }
                }
            }

            if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)

            Button(
                onClick = {
                    if (amount.isBlank() || principal <= 0) { error = "Enter valid loan amount"; return@Button }
                    showConfirm = true
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Secondary)
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Review & Issue Loan")
            }
        }
    }
}

@Composable
fun LoanDetailRow(label: String, value: String, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailScreen(
    loanId: Long,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var showRepaymentSheet by remember { mutableStateOf(false) }
    var repayAmount by remember { mutableStateOf("") }
    var payMode by remember { mutableStateOf("Cash") }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val snackState = remember { SnackbarHostState() }
    var snackMsg by remember { mutableStateOf("") }
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    // Get loan from all members' data — simplified approach
    val allMembers by viewModel.membersWithSavings.collectAsStateWithLifecycle()

    LaunchedEffect(snackMsg) {
        if (snackMsg.isNotEmpty()) { snackState.showSnackbar(snackMsg); snackMsg = "" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loan Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        snackbarHost = { SnackbarHost(snackState) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("Loan details view — integrated via Member Profile → Issue Loan flow.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
        }
    }
}
