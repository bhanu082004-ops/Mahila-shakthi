package com.mahilashakti.unnati

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.mahilashakti.unnati.ui.Screen
import com.mahilashakti.unnati.ui.screens.*
import com.mahilashakti.unnati.ui.theme.MahilaShaktiTheme
import com.mahilashakti.unnati.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MahilaShaktiTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MahilaShaktiNavHost()
                }
            }
        }
    }
}

@Composable
fun MahilaShaktiNavHost() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = hiltViewModel()
    val group by viewModel.group.collectAsStateWithLifecycle()
    val isGroupSetup = group != null

    val startDestination = if (isGroupSetup) Screen.Home.route else Screen.Setup.route

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Setup.route) {
            SetupScreen(
                viewModel = viewModel,
                onSetupComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(Screen.Members.route) {
            MembersScreen(
                viewModel = viewModel,
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddMember.route) {
            AddMemberScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MemberProfile.route,
            arguments = listOf(navArgument("memberId") { type = NavType.LongType })
        ) { backStack ->
            val memberId = backStack.arguments?.getLong("memberId") ?: return@composable
            MemberProfileScreen(
                memberId = memberId,
                viewModel = viewModel,
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Meeting.route) {
            MeetingScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AllLoans.route) {
            AllLoansScreen(
                viewModel = viewModel,
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.IssueLoan.route,
            arguments = listOf(navArgument("memberId") { type = NavType.LongType })
        ) { backStack ->
            val memberId = backStack.arguments?.getLong("memberId") ?: return@composable
            IssueLoanScreen(
                memberId = memberId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.LoanDetail.route,
            arguments = listOf(navArgument("loanId") { type = NavType.LongType })
        ) { backStack ->
            val loanId = backStack.arguments?.getLong("loanId") ?: return@composable
            LoanDetailScreen(
                loanId = loanId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Reports.route) {
            ReportsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AiCoach.route,
            arguments = listOf(navArgument("memberId") { type = NavType.LongType })
        ) { backStack ->
            val memberId = backStack.arguments?.getLong("memberId") ?: 0L
            AiCoachScreen(
                memberId = memberId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onReset = {
                    navController.navigate(Screen.Setup.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
