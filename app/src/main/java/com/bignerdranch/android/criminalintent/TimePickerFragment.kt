package com.bignerdranch.android.criminalintent

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.telecom.Call
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_DATE = "date"

class TimePickerFragment: DialogFragment(){

    interface Callbacks {
        fun onTimeSelected(date: Date)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val date = arguments?.getSerializable(ARG_DATE) as Date
        val calendar = Calendar.getInstance()
        calendar.time = date

        // curiosity, there we are setting listener on the onCreateDialog, not on the onStart
        val timeListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            val resultDate = GregorianCalendar(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    hour,
                    minute
            ).time

            targetFragment?.let { fragment ->
                (fragment as Callbacks).onTimeSelected(resultDate)
            }
        }

        return TimePickerDialog(
                requireContext(),
                timeListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        )
    }

    companion object {
        fun newInstance(date: Date): TimePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }

            return TimePickerFragment().apply { arguments = args }
        }
    }
}