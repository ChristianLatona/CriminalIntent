package com.bignerdranch.android.criminalintent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.io.File
import java.util.*

class CrimeDetailViewModel: ViewModel() {

    private val crimeRepository = CrimeRepository.get()
    private val crimeIdLiveData = MutableLiveData<UUID>()

    var crimeLiveData: LiveData<Crime?> =
            Transformations.switchMap(crimeIdLiveData) { crimeId -> // seems like the observer, in fact it is
                crimeRepository.getCrime(crimeId) // it detect changes on the MutableLiveData*
            } // this function is very particular, but the documentation is not very clear about this one...
// here we pass only a parameter, not like the doc explained, but this observer counts as a parameter
    fun loadCrime(crimeId: UUID){
        crimeIdLiveData.value = crimeId // *that happens here
    }

    fun saveCrime(crime: Crime){
        crimeRepository.updateCrime(crime)
    }

    fun getPhotoFile(crime: Crime): File{
        return crimeRepository.getPhotoFile(crime)
    }
}