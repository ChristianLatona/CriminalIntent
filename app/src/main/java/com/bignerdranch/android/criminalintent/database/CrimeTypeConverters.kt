package com.bignerdranch.android.criminalintent.database

import androidx.room.TypeConverter
import java.util.*

class CrimeTypeConverters {

    @TypeConverter
    fun fromDate(date: Date?): Long? { // long is 64bit integer
        return date?.time // when you put in database, it will only store long value,
        // its all optional because values can miss in the DB
    }

    @TypeConverter
    fun toDate(millsSinceEpoch: Long?): Date?{ // remember: apply take this and return this,
        // let take this and return its result
        return millsSinceEpoch?.let {
            Date(it)
        }
    }

    @TypeConverter
    fun toUUID(uuid: String?): UUID? {
        return UUID.fromString(uuid)
    }

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

}