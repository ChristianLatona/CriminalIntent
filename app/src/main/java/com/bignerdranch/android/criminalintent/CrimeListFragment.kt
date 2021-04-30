package com.bignerdranch.android.criminalintent

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "CrimeListFragment"
class CrimeListFragment: Fragment() {

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this).get(CrimeListViewModel::class.java)
    }
    private lateinit var crimeRecyclerView: RecyclerView
    // private var adapter: CrimeListAdapter = CrimeListAdapter(emptyList()) // I guess there will be the case the recyclerView is empty
    /**
     *REQUIRED INTERFACE FOR CALLBACK FUNCTIONS
     */
    interface Callbacks{ // this is needed because MainActivity needs to implement
        fun onCrimeSelected(crimeId: UUID) // the onCrimeSelected method to continue app
    }
    private var callbacks: Callbacks? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list,container,false)
        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        // recyclerView requires a LayoutManager to work, it doesn't position items by itself

        crimeRecyclerView.adapter = CrimeListAdapter()
        // crimeRecyclerView.adapter = CrimeAdapter(crimeListViewModel.crimes)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimeListLiveData.observe(
            viewLifecycleOwner, // in theory 2nd parameter would use androidx.lifecycle.Observer, but it's just redundant
            { crimes ->
                // Log.d(TAG,"crimes: $crimes")
                crimes?.let {
                    // Log.i(TAG,"got crimes ${crimes.size}")
                    updateUI(crimes)
                }
            }
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.new_crime -> {
                val crime = Crime()
                crimeListViewModel.addCrime(crime) // adding an empty crime for now, divide problems in sub-problems
                callbacks?.onCrimeSelected(crime.id) // let's open the created crime
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    private fun updateUI(crimes: List<Crime>){ // in the future this will get filled, as configuring UI gets more involved
        // adapter = CrimeListAdapter(crimes)
        // crimeRecyclerView.adapter = adapter
        when (crimes.size){
            0 -> crimeRecyclerView.adapter = EmptyCrimeAdapter()
            1 -> {
                crimeRecyclerView.adapter = CrimeListAdapter()
                (crimeRecyclerView.adapter as CrimeListAdapter).submitList(crimes)
            }
            else -> (crimeRecyclerView.adapter as CrimeListAdapter).submitList(crimes)
        }
    }

    private inner class EmptyCrimeHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {

        private val crimesEmptyTextView: TextView = itemView.findViewById(R.id.crime_empty)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(){
            crimesEmptyTextView.visibility = View.VISIBLE
        }

        override fun onClick(v: View?) {
            val crime = Crime()
            crimeListViewModel.addCrime(crime)
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    private inner class EmptyCrimeAdapter(): RecyclerView.Adapter<EmptyCrimeHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmptyCrimeHolder {
            val crimeView = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            return EmptyCrimeHolder(crimeView)
        }

        override fun onBindViewHolder(holder: EmptyCrimeHolder, position: Int) {
            holder.bind()
        }

        override fun getItemCount(): Int {
            return 1
        }
    }

    private inner class CrimeHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener{ // complicatino questo utilizzo di view

        lateinit var crime: Crime

        val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)
        val titleTextView: TextView = itemView.findViewById(R.id.crime_title) // note: it doesn't use the view parameter
        val dateTextView: TextView = itemView.findViewById(R.id.crime_date) // crimeHolder is used only for Adapter.onCreateViewHolder
        // things like itemView are referred as fields (I'm not sure), i think putting view instead of itemView is the same,
        // because the passed view will be the list_item

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime){
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = SimpleDateFormat("EEEE, MMM, dd, yyyy", Locale.getDefault()).format(this.crime.date)//.toUpperCase(Locale.getDefault())
                    // EEEE show full name, no sense
                    // DateFormat.getDateInstance(DateFormat.MEDIUM).format(this.crime.date)
                    // this.crime.date.toString()
            if(crime.isSolved){
                solvedImageView.visibility = View.VISIBLE
                itemView.contentDescription = "${titleTextView.text}, ${dateTextView.text}, ${solvedImageView.contentDescription}"
            }else{
                solvedImageView.visibility = View.GONE
                itemView.contentDescription = "${titleTextView.text}, ${dateTextView.text}"
            }
        }

        override fun onClick(v: View?) {
            //Toast.makeText(context,"${crime.title} pressed",Toast.LENGTH_SHORT).show()
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    private inner class CrimeListAdapter()
        : androidx.recyclerview.widget.ListAdapter<Crime, CrimeHolder>(DiffCallBack()) {

        // the doc says that u must add parameter to list adapter, i didn't find out
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val crimeView = layoutInflater.inflate(R.layout.list_item_crime, parent, false) // strange parent, inner class
            return CrimeHolder(crimeView)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = getItem(position) //crimes[position]
            holder.bind(crime)
        }

        // override fun getItemCount(): Int = crimes.size
        // this made me crazy, the subclass listAdapter completely bugs if you don't remove getItemCount()
    }

    private inner class DiffCallBack(): DiffUtil.ItemCallback<Crime>() {
        override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem.id == newItem.id // this passage is needed, first it find with comparing the item
        }

        override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem == newItem
            // check every property manually, but needs just to compare the whole object
        }

    }

}
