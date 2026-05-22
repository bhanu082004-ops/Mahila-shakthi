package com.mahilashakti.unnati.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mahilashakti.unnati.ui.Screen
import com.mahilashakti.unnati.ui.components.*
import com.mahilashakti.unnati.ui.theme.*
import com.mahilashakti.unnati.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit
) {
    val group by viewModel.group.collectAsStateWithLifecycle()
    val capital by viewModel.totalCapital.collectAsStateWithLifecycle()
    val activeLoansCount by viewModel.activeLoansCount.collectAsStateWithLifecycle()
    val outstanding by viewModel.totalOutstanding.collectAsStateWithLifecycle()
    val membersWithSavings by viewModel.membersWithSavings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mahila Shakti Unnati", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                        Text(group?.groupName ?: "SHG Digital Ledger", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigate(Screen.Meeting.route) },
                icon = { Icon(Icons.Default.EventNote, null) },
                text = { Text("This Week's Meeting") },
                containerColor = Primary,
                contentColor = Color.White
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Hero card
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(Primary, Color(0xFFBF360C))))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text("Group Capital", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
                                Text("₹ ${String.format("%,.2f", capital)}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                            Text("🌸", fontSize = 48.sp)
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            MiniStat("Members", "${membersWithSavings.size}")
                            MiniStat("Active Loans", "$activeLoansCount")
                            MiniStat("Outstanding", "₹${String.format("%,.0f", outstanding)}")
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Meeting Day: ${group?.let { getDayName(it.meetingDay) } ?: "-"}  •  Weekly: ₹${String.format("%,.0f", group?.weeklyContribution ?: 0.0)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            // Quick actions
            item {
                Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickActionCard("Members", Icons.Default.Group, Primary, Modifier.weight(1f)) { onNavigate(Screen.Members.route) }
                    QuickActionCard("Loans", Icons.Default.AccountBalance, Color(0xFF00796B), Modifier.weight(1f)) { onNavigate(Screen.AllLoans.route) }
                    QuickActionCard("Reports", Icons.Default.Assessment, Secondary, Modifier.weight(1f)) { onNavigate(Screen.Reports.route) }
                    QuickActionCard("AI Coach", Icons.Default.AutoAwesome, Tertiary, Modifier.weight(1f)) { onNavigate(Screen.AiCoach.createRoute(0L)) }
                }
            }

            // Stats
            item {
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "Total Collected",
                        value = "₹${String.format("%,.0f", capital)}",
                        icon = Icons.Default.Savings,
                        modifier = Modifier.weight(1f),
                        containerColor = PaidGreenContainer,
                        contentColor = PaidGreen
                    )
                    StatCard(
                        title = "Loan Outstanding",
                        value = "₹${String.format("%,.0f", outstanding)}",
                        icon = Icons.Default.AccountBalance,
                        modifier = Modifier.weight(1f),
                        containerColor = PendingAmberContainer,
                        contentColor = PendingAmber
                    )
                }
            }

            // Members preview
            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(
                    title = "Members",
                    action = {
                        TextButton(onClick = { onNavigate(Screen.Members.route) }) { Text("See All →") }
                    }
                )
            }

            if (membersWithSavings.isEmpty()) {
                item {
                    EmptyState(
                        "No members yet. Add your first member!",
                        Icons.Outlined.PersonAdd
                    )
                }
            } else {
                items(membersWithSavings.take(4)) { mws ->
                    val totalSavings = mws.savings.filter { it.status == "PAID" }.sumOf { it.amount }
                    MemberPreviewCard(
                        name = mws.member.name,
                        phone = mws.member.phone,
                        photoUri = mws.member.photoUri,
                        creditScore = mws.member.creditScore,
                        savings = totalSavings,
                        role = mws.member.role,
                        onClick = { onNavigate(Screen.MemberProfile.createRoute(mws.member.memberId)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
    }
}

@Composable
private fun QuickActionCard(title: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun MemberPreviewCard(
    name: String,
    phone: String,
    photoUri: String?,
    creditScore: Int,
    savings: Double,
    role: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MemberAvatar(photoUri, name, size = 48.dp)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    if (role == "SECRETARY") {
                        Surface(shape = RoundedCornerShape(50), color = Secondary.copy(alpha = 0.15f)) {
                            Text("Secretary", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Secondary)
                        }
                    }
                }
                Text(phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                CreditScoreBadge(score = creditScore)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("₹${String.format("%,.0f", savings)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = PaidGreen)
                Text("Savings", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

private fun getDayName(day: Int) = listOf("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday").getOrNull(day - 1) ?: "Monday"
