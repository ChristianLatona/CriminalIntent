package com.bignerdranch.android.criminalintent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bignerdranch.android.criminalintent.Crime

@Database(entities = [Crime::class], version = 3,exportSchema = false)
@TypeConverters(CrimeTypeConverters::class)
abstract class CrimeDatabase: RoomDatabase() { // room will make this usable with databaseBuilder func later on
    abstract fun crimeDao(): CrimeDao // this too
}

val migration_1_2 = object: Migration(1, 2){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
                "ALTER TABLE Crime ADD COLUMN suspect TEXT NOT NULL DEFAULT ''"
        )
    }
}

val migration_2_3 = object: Migration(2, 3){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
                "ALTER TABLE Crime ADD COLUMN phone TEXT NOT NULL DEFAULT ''"
        )
    }
}