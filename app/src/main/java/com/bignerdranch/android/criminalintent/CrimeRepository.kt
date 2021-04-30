package com.bignerdranch.android.criminalintent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.bignerdranch.android.criminalintent.database.CrimeDatabase
import com.bignerdranch.android.criminalintent.database.migration_1_2
import com.bignerdranch.android.criminalintent.database.migration_2_3
import java.io.File
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context){ // clear usage of a private constructor
    // this structure allow you to initialize a repository only once, in fact is a singleton
    // but, why not using the object declaration? because it cannot have a constructor

    private val database: CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java, // here it doesn't need a kclass, they built it like this idk why
        DATABASE_NAME
    ).addMigrations(migration_1_2, migration_2_3)
            .build()

    private val crimeDao = database.crimeDao()
    private val executor = Executors.newSingleThreadExecutor()
    private val filesDir = context.applicationContext.filesDir // absolute path of the filesDir of this application


    fun getPhotoFile(crime: Crime): File = File(filesDir, crime.photoFileName) // then you append the photoFileName
    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()
    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)
    fun updateCrime(crime: Crime){
        executor.execute{
            crimeDao.updateCrime(crime)
        }
    }
    fun addCrime(crime: Crime){
        executor.execute{
            crimeDao.addCrime(crime)
        }
    }

    companion object{
        private var INSTANCE: CrimeRepository? = null

        fun initialize(context: Context){
            if (INSTANCE == null){
                INSTANCE = CrimeRepository(context)
            }
        }

        fun get(): CrimeRepository {
            return INSTANCE ?: throw IllegalStateException("Repository must be initialized")
        }
    }
}
/*
object CrimeRepository2{ // object declarations cannot have constructor, big bullshit
    private val database: CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java, // here it doesn't need a kclass, they built it like this idk why
        DATABASE_NAME
    ).build()

    private val crimeDao = database.crimeDao()

    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()
    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)
}
*/

