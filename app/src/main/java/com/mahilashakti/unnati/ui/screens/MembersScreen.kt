package com.mahilashakti.unnati.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mahilashakti.unnati.data.entity.Member
import com.mahilashakti.unnati.ui.Screen
import com.mahilashakti.unnati.ui.components.*
import com.mahilashakti.unnati.ui.theme.*
import com.mahilashakti.unnati.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val group by viewModel.group.collectAsStateWithLifecycle()
    val membersWithSavings by viewModel.membersWithSavings.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var showPinDialog by remember { mutableStateOf(false) }

    val filtered = if (query.isBlank()) membersWithSavings
    else membersWithSavings.filter { it.member.name.contains(query, ignoreCase = true) }

    if (showPinDialog) {
        PinDialog(
            onDismiss = { showPinDialog = false },
            onConfirm = { pin ->
                if (viewModel.verifyPin(pin)) {
                    showPinDialog = false
                    onNavigate(Screen.AddMember.route)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Members (${membersWithSavings.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showPinDialog = true },
                containerColor = Primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.PersonAdd, null)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Search
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search members...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = if (query.isNotEmpty()) { { IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, null) } } } else null,
                shape = RoundedCornerShape(50),
                singleLine = true
            )

            if (filtered.isEmpty()) {
                EmptyState(if (query.isNotEmpty()) "No members found for \"$query\"" else "No members yet. Tap + to add.", Icons.Outlined.PersonSearch)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.member.memberId }) { mws ->
                        val totalSavings = mws.savings.filter { it.status == "PAID" }.sumOf { it.amount }
                        val paidWeeks = mws.savings.count { it.status == "PAID" }
                        val totalWeeks = mws.savings.size

                        Card(
                            onClick = { onNavigate(Screen.MemberProfile.createRoute(mws.member.memberId)) },
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(3.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                MemberAvatar(mws.member.photoUri, mws.member.name, 56.dp)
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(mws.member.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                        if (mws.member.role == "SECRETARY") {
                                            Surface(shape = RoundedCornerShape(50), color = Secondary.copy(0.15f)) {
                                                Text("Secretary", Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Secondary)
                                            }
                                        }
                                    }
                                    Text(mws.member.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    CreditScoreBadge(mws.member.creditScore)
                                    if (totalWeeks > 0) {
                                        LinearProgressIndicator(
                                            progress = { paidWeeks.toFloat() / totalWeeks },
                                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                            color = PaidGreen,
                                            trackColor = PaidGreen.copy(0.2f)
                                        )
                                        Text("${paidWeeks}/${totalWeeks} meetings paid", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("₹${String.format("%,.0f", totalSavings)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = PaidGreen)
                                    Text("Total Savings", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
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
fun AddMemberScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val group by viewModel.group.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("MEMBER") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var snackMessage by remember { mutableStateOf("") }
    val snackState = remember { SnackbarHostState() }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUri = uri
    }

    LaunchedEffect(snackMessage) {
        if (snackMessage.isNotEmpty()) { snackState.showSnackbar(snackMessage); snackMessage = "" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Member", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        snackbarHost = { SnackbarHost(snackState) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo picker
            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                if (photoUri != null) {
                    MemberAvatar(photoUri.toString(), name, 100.dp, Modifier.clip(CircleShape).clickable { galleryLauncher.launch("image/*") })
                } else {
                    Box(
                        modifier = Modifier.size(100.dp).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Primary.copy(0.3f), Secondary.copy(0.3f))))
                            .clickable { galleryLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AddAPhoto, null, tint = Primary, modifier = Modifier.size(36.dp))
                    }
                }
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).size(32.dp).clip(CircleShape).clickable { galleryLauncher.launch("image/*") },
                    color = Primary
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.padding(6.dp))
                }
            }
            Text("Tap to add photo (optional)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.align(Alignment.CenterHorizontally))

            OutlinedTextField(value = name, onValueChange = { name = it; error = "" }, label = { Text("Full Name *") }, leadingIcon = { Icon(Icons.Default.Person, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = phone, onValueChange = { if (it.length <= 10) { phone = it; error = "" } }, label = { Text("Phone Number *") }, leadingIcon = { Icon(Icons.Default.Phone, null) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)

            // Role
            Text("Role", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("MEMBER", "SECRETARY").forEach { r ->
                    FilterChip(selected = role == r, onClick = { role = r }, label = { Text(r.lowercase().replaceFirstChar { it.uppercase() }) })
                }
            }

            if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)

            Button(
                onClick = {
                    if (name.isBlank()) { error = "Name is required"; return@Button }
                    if (phone.length != 10) { error = "Enter valid 10-digit phone"; return@Button }
                    isLoading = true
                    viewModel.addMember(
                        Member(
                            groupId = group?.groupId ?: 1L,
                            name = name.trim(),
                            phone = phone,
                            photoUri = photoUri?.toString(),
                            joinDate = System.currentTimeMillis(),
                            role = role
                        )
                    ) { success, msg ->
                        isLoading = false
                        if (success) onSuccess() else { error = msg }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Add Member", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
