package com.example.easy

import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dayview.view.*
import kotlinx.android.synthetic.main.dayview.view.date
import kotlinx.android.synthetic.main.schedule.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*

var chosenDay : View? = null
class MainActivity : AppCompatActivity() {
    private lateinit var viewAdapter: MyAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    var selectedItem = 0

    private val seznamUrnik : MutableList<vsebina> = mutableListOf()
    val ure = listOf("7.10", "8.00", "8.50", "9.40", "10.30", "11.20", "12.10", "13.00", "13.50")
    private val scheduleAdapter = ScheduleAdapter(seznamUrnik,ure)

    private fun showAlertDialog(){
        service.allClasses().enqueue(object: Callback<List<String>>{
            override fun onFailure(call: Call<List<String>>, t: Throwable) {

            }
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                    val options = response.body()!!.toTypedArray()
                    options.sort()
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle("Izberi razred")
                    builder.setSingleChoiceItems(options, 0) { _: DialogInterface, item: Int ->
                        selectedItem = item
                    }
                builder.setPositiveButton(R.string.okay) { dialogInterface: DialogInterface, _: Int ->
                    val preferences = getSharedPreferences("SharedPref", Context.MODE_PRIVATE)
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("notification" + preferences.getString("izbranRazred",""))
                    FirebaseMessaging.getInstance().subscribeToTopic("notification" + options[selectedItem])
                    with(preferences.edit()){
                        putString("izbranRazred", options[selectedItem])
                        apply()
                        razred.text = options[selectedItem]

                    }
                    Log.d("test", options[selectedItem])

                    service.danes(options[selectedItem]).enqueue(object: Callback<List<vsebina>>{
                        override fun onFailure(call: Call<List<vsebina>>, t: Throwable) {
                            throw t
                        }

                        override fun onResponse(
                            call: Call<List<vsebina>>,
                            response: Response<List<vsebina>>
                        ) {

                            seznamUrnik.clear()
                            seznamUrnik.addAll(response.body()!!)
                            scheduleAdapter.notifyDataSetChanged()
                        }
                    })
                    dialogInterface.dismiss()
                }
                builder.setNegativeButton(R.string.cancel) { dialogInterface: DialogInterface, p1: Int ->
                    dialogInterface.dismiss()
                }
                builder.show()
            }

        })
    }


    private var retrofit = Retrofit.Builder()
        .baseUrl("https://easy-matura.ddns.net/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    var service: EasyInterface = retrofit.create(EasyInterface::class.java)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("SharedPref", Context.MODE_PRIVATE)
        razred.text = sharedPref.getString("izbranRazred", "")

        urnik.adapter = scheduleAdapter
        urnik.layoutManager = LinearLayoutManager(this@MainActivity)

        val simpleDateFormat = SimpleDateFormat("LLLL",Locale.forLanguageTag("sl-SI"))
        val dateString = simpleDateFormat.format(Date())
        mesec.text = dateString.toUpperCase()

        val preferences = getSharedPreferences("SharedPref", Context.MODE_PRIVATE)
        val izbranRazred = preferences.getString("izbranRazred", "")
        service.danes(izbranRazred ?: "").enqueue(object: Callback<List<vsebina>>{
            override fun onFailure(call: Call<List<vsebina>>, t: Throwable) {
                throw t
            }

            override fun onResponse(call: Call<List<vsebina>>, response: Response<List<vsebina>>) {
                seznamUrnik.clear()
                seznamUrnik.addAll(response.body()!!)
                scheduleAdapter.notifyDataSetChanged()
            }
        })
        viewManager = LinearLayoutManager(this)
        viewAdapter = MyAdapter()
        viewAdapter.SetOnClickListener(View.OnClickListener {
            chosenDay?.alpha = 0.5f
            it.alpha = 1f
            chosenDay = it
            service.izbranDan(izbranRazred ?: "", it.tag as Int).enqueue(object: Callback<List<vsebina>>{
                override fun onFailure(call: Call<List<vsebina>>, t: Throwable) {
                    throw t
                }

                override fun onResponse(call: Call<List<vsebina>>, response: Response<List<vsebina>>) {
                    seznamUrnik.clear()
                    seznamUrnik.addAll(response.body()!!)
                    scheduleAdapter.notifyDataSetChanged()
                }

            })
        })
        change.setOnClickListener {
            showAlertDialog()
        }

        weekdays.apply {
            setHasFixedSize(true)
            adapter = viewAdapter
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
        }

        Log.d("FCMToken", "token "+ FirebaseInstanceId.getInstance().token)
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("TAG", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
            })
    }
}

class MyAdapter :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
    private var datum : Date = Date()
    private val calendar = Calendar.getInstance()
    private val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
    private lateinit var listener : View.OnClickListener
    init{
        calendar.set(Calendar.DAY_OF_WEEK,calendar.firstDayOfWeek)
        datum = calendar.time

    }

    inner class MyViewHolder(val textView: View) : RecyclerView.ViewHolder(textView),View.OnClickListener{
        init{
            textView.setOnClickListener(this)
        }
        override fun onClick(p0: View?) {
            listener.onClick(p0)
            p0?.isSelected = true
        }

    }
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.dayview, parent, false)
        return MyViewHolder(textView)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        //dnevi po datumu
        if(calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY){

            val day = SimpleDateFormat("E",Locale.forLanguageTag("sl-SI"))
            holder.textView.day.text = day.format(datum).toUpperCase()
            holder.textView.tag = position+1
            val simpleDate = SimpleDateFormat("dd",Locale.getDefault())
            val dayInt = simpleDate.format(datum)
            holder.textView.date.text = dayInt
            //danes
            datum.time += 1000*60*60*24
            if(position == currentDay-1){
                chosenDay = holder.textView
            }
            holder.textView.alpha =
                if(position == currentDay-2) 1.0f
                else 0.5f
        }
    }
    fun SetOnClickListener(listener : View.OnClickListener){
            this.listener = listener
    }

    override fun getItemCount() = 5
}

class ScheduleAdapter(private val myDataset: List<vsebina>,private val ure : List<String>):
    RecyclerView.Adapter<ScheduleAdapter.MyViewHolder>() {

    class MyViewHolder(val textView: View) : RecyclerView.ViewHolder(textView)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.schedule, parent, false)
        return MyViewHolder(textView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

       // if(()
        with(holder.textView){
            predmet.text = myDataset[position].Predmet
            profesor.text = myDataset[position].Profesor
            ura.text = ure[position]
        }

    }

    override fun getItemCount() = myDataset.size
}

