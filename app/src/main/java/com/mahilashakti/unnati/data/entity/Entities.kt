package com.mahilashakti.unnati.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "shg_group")
data class SHGGroup(
    @PrimaryKey(autoGenerate = true) val groupId: Long = 0,
    val groupName: String,
    val meetingDay: Int, // 1=Mon..7=Sun
    val weeklyContribution: Double,
    val formationDate: Long, // epoch ms
    val secretaryPin: String,
    val defaultInterestRate: Double = 12.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true) val memberId: Long = 0,
    val groupId: Long,
    val name: String,
    val phone: String,
    val photoUri: String? = null,
    val joinDate: Long,
    val role: String = "MEMBER", // "SECRETARY" or "MEMBER"
    val creditScore: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "savings_entries",
    foreignKeys = [
        ForeignKey(entity = Member::class, parentColumns = ["memberId"], childColumns = ["memberId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("memberId")]
)
data class SavingsEntry(
    @PrimaryKey(autoGenerate = true) val entryId: Long = 0,
    val memberId: Long,
    val meetingDate: Long,
    val amount: Double,
    val status: String, // "PAID" or "PENDING"
    val weekNumber: Int,
    val year: Int,
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "loans",
    foreignKeys = [
        ForeignKey(entity = Member::class, parentColumns = ["memberId"], childColumns = ["memberId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("memberId")]
)
data class Loan(
    @PrimaryKey(autoGenerate = true) val loanId: Long = 0,
    val memberId: Long,
    val principal: Double,
    val annualInterestRate: Double,
    val tenureMonths: Int,
    val simpleInterest: Double,
    val totalRepayable: Double,
    val monthlyInstalment: Double,
    val outstandingBalance: Double,
    val issueDate: Long,
    val status: String = "ACTIVE", // "ACTIVE" or "CLOSED"
    val purpose: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "loan_repayments",
    foreignKeys = [
        ForeignKey(entity = Loan::class, parentColumns = ["loanId"], childColumns = ["loanId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("loanId")]
)
data class LoanRepayment(
    @PrimaryKey(autoGenerate = true) val repaymentId: Long = 0,
    val loanId: Long,
    val amount: Double,
    val paymentDate: Long,
    val paymentMode: String = "Cash",
    val recordedAt: Long = System.currentTimeMillis()
)
