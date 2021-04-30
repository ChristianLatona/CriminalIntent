package com.bignerdranch.android.criminalintent

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment

private const val ARG_FILE_PATH = "filePath"
lateinit var photoBitmap: Bitmap
class PhotoZoomFragment: DialogFragment() {

    /*override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_photo_zoom)

        return view
    }*/

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val view = requireActivity().layoutInflater.inflate(R.layout.dialog_photo_zoom, null)
            // curious but seems very dangerous, we re not passing the viewGroup because its is going in the dialog layout

            val crimePicture = view.findViewById(R.id.crime_picture) as ImageView
            val photoPath = arguments?.getString(ARG_FILE_PATH) as String
            crimePicture.setImageBitmap(getScaledBitmap(photoPath,requireActivity()))

            val builder = AlertDialog.Builder(activity)
            builder.apply {
                setView(view)
                setTitle(R.string.crime_photo)
                setNegativeButton(R.string.dismiss) { _, _ -> dialog?.cancel() } //  DialogInterface.OnClickListener
                // as i wondered, lambda parameters stands outside parenthesis
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }

    companion object{
        fun newInstance(photoPath: String): PhotoZoomFragment {
            val args = Bundle().apply {
                putString(ARG_FILE_PATH, photoPath)
            }
            return PhotoZoomFragment().apply { arguments = args }
        }
    }

}