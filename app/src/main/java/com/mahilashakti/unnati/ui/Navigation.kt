package com.mahilashakti.unnati.ui

sealed class Screen(val route: String) {
    object Setup : Screen("setup")
    object Home : Screen("home")
    object Members : Screen("members")
    object MemberProfile : Screen("member_profile/{memberId}") {
        fun createRoute(memberId: Long) = "member_profile/$memberId"
    }
    object AddMember : Screen("add_member")
    object Meeting : Screen("meeting")
    object AllLoans : Screen("all_loans")
    object IssueLoan : Screen("issue_loan/{memberId}") {
        fun createRoute(memberId: Long) = "issue_loan/$memberId"
    }
    object LoanDetail : Screen("loan_detail/{loanId}") {
        fun createRoute(loanId: Long) = "loan_detail/$loanId"
    }
    object Reports : Screen("reports")
    object AiCoach : Screen("ai_coach/{memberId}") {
        fun createRoute(memberId: Long) = "ai_coach/$memberId"
    }
    object Settings : Screen("settings")
}
