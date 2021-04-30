package com.bignerdranch.android.criminalintent.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bignerdranch.android.criminalintent.Crime
import java.util.*

@Dao
interface CrimeDao {

    @Query("SELECT * from crime") // where is the table defined as "crime"?, i guess its the Crime class entity
    fun getCrimes(): LiveData<List<Crime>> // yes, it is the name of the entity in lower case

    @Query("SELECT * from crime WHERE id=(:id)") // <- this :id is the input of fun below
    fun getCrime(id: UUID): LiveData<Crime?>

    @Update // ??? seems super comfortable
    fun updateCrime(crime: Crime)

    @Insert // in order to do these operations of insert and update, you must use an executor, don't rely on Room
    // automatically running these on a background thread, do it yourself with executor
    fun addCrime(crime: Crime)

} // these LiveData "are a background thread"