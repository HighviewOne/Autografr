package com.autografr.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.autografr.app.data.local.converter.Converters
import com.autografr.app.data.local.dao.RequestDao
import com.autografr.app.data.local.dao.SignedPhotoDao
import com.autografr.app.data.local.dao.TransactionDao
import com.autografr.app.data.local.dao.UserDao
import com.autografr.app.data.local.entity.RequestEntity
import com.autografr.app.data.local.entity.SignedPhotoEntity
import com.autografr.app.data.local.entity.TransactionEntity
import com.autografr.app.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        SignedPhotoEntity::class,
        RequestEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AutografrDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun signedPhotoDao(): SignedPhotoDao
    abstract fun requestDao(): RequestDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "autografr_db"
    }
}
