package com.autografr.app.di

import android.content.Context
import androidx.room.Room
import com.autografr.app.data.local.AutografrDatabase
import com.autografr.app.data.local.dao.RequestDao
import com.autografr.app.data.local.dao.SignedPhotoDao
import com.autografr.app.data.local.dao.TransactionDao
import com.autografr.app.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AutografrDatabase {
        return Room.databaseBuilder(
            context,
            AutografrDatabase::class.java,
            AutografrDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideUserDao(database: AutografrDatabase): UserDao = database.userDao()

    @Provides
    fun provideSignedPhotoDao(database: AutografrDatabase): SignedPhotoDao = database.signedPhotoDao()

    @Provides
    fun provideRequestDao(database: AutografrDatabase): RequestDao = database.requestDao()

    @Provides
    fun provideTransactionDao(database: AutografrDatabase): TransactionDao = database.transactionDao()
}
