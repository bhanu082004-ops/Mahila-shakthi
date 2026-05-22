package com.mahilashakti.unnati

import android.content.Context
import com.mahilashakti.unnati.data.AppDatabase
import com.mahilashakti.unnati.data.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        AppDatabase.getDatabase(ctx)

    @Provides fun provideGroupDao(db: AppDatabase): GroupDao = db.groupDao()
    @Provides fun provideMemberDao(db: AppDatabase): MemberDao = db.memberDao()
    @Provides fun provideSavingsDao(db: AppDatabase): SavingsDao = db.savingsDao()
    @Provides fun provideLoanDao(db: AppDatabase): LoanDao = db.loanDao()
    @Provides fun provideRepaymentDao(db: AppDatabase): RepaymentDao = db.repaymentDao()
}
