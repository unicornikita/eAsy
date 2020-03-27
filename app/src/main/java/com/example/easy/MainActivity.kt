package com.example.easy


import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dayview.view.*
import kotlinx.android.synthetic.main.schedule.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private fun showAlertDialog(){
        service.allClasses().enqueue(object: Callback<List<String>>{
            override fun onFailure(call: Call<List<String>>, t: Throwable) {

            }

            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                    val options = response.body()!!.toTypedArray()
                    var selectedItem = 0
                    val builder = AlertDialog.Builder(applicationContext)
                    builder.setTitle("Select an option")
                    builder.setSingleChoiceItems(options, 0) { _: DialogInterface, item: Int ->
                        selectedItem = item
                    }
                builder.setPositiveButton(R.string.okay) { dialogInterface: DialogInterface, p1: Int ->
                    service.setClass(options[p1])?.enqueue(object:  Callback<String?>{
                        override fun onFailure(call: Call<String?>, t: Throwable) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onResponse(call: Call<String?>, response: Response<String?>) {

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
    var retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .build()

    var service: EasyInterface = retrofit.create<EasyInterface>(EasyInterface::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myDataset = arrayOf("PON", "TOR", "SRE", "ČET", "PET")
        val predmeti = arrayOf("MATEMATIKA", "MATEMATIKA", "ANGLEŠČINA", "SLOVENŠČINA")
        viewManager = LinearLayoutManager(this)
        viewAdapter = MyAdapter(myDataset)
        change.setOnClickListener {


        }

        recyclerView = weekdays.apply {
            setHasFixedSize(true)
            adapter = viewAdapter
           layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
        }
        urnik.adapter = ScheduleAdapter(predmeti)
        urnik.layoutManager = LinearLayoutManager(this)

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("TAG", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token


                Log.d("tk", token)
                Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
            })


    }


}

class MyAdapter(private val myDataset: Array<String>) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
    class MyViewHolder(val textView: View) : RecyclerView.ViewHolder(textView)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyAdapter.MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.dayview, parent, false)
        return MyViewHolder(textView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
       holder.textView.day.text = myDataset[position]
        if(position == 0) {
            holder.textView.alpha = 1.0f
        }
        else holder.textView.alpha = 0.5f
    }

    override fun getItemCount() = myDataset.size
}

class ScheduleAdapter(private val myDataset: Array<String>) :
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
      holder.textView.predmet.text = myDataset[position]

        if(position == 0) {
            holder.textView.alpha = 1.0f
        }
        else holder.textView.alpha = 0.5f
    }

    override fun getItemCount() = myDataset.size
}

