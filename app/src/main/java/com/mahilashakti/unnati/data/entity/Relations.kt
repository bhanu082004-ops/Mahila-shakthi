package com.mahilashakti.unnati.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class MemberWithSavings(
    @Embedded val member: Member,
    @Relation(parentColumn = "memberId", entityColumn = "memberId")
    val savings: List<SavingsEntry>
)

data class MemberWithLoans(
    @Embedded val member: Member,
    @Relation(parentColumn = "memberId", entityColumn = "memberId")
    val loans: List<Loan>
)

data class LoanWithRepayments(
    @Embedded val loan: Loan,
    @Relation(parentColumn = "loanId", entityColumn = "loanId")
    val repayments: List<LoanRepayment>
)

data class LoanWithMember(
    @Embedded val loan: Loan,
    @Relation(parentColumn = "memberId", entityColumn = "memberId")
    val member: Member
)

data class MemberFull(
    @Embedded val member: Member,
    @Relation(parentColumn = "memberId", entityColumn = "memberId")
    val savings: List<SavingsEntry>,
    @Relation(
        entity = Loan::class,
        parentColumn = "memberId",
        entityColumn = "memberId"
    )
    val loans: List<LoanWithRepayments>
)
