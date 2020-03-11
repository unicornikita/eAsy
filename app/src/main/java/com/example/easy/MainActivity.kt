package com.example.easy


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dayview.view.*
import kotlinx.android.synthetic.main.schedule.view.*


class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myDataset = arrayOf("PON", "TOR", "SRE", "ČET", "PET")
        val predmeti = arrayOf("MATEMATIKA", "MATEMATIKA", "ANGLEŠČINA", "SLOVENŠČINA")
        viewManager = LinearLayoutManager(this)
        viewAdapter = MyAdapter(myDataset)


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
