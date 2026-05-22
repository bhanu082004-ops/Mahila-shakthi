package com.mahilashakti.unnati.data.dao

import androidx.room.*
import com.mahilashakti.unnati.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: SHGGroup): Long

    @Update
    suspend fun updateGroup(group: SHGGroup)

    @Query("SELECT * FROM shg_group LIMIT 1")
    fun getGroup(): Flow<SHGGroup?>

    @Query("SELECT * FROM shg_group LIMIT 1")
    suspend fun getGroupOnce(): SHGGroup?

    @Query("DELETE FROM shg_group")
    suspend fun deleteAllGroups()

    @Query("DELETE FROM members")
    suspend fun deleteAllMembers()

    @Query("DELETE FROM savings_entries")
    suspend fun deleteAllSavings()

    @Query("DELETE FROM loans")
    suspend fun deleteAllLoans()
}

@Dao
interface MemberDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMember(member: Member): Long

    @Update
    suspend fun updateMember(member: Member)

    @Query("SELECT * FROM members WHERE groupId = :groupId AND isActive = 1 ORDER BY name ASC")
    fun getActiveMembers(groupId: Long): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE memberId = :id")
    fun getMemberById(id: Long): Flow<Member?>

    @Query("SELECT * FROM members WHERE memberId = :id")
    suspend fun getMemberByIdOnce(id: Long): Member?

    @Query("SELECT * FROM members WHERE phone = :phone LIMIT 1")
    suspend fun getMemberByPhone(phone: String): Member?

    @Query("SELECT * FROM members WHERE groupId = :groupId AND isActive = 1 ORDER BY name ASC")
    suspend fun getActiveMembersOnce(groupId: Long): List<Member>

    @Query("UPDATE members SET creditScore = :score WHERE memberId = :memberId")
    suspend fun updateCreditScore(memberId: Long, score: Int)

    @Transaction
    @Query("SELECT * FROM members WHERE groupId = :groupId AND isActive = 1 ORDER BY name ASC")
    fun getMembersWithSavings(groupId: Long): Flow<List<MemberWithSavings>>

    @Transaction
    @Query("SELECT * FROM members WHERE memberId = :memberId")
    fun getMemberFull(memberId: Long): Flow<List<MemberFull>>
}

@Dao
interface SavingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsEntry(entry: SavingsEntry): Long

    @Update
    suspend fun updateSavingsEntry(entry: SavingsEntry)

    @Query("SELECT * FROM savings_entries WHERE memberId = :memberId ORDER BY meetingDate DESC")
    fun getSavingsForMember(memberId: Long): Flow<List<SavingsEntry>>

    @Query("SELECT SUM(amount) FROM savings_entries WHERE status = 'PAID'")
    fun getTotalGroupCapital(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM savings_entries WHERE memberId = :memberId AND status = 'PAID'")
    fun getTotalSavingsForMember(memberId: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM savings_entries WHERE memberId = :memberId AND status = 'PAID'")
    suspend fun getTotalSavingsForMemberOnce(memberId: Long): Double?

    @Query("SELECT * FROM savings_entries WHERE weekNumber = :week AND year = :year ORDER BY memberId ASC")
    fun getSavingsForWeek(week: Int, year: Int): Flow<List<SavingsEntry>>

    @Query("SELECT * FROM savings_entries WHERE memberId = :memberId AND weekNumber = :week AND year = :year LIMIT 1")
    suspend fun getSavingsEntryForMemberWeek(memberId: Long, week: Int, year: Int): SavingsEntry?

    @Query("SELECT COUNT(*) FROM savings_entries WHERE memberId = :memberId AND status = 'PAID'")
    suspend fun getPaidWeeksCount(memberId: Long): Int

    @Query("SELECT COUNT(*) FROM savings_entries WHERE memberId = :memberId")
    suspend fun getTotalWeeksCount(memberId: Long): Int

    @Query("SELECT SUM(amount) FROM savings_entries WHERE memberId = :memberId AND status = 'PAID' AND meetingDate BETWEEN :start AND :end")
    suspend fun getSavingsForMonth(memberId: Long, start: Long, end: Long): Double?

    @Query("SELECT SUM(amount) FROM savings_entries WHERE status = 'PAID' AND meetingDate BETWEEN :start AND :end")
    suspend fun getGroupCapitalForMonth(start: Long, end: Long): Double?
}

@Dao
interface LoanDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLoan(loan: Loan): Long

    @Update
    suspend fun updateLoan(loan: Loan)

    @Query("SELECT * FROM loans WHERE memberId = :memberId ORDER BY createdAt DESC")
    fun getLoansForMember(memberId: Long): Flow<List<Loan>>

    @Query("SELECT * FROM loans WHERE memberId = :memberId AND status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveLoanForMember(memberId: Long): Loan?

    @Query("SELECT * FROM loans WHERE loanId = :loanId")
    fun getLoanById(loanId: Long): Flow<Loan?>

    @Query("SELECT COUNT(*) FROM loans WHERE status = 'ACTIVE'")
    fun getActiveLoansCount(): Flow<Int>

    @Query("SELECT SUM(outstandingBalance) FROM loans WHERE status = 'ACTIVE'")
    fun getTotalOutstanding(): Flow<Double?>

    @Transaction
    @Query("SELECT * FROM loans WHERE memberId = :memberId ORDER BY createdAt DESC")
    fun getLoansWithRepayments(memberId: Long): Flow<List<LoanWithRepayments>>

    @Query("SELECT * FROM loans WHERE status = 'ACTIVE'")
    suspend fun getAllActiveLoansOnce(): List<Loan>

    @Transaction
    @Query("SELECT * FROM loans WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getAllActiveLoansWithMember(): Flow<List<LoanWithMember>>

    @Transaction
    @Query("SELECT * FROM loans WHERE loanId = :loanId")
    fun getLoanWithRepaymentsById(loanId: Long): Flow<LoanWithRepayments?>
}

@Dao
interface RepaymentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRepayment(repayment: LoanRepayment): Long

    @Query("SELECT * FROM loan_repayments WHERE loanId = :loanId ORDER BY paymentDate DESC")
    fun getRepaymentsForLoan(loanId: Long): Flow<List<LoanRepayment>>

    @Query("SELECT COUNT(*) FROM loan_repayments WHERE loanId = :loanId")
    suspend fun getRepaymentCount(loanId: Long): Int
}
