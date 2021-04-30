package com.bignerdranch.android.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.telecom.Call
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_DATE = "date"

class DatePickerFragment: DialogFragment() {

    interface Callbacks{
        fun onDateSelected(date: Date)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val date = arguments?.getSerializable(ARG_DATE) as Date // casting from serializable to date
        val calendar = Calendar.getInstance() // this is needed to get the current instant calendar
        // equivalent result by using GregorianCalendar() which is a subclass
        calendar.time = date
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)
        val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinutes = calendar.get(Calendar.MINUTE)

        //let's initialize the listener
        val dateListener = DatePickerDialog.OnDateSetListener {
            _:DatePicker, year: Int, month: Int, day: Int -> // i was a lot confused about this, they are simply
            // the temporary variables of the lambda functions, so they can be more than one
            val resultDate: Date = GregorianCalendar(year,month,day,initialHour,initialMinutes).time
            targetFragment?.let { fragment ->  
                (fragment as Callbacks).onDateSelected(resultDate)
                // you need this casting because otherwise you will not find the onDateSelected fun
                // this is possible because the fragment implements Callbacks
            }
        }

        return DatePickerDialog(
                requireContext(), // return the context this fragment is associated with (main activity), method of Fragment
                // seems the same as context, but non nullable
                dateListener,
                initialYear,
                initialMonth,
                initialDay
        )

    }

    companion object {
        fun newInstance(date: Date): DatePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATE,date)
            }
            return DatePickerFragment().apply { arguments = args }
        }
    }

}