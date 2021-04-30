package com.bignerdranch.android.criminalintent

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import java.util.*

class CrimeListViewModel: ViewModel() {

    /*val crimes = mutableListOf<Crime>()
    //very intresting, i can set this and other lists to val and still works, but not with an array
    //theorically in java arrays have fixed length

    init {
        for (i in 0..100){
            val crime = Crime()
            crime.title = "Crime #$i"
            crime.isSolved = i % 2 == 0
            crimes += crime
        }
    }*/

    private val crimeRepository = CrimeRepository.get()
    val crimeListLiveData = crimeRepository.getCrimes()

    fun addCrime(crime: Crime){
        crimeRepository.addCrime(crime)
    }
}