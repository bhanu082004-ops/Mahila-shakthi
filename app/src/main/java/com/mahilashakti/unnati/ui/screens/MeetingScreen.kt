package com.mahilashakti.unnati.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mahilashakti.unnati.ui.components.*
import com.mahilashakti.unnati.ui.theme.*
import com.mahilashakti.unnati.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val group by viewModel.group.collectAsStateWithLifecycle()
    val membersWithSavings by viewModel.membersWithSavings.collectAsStateWithLifecycle()
    val (weekNum, year) = viewModel.getCurrentWeekAndYear()

    // Track local state per member (memberId -> "PAID"/"PENDING"/null)
    val memberStatuses = remember { mutableStateMapOf<Long, String>() }
    var savingMemberId by remember { mutableStateOf<Long?>(null) }
    val snackState = remember { SnackbarHostState() }
    var snackMsg by remember { mutableStateOf("") }
    val contribution = group?.weeklyContribution ?: 0.0

    LaunchedEffect(snackMsg) {
        if (snackMsg.isNotEmpty()) { snackState.showSnackbar(snackMsg); snackMsg = "" }
    }

    val dateStr = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("This Week's Meeting", fontWeight = FontWeight.Bold)
                        Text("Week $weekNum · $dateStr", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        snackbarHost = { SnackbarHost(snackState) }
    ) { padding ->
        if (membersWithSavings.isEmpty()) {
            EmptyState("No members yet. Add members first!", Icons.Default.PersonAdd, Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Summary card
            item {
                val paidCount = memberStatuses.values.count { it == "PAID" }
                val totalCount = membersWithSavings.size
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Collected This Week", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("₹${String.format("%,.2f", paidCount * contribution)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = PaidGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("$paidCount / $totalCount", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            Text("Members Paid", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    LinearProgressIndicator(
                        progress = { if (totalCount > 0) paidCount.toFloat() / totalCount else 0f },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = PaidGreen,
                        trackColor = PaidGreen.copy(0.2f)
                    )
                }
            }

            item { Text("Mark Attendance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }

            items(membersWithSavings, key = { it.member.memberId }) { mws ->
                val memberId = mws.member.memberId
                val currentStatus = memberStatuses[memberId]
                val isSaving = savingMemberId == memberId

                Card(
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (currentStatus) {
                            "PAID" -> PaidGreenContainer
                            "PENDING" -> PendingAmberContainer
                            else -> MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        MemberAvatar(mws.member.photoUri, mws.member.name, 44.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(mws.member.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text("₹${String.format("%,.2f", contribution)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }

                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // PAID button
                                FilledTonalButton(
                                    onClick = {
                                        savingMemberId = memberId
                                        memberStatuses[memberId] = "PAID"
                                        viewModel.recordSavings(memberId, contribution, "PAID", System.currentTimeMillis(), weekNum, year) {
                                            savingMemberId = null
                                            snackMsg = "✅ ${mws.member.name} marked Paid"
                                        }
                                    },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (currentStatus == "PAID") PaidGreen else PaidGreen.copy(0.15f),
                                        contentColor = if (currentStatus == "PAID") Color.White else PaidGreen
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Paid", style = MaterialTheme.typography.labelMedium)
                                }

                                // PENDING button
                                FilledTonalButton(
                                    onClick = {
                                        savingMemberId = memberId
                                        memberStatuses[memberId] = "PENDING"
                                        viewModel.recordSavings(memberId, contribution, "PENDING", System.currentTimeMillis(), weekNum, year) {
                                            savingMemberId = null
                                            snackMsg = "⚠️ ${mws.member.name} marked Pending"
                                        }
                                    },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (currentStatus == "PENDING") PendingAmber else PendingAmber.copy(0.15f),
                                        contentColor = if (currentStatus == "PENDING") Color.White else PendingAmber
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Pending", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Text(
                        "💡 Tip: You can update a member's status at any time during the meeting. Changes are saved instantly.",
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
