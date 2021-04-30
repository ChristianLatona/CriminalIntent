package com.bignerdranch.android.criminalintent

import android.app.Application

class CriminalIntentApplication: Application() { // i think there is a faster way, but this is the best practice

    override fun onCreate() {
        super.onCreate()
        CrimeRepository.initialize(this)

    }
}