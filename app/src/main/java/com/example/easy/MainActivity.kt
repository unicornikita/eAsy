package com.example.easy


import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.dayview.*
import kotlinx.android.synthetic.main.dayview.view.*
import kotlinx.android.synthetic.main.dayview.view.date
import kotlinx.android.synthetic.main.schedule.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    var selectedItem = 0
    private fun showAlertDialog(){
        service.allClasses().enqueue(object: Callback<List<String>>{
            override fun onFailure(call: Call<List<String>>, t: Throwable) {

            }
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                    val options = response.body()!!.toTypedArray()

                    val builder = AlertDialog.Builder(applicationContext)
                    builder.setTitle("Select an option")
                    builder.setSingleChoiceItems(options, 0) { _: DialogInterface, item: Int ->
                        selectedItem = item
                    }
                builder.setPositiveButton(R.string.okay) { dialogInterface: DialogInterface, p1: Int ->
                    service.setClass(options[selectedItem])?.enqueue(object:  Callback<String?>{
                        override fun onFailure(call: Call<String?>, t: Throwable) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onResponse(call: Call<String?>, response: Response<String?>) {
                            razred.text =  selectedItem.toString().toUpperCase()
                        }

                    })
                    dialogInterface.dismiss()
                }
                builder.setNegativeButton(R.string.cancel) { dialogInterface: DialogInterface, p1: Int ->
                    dialogInterface.dismiss()
                }
                builder.create()
                    builder.show();
            }

        })
    }


    private var retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    var service: EasyInterface = retrofit.create<EasyInterface>(EasyInterface::class.java)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val simpleDateFormat = SimpleDateFormat("LLLL",Locale.forLanguageTag("sl-SI"))
        val dateString = simpleDateFormat.format(Date())
        mesec.text = dateString.toUpperCase()
        val ure = listOf("7.10", "8.00", "8.50", "9.40", "10.30", "11.20", "12.10", "13.00", "13.50")
    service.danes().enqueue(object: Callback<List<vsebina>>{
        override fun onFailure(call: Call<List<vsebina>>, t: Throwable) {
            TODO("Not yet implemented")
        }

        override fun onResponse(call: Call<List<vsebina>>, response: Response<List<vsebina>>) {
            urnik.adapter = response.body()?.let { ScheduleAdapter(it, ure)}
            urnik.layoutManager = LinearLayoutManager(this@MainActivity)

        }

    })
        viewManager = LinearLayoutManager(this)
        viewAdapter = MyAdapter()
        change.setOnClickListener {
            showAlertDialog()
        }

        weekdays.apply {
            setHasFixedSize(true)
            adapter = viewAdapter
           layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
        }


        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("TAG", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
            })
    }
}

class MyAdapter() :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
    private var datum : Date = Date()
    class MyViewHolder(val textView: View) : RecyclerView.ViewHolder(textView),View.OnClickListener{
        init{
            textView.setOnClickListener(this)
        }
        override fun onClick(p0: View?) {
            p0?.isSelected = true
        }

    }
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyAdapter.MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.dayview, parent, false)
        return MyViewHolder(textView)


    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //dnevi po datumu
        val day = SimpleDateFormat("E",Locale.forLanguageTag("sl-SI"))
        holder.textView.day.text = day.format(datum).toUpperCase()
        val simpleDate = SimpleDateFormat("dd",Locale.getDefault())
        val dayInt = simpleDate.format(datum)
        holder.textView.date.text = dayInt
        //danes
        datum.time += 1000*60*60*24
        if(position == 0) {
            holder.textView.alpha = 1.0f
        }
        else holder.textView.alpha = 0.5f
    }

    override fun getItemCount() = 5
}

class ScheduleAdapter(private val myDataset: List<vsebina>,private val ure : List<String>):
    RecyclerView.Adapter<ScheduleAdapter.MyViewHolder>() {

    class MyViewHolder(val textView: View) : RecyclerView.ViewHolder(textView)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ScheduleAdapter.MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.schedule, parent, false)
        return MyViewHolder(textView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
      holder.textView.predmet.text = myDataset[position].predmet
      holder.textView.profesor.text = myDataset[position].profesor
      holder.textView.ura.text = ure[position]


        if(position == 0) {
            holder.textView.alpha = 1.0f
        }
        else holder.textView.alpha = 0.5f
    }

    override fun getItemCount() = myDataset.size
}

