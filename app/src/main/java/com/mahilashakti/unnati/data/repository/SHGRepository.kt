package com.mahilashakti.unnati.data.repository

import com.mahilashakti.unnati.data.dao.*
import com.mahilashakti.unnati.data.entity.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Calendar

@Singleton
class SHGRepository @Inject constructor(
    private val groupDao: GroupDao,
    private val memberDao: MemberDao,
    private val savingsDao: SavingsDao,
    private val loanDao: LoanDao,
    private val repaymentDao: RepaymentDao
) {
    // Group
    fun getGroup(): Flow<SHGGroup?> = groupDao.getGroup()
    suspend fun insertGroup(group: SHGGroup) = groupDao.insertGroup(group)
    suspend fun updateGroup(group: SHGGroup) = groupDao.updateGroup(group)
    suspend fun getGroupOnce() = groupDao.getGroupOnce()
    
    suspend fun resetDatabase() {
        groupDao.deleteAllLoans()
        groupDao.deleteAllSavings()
        groupDao.deleteAllMembers()
        groupDao.deleteAllGroups()
    }

    // Members
    fun getActiveMembers(groupId: Long) = memberDao.getActiveMembers(groupId)
    suspend fun insertMember(member: Member) = memberDao.insertMember(member)
    suspend fun updateMember(member: Member) = memberDao.updateMember(member)
    suspend fun getMemberByPhone(phone: String) = memberDao.getMemberByPhone(phone)
    fun getMemberById(id: Long) = memberDao.getMemberById(id)
    suspend fun getMemberByIdOnce(id: Long) = memberDao.getMemberByIdOnce(id)
    fun getMembersWithSavings(groupId: Long) = memberDao.getMembersWithSavings(groupId)
    fun getMemberFull(memberId: Long) = memberDao.getMemberFull(memberId)
    suspend fun getActiveMembersOnce(groupId: Long) = memberDao.getActiveMembersOnce(groupId)

    // Savings
    fun getSavingsForMember(memberId: Long) = savingsDao.getSavingsForMember(memberId)
    fun getTotalGroupCapital() = savingsDao.getTotalGroupCapital()
    fun getTotalSavingsForMember(memberId: Long) = savingsDao.getTotalSavingsForMember(memberId)
    suspend fun getTotalSavingsForMemberOnce(memberId: Long) = savingsDao.getTotalSavingsForMemberOnce(memberId)
    fun getSavingsForWeek(week: Int, year: Int) = savingsDao.getSavingsForWeek(week, year)
    suspend fun getSavingsEntryForMemberWeek(memberId: Long, week: Int, year: Int) =
        savingsDao.getSavingsEntryForMemberWeek(memberId, week, year)
    suspend fun insertSavingsEntry(entry: SavingsEntry) = savingsDao.insertSavingsEntry(entry)
    suspend fun updateSavingsEntry(entry: SavingsEntry) = savingsDao.updateSavingsEntry(entry)
    suspend fun getPaidWeeksCount(memberId: Long) = savingsDao.getPaidWeeksCount(memberId)
    suspend fun getTotalWeeksCount(memberId: Long) = savingsDao.getTotalWeeksCount(memberId)

    // Loans
    fun getLoansForMember(memberId: Long) = loanDao.getLoansForMember(memberId)
    suspend fun getActiveLoanForMember(memberId: Long) = loanDao.getActiveLoanForMember(memberId)
    suspend fun insertLoan(loan: Loan) = loanDao.insertLoan(loan)
    suspend fun updateLoan(loan: Loan) = loanDao.updateLoan(loan)
    fun getActiveLoansCount() = loanDao.getActiveLoansCount()
    fun getTotalOutstanding() = loanDao.getTotalOutstanding()
    fun getLoansWithRepayments(memberId: Long) = loanDao.getLoansWithRepayments(memberId)
    fun getLoanById(loanId: Long) = loanDao.getLoanById(loanId)
    fun getAllActiveLoansWithMember() = loanDao.getAllActiveLoansWithMember()

    // Repayments
    suspend fun insertRepayment(repayment: LoanRepayment) = repaymentDao.insertRepayment(repayment)
    fun getRepaymentsForLoan(loanId: Long) = repaymentDao.getRepaymentsForLoan(loanId)
    suspend fun getRepaymentCount(loanId: Long) = repaymentDao.getRepaymentCount(loanId)

    // Credit Score calculation
    suspend fun calculateAndUpdateCreditScore(memberId: Long, joinDateMs: Long) {
        val paidWeeks = savingsDao.getPaidWeeksCount(memberId)
        val totalWeeks = savingsDao.getTotalWeeksCount(memberId)
        val savingsScore = if (totalWeeks > 0) (paidWeeks.toDouble() / totalWeeks * 50).toInt() else 0

        // Tenure score
        val monthsInGroup = ((System.currentTimeMillis() - joinDateMs) / (1000L * 60 * 60 * 24 * 30)).toInt()
        val tenureScore = (minOf(monthsInGroup.toDouble() / 24.0, 1.0) * 20).toInt()

        // Loan repayment score (simplified)
        val activeLoan = loanDao.getActiveLoanForMember(memberId)
        val loanScore = if (activeLoan == null) 30 else {
            val repayments = repaymentDao.getRepaymentCount(activeLoan.loanId)
            val expected = activeLoan.tenureMonths
            if (expected > 0) (minOf(repayments.toDouble() / expected.toDouble(), 1.0) * 30).toInt() else 15
        }

        val totalScore = (savingsScore + tenureScore + loanScore).coerceIn(0, 100)
        memberDao.updateCreditScore(memberId, totalScore)
    }

    // Monthly report data
    suspend fun getMonthlyReportData(year: Int, month: Int): MonthlyReport {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        val startMs = cal.timeInMillis
        cal.set(year, month - 1, cal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        val endMs = cal.timeInMillis

        val group = groupDao.getGroupOnce() ?: return MonthlyReport()
        val members = memberDao.getActiveMembersOnce(group.groupId)
        val totalCapital = savingsDao.getTotalSavingsForMemberOnce(0) ?: 0.0
        val monthCapital = savingsDao.getGroupCapitalForMonth(startMs, endMs) ?: 0.0
        val activeLoans = loanDao.getAllActiveLoansOnce()

        val memberRows = members.map { member ->
            val savingsThisMonth = savingsDao.getSavingsForMonth(member.memberId, startMs, endMs) ?: 0.0
            val activeLoan = loanDao.getActiveLoanForMember(member.memberId)
            MemberReportRow(
                name = member.name,
                savingsThisMonth = savingsThisMonth,
                loanOutstanding = activeLoan?.outstandingBalance ?: 0.0,
                creditScore = member.creditScore
            )
        }

        return MonthlyReport(
            groupName = group.groupName,
            year = year,
            month = month,
            totalCapital = monthCapital,
            membersCount = members.size,
            activeLoansCount = activeLoans.size,
            totalOutstanding = activeLoans.sumOf { it.outstandingBalance },
            memberRows = memberRows
        )
    }
}

data class MonthlyReport(
    val groupName: String = "",
    val year: Int = 0,
    val month: Int = 0,
    val totalCapital: Double = 0.0,
    val membersCount: Int = 0,
    val activeLoansCount: Int = 0,
    val totalOutstanding: Double = 0.0,
    val memberRows: List<MemberReportRow> = emptyList()
)

data class MemberReportRow(
    val name: String,
    val savingsThisMonth: Double,
    val loanOutstanding: Double,
    val creditScore: Int
)
