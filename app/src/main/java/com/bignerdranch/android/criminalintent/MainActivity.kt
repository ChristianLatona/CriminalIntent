package com.bignerdranch.android.criminalintent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Debug
import android.util.Log
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), CrimeListFragment.Callbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //now that we just learned the FragmentManager, we will give the fragment to manage
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)//it takes layout container
        //supportFragmentManager is just an explicit call to he Activity's FragmentManager
        //you use this thanks to jetpack library and the appCompatActivity class
        if(currentFragment == null){
                val fragment = CrimeListFragment()
                supportFragmentManager
                        .beginTransaction()
                        .add(R.id.fragment_container,fragment) // no need to add to backstack
                        .commit()
        }
    }

    override fun onCrimeSelected(crimeId: UUID) {
        Log.d(TAG,"crimeID: $crimeId")
        val fragment = CrimeFragment.newInstance(crimeId)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container,fragment)
                .addToBackStack(null)
                .commit()
    }

}


