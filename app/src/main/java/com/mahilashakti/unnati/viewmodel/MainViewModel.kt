package com.mahilashakti.unnati.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahilashakti.unnati.data.entity.*
import com.mahilashakti.unnati.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo: SHGRepository
) : ViewModel() {

    val group: StateFlow<SHGGroup?> = repo.getGroup()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalCapital: StateFlow<Double> = repo.getTotalGroupCapital()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val activeLoansCount: StateFlow<Int> = repo.getActiveLoansCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalOutstanding: StateFlow<Double> = repo.getTotalOutstanding()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val membersWithSavings: StateFlow<List<MemberWithSavings>> = group
        .filterNotNull()
        .flatMapLatest { repo.getMembersWithSavings(it.groupId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allActiveLoans: StateFlow<List<LoanWithMember>> = repo.getAllActiveLoansWithMember()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveGroup(group: SHGGroup, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.insertGroup(group)
            onDone()
        }
    }

    fun updateGroup(group: SHGGroup) {
        viewModelScope.launch { repo.updateGroup(group) }
    }
    
    fun resetApp(onDone: () -> Unit) {
        viewModelScope.launch {
            repo.resetDatabase()
            onDone()
        }
    }

    fun addMember(member: Member, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val existing = repo.getMemberByPhone(member.phone)
            if (existing != null) {
                onResult(false, "A member with this phone number already exists.")
                return@launch
            }
            repo.insertMember(member)
            onResult(true, "Member added successfully!")
        }
    }

    fun updateMember(member: Member) {
        viewModelScope.launch { repo.updateMember(member) }
    }

    // Meeting / savings
    fun recordSavings(
        memberId: Long,
        amount: Double,
        status: String,
        meetingDate: Long,
        weekNumber: Int,
        year: Int,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val existing = repo.getSavingsEntryForMemberWeek(memberId, weekNumber, year)
            if (existing != null) {
                repo.updateSavingsEntry(existing.copy(status = status, amount = amount, meetingDate = meetingDate))
            } else {
                repo.insertSavingsEntry(SavingsEntry(memberId = memberId, meetingDate = meetingDate, amount = amount, status = status, weekNumber = weekNumber, year = year))
            }
            // Update credit score
            val member = repo.getMemberByIdOnce(memberId)
            member?.let { repo.calculateAndUpdateCreditScore(memberId, it.joinDate) }
            onDone()
        }
    }

    // Loan
    fun issueLoan(
        memberId: Long,
        principal: Double,
        rate: Double,
        tenureMonths: Int,
        purpose: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val active = repo.getActiveLoanForMember(memberId)
            if (active != null) {
                onResult(false, "Member has an existing active loan of ₹${String.format("%,.2f", active.outstandingBalance)}. Please clear it first.")
                return@launch
            }
            val si = (principal * rate * (tenureMonths / 12.0)) / 100.0
            val total = principal + si
            val monthly = total / tenureMonths
            repo.insertLoan(Loan(
                memberId = memberId,
                principal = principal,
                annualInterestRate = rate,
                tenureMonths = tenureMonths,
                simpleInterest = si,
                totalRepayable = total,
                monthlyInstalment = monthly,
                outstandingBalance = total,
                issueDate = System.currentTimeMillis(),
                purpose = purpose
            ))
            onResult(true, "Loan issued successfully!")
        }
    }

    fun recordRepayment(
        loan: Loan,
        amount: Double,
        paymentMode: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            if (amount > loan.outstandingBalance) {
                onResult(false, "Payment ₹${String.format("%,.2f", amount)} exceeds outstanding ₹${String.format("%,.2f", loan.outstandingBalance)}.")
                return@launch
            }
            repo.insertRepayment(LoanRepayment(loanId = loan.loanId, amount = amount, paymentDate = System.currentTimeMillis(), paymentMode = paymentMode))
            val newBalance = loan.outstandingBalance - amount
            val newStatus = if (newBalance <= 0.01) "CLOSED" else "ACTIVE"
            repo.updateLoan(loan.copy(outstandingBalance = newBalance.coerceAtLeast(0.0), status = newStatus))
            // Update credit score
            val member = repo.getMemberByIdOnce(loan.memberId)
            member?.let { repo.calculateAndUpdateCreditScore(loan.memberId, it.joinDate) }
            onResult(true, if (newStatus == "CLOSED") "Loan fully repaid! 🎉" else "Repayment recorded. Outstanding: ₹${String.format("%,.2f", newBalance)}")
        }
    }

    suspend fun getMonthlyReport(year: Int, month: Int): MonthlyReport {
        return repo.getMonthlyReportData(year, month)
    }

    fun getCurrentWeekAndYear(): Pair<Int, Int> {
        val cal = Calendar.getInstance()
        return Pair(cal.get(Calendar.WEEK_OF_YEAR), cal.get(Calendar.YEAR))
    }

    fun verifyPin(inputPin: String): Boolean {
        return group.value?.secretaryPin == inputPin
    }
}
