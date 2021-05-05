package com.bignerdranch.android.criminalintent

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val DIALOG_PHOTO = "DialogPhoto"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 1
private const val REQUEST_CONTACT = 2
private const val REQUEST_PHOTO = 3
private const val DATE_FORMAT = "EEE, MMM, dd"
private const val PERMISSION_CONTACTS = 102

class CrimeFragment:Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {

    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var callButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private var photoWidth: Int = 0
    private var photoHeight: Int = 0
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        // crime.suspect = savedInstanceState?.getString(KEY_SUSPECT) ?: ""
        // Log.d(TAG, savedInstanceState?.getString(KEY_SUSPECT) ?: "")
        // crimeDetailViewModel.saveCrime(crime)
        crimeDetailViewModel.loadCrime(crimeId)
    }

    /*override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SUSPECT, crime.suspect)
    }*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        titleField = view.findViewById(R.id.crime_title) as EditText //curioso casting
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        timeButton = view.findViewById(R.id.crime_time) as Button
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        callButton = view.findViewById(R.id.crime_call) as Button
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo) as ImageView
        //si puo fare anche cosi
        //solvedCheckBox.setOnCheckedChangeListener{}
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return
            requestCode == REQUEST_CONTACT && data != null -> {
                val contactURI: Uri = data.data ?: return // data set returned, which needs to be fetched by query
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID) // specify the camps that you want from the query

                val cursor =  // there will happen an SQL query
                        requireActivity().contentResolver.query( // android provides an in depth set of API for contacts in contentProvider
                                // you can access it with contentResolver
                                contactURI, queryFields, null, null, null
                        )

                var contactId: String? = null
                cursor?.use { // a let that handles exceptions
                    if (it.count == 0) {
                        return
                    }
                    // pull out the first column of the first row of data, that is your suspect name
                    it.moveToFirst() // pointing first row of the result
                    val suspect = it.getString(0) // return the value at column index
                    contactId = it.getString(1)
                    // Log.d(TAG,"$contactId")
                    crime.suspect = suspect
                    suspectButton.text = suspect // remember, onActivityResult is called before onViewCreated
                }

                val phoneURI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI // this is the constant 4 the phone number
                val phoneProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val phoneSelection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?" // this is a where clause
                val phoneSelectionArgs = arrayOf(contactId.toString()) // this will replace the ? above
                val phoneCursor = requireActivity().contentResolver.query(
                        phoneURI, phoneProjection, phoneSelection, phoneSelectionArgs, null
                )

                phoneCursor?.use {
                    if (it.count == 0){
                        return
                    }
                    it.moveToFirst()
                    val phoneNumber = it.getString(0)
                    crime.phone = phoneNumber
                }
                crimeDetailViewModel.saveCrime(crime)
            }
            requestCode == REQUEST_PHOTO -> {
                updatePhotoView(photoWidth, photoHeight)
                photoView.announceForAccessibility(photoView.contentDescription)
                requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
                viewLifecycleOwner, { crime ->
            crime?.let { // seems like .let{} is used for nullable values, but it's not only this
                this.crime = crime
                photoFile = crimeDetailViewModel.getPhotoFile(crime)
                photoUri = FileProvider.getUriForFile(requireActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider",
                        photoFile)
                photoView.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener{
                    override fun onGlobalLayout() {
                        photoView.viewTreeObserver.removeOnGlobalLayoutListener(this) // this is jesus for removing memory leaks
                        // but it needs a shit code
                        photoWidth = photoView.width
                        photoHeight = photoView.height
                        updatePhotoView(photoWidth, photoHeight)
                    }
                })
                if (crime.isSolved){
                    solvedCheckBox.contentDescription = getString(R.string.crime_solved_description)
                }else{
                    solvedCheckBox.contentDescription = getString(R.string.crime_not_solved_description)
                }
                updateUI()
            }
        })


    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun onStart() { // we re putting here the initialization of listeners and watchers, as we said previously
        super.onStart() // WHY DO WE PUT LISTENERS ON onStart? IT WORKS PERFECTLY IN onCreateView and its a better practice

        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //removed the TO DO, seems to bug the app
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title=s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                //same
            }
        }
        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
            crime.isSolved=isChecked
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE) // when we instantiate it, make
                // CrimeFragment the target
                show(this@CrimeFragment.parentFragmentManager, DIALOG_DATE)
                // this @ allows you to to get the fragment manager from CrimeFragment instead of searching it in
                // DatePickerFragment, it's specific of apply i think, but i also think it is automatic now
            }
        }

        timeButton.setOnClickListener{
            TimePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_TIME)
                show(this@CrimeFragment.parentFragmentManager, DIALOG_TIME)
                // i tried without the @ notation, it didn't work out, always put it because
                // it will refer the dialogFragment if inside apply
            }
        }

        reportButton.setOnClickListener{
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport()) // curious EXTRA_TEXT, so you con't use proper tags in implicit
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also { intent -> // this also makes you skip a variable, very good
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        suspectButton.apply{
            checkForPermissions(android.Manifest.permission.READ_CONTACTS, "contacts", PERMISSION_CONTACTS)
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)// this constant is for access the contacts
            // i did this with "also", but i will be using the val later on
            // this below is just a test, to see if it works in devices without contacts application
            // pickContactIntent.addCategory(Intent.CATEGORY_HOME)
            setOnClickListener{
                startActivityForResult(pickContactIntent, REQUEST_CONTACT) // i forgot how to get the result back
            }
            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null){
                isEnabled = false
            }
        }

        callButton.setOnClickListener{
            Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${crime.phone}") // this data field is so curious to see
                // we are setting The Uri of the data this intent is now targeting.
            }.also { callIntent ->
                startActivity(callIntent)
            }
        }

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager

            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY)

            if (resolvedActivity == null){
                photoButton.isEnabled = false
            } // we always used this ResolveInfo to check if there is the app in the device that we need

            setOnClickListener{
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                // this is the path of the filesystem that the camera needs to know for taking results
                // infact the extra is called EXTRA_OUTPUT, you are saying the target activity there to save data
                // it magically put the png in the photoUri path

                val cameraActivities: List<ResolveInfo> = packageManager.queryIntentActivities( // i think this can stay outside
                        captureImage,PackageManager.MATCH_DEFAULT_ONLY)

                for (cameraActivity in cameraActivities){
                    requireActivity().grantUriPermission(
                            cameraActivity.activityInfo.packageName,
                            photoUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }

        photoView.setOnClickListener {
            if (photoFile.exists()){
                PhotoZoomFragment.newInstance(photoFile.path).apply {
                    show(this@CrimeFragment.parentFragmentManager, DIALOG_PHOTO)
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    private fun updateUI(){
        // this.titleField.text = crime.title // it requires an editable
        this.titleField.setText(crime.title)
        this.solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        this.dateButton.text = SimpleDateFormat("yyyy, MMM, dd - EEEE", Locale.getDefault()).format(this.crime.date)
        this.timeButton.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(this.crime.date)
        if (crime.suspect.isNotEmpty()) {
            suspectButton.text = crime.suspect
        }
        updatePhotoView(photoWidth, photoHeight)
        // Log.d(TAG,"timeButton text: ${this.crime}")
    }

    private fun updatePhotoView(width: Int, height: Int){
        // Log.d(TAG, "photoFile.exists(): ${photoFile.exists()}")
        if (photoFile.exists()){
            photoView.background = null // removes that horrible gray
            val bitmap = getScaledBitmap(photoFile.path, width, height)
            if (bitmap == null){
                Log.d(TAG, "bitmap is null")
            }
            photoView.setImageBitmap(bitmap)
            photoView.contentDescription = getString(R.string.crime_photo_image_description)
        }else{
            photoView.setImageDrawable(null)
            photoView.contentDescription = getString(R.string.crime_photo_no_image_description)
        }
    }

    private fun checkForPermissions(permission: String, name: String, requestCode: Int){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){ // this runtime permissions exist from marshmallow
            /*if(ContextCompat.checkSelfPermission(activity!!.applicationContext,permission) == PackageManager.PERMISSION_DENIED) {
                callButton.isEnabled = false
                suspectButton.isEnabled = false // this could have be done better, by separating uri from array with different cursors
                Toast.makeText(context, "$name permission refused", Toast.LENGTH_SHORT).show()
            }*/
            when {
                /*ContextCompat.checkSelfPermission(activity!!.applicationContext,permission) == PackageManager.PERMISSION_GRANTED ->{
                    callButton.isEnabled = true
                    suspectButton.isEnabled = true
                }*/
                shouldShowRequestPermissionRationale(permission) -> showDialog(permission, name, requestCode)
                else -> requestPermissions(arrayOf(permission), requestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        fun innerCheck(name: String){
            if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_DENIED){
                callButton.isEnabled = false
                suspectButton.isEnabled = false // this could have be done better, by separating uri from array with different cursors
                Toast.makeText(context, "$name permission refused", Toast.LENGTH_SHORT).show()
            } else {
                callButton.isEnabled = true
                suspectButton.isEnabled = true
                // Toast.makeText(context, "$name permission granted", Toast.LENGTH_SHORT).show()
            }
        }
        when(requestCode) {
            PERMISSION_CONTACTS -> innerCheck("contacts")
        }
    }

    private fun showDialog(permission: String, name: String, requestCode: Int){
        val builder = AlertDialog.Builder(activity)
        builder.apply {
            setMessage("Permission to access your $name is required to use this app")
            setTitle("Permissions required")
            setPositiveButton("OK"){ _, _ ->
                requestPermissions(arrayOf(permission), requestCode)
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun getCrimeReport(): String{
        val solvedString = if(crime.isSolved){
            getString(R.string.crime_report_solved)
        }else{
            getString(R.string.crime_report_unsolved)
        }
        val dateString = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(crime.date)
        val suspect = if (crime.suspect.isBlank()){
            getString(R.string.crime_report_no_suspect)
        }else{
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    override fun onTimeSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    companion object{
        fun newInstance(crimeId: UUID): CrimeFragment{
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return  CrimeFragment().apply { arguments = args } // arguments is a keyword to the place where to stash
            // datas, which is inside a fragment
        }
    }
}