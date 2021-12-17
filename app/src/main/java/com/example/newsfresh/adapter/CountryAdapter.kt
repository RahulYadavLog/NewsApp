package com.example.newsfresh

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.newsfresh.model.CountryModel

class CountryAdapter(context:Context, private var countryList: ArrayList<CountryModel>, private val listener: CountryItemClicked):RecyclerView.Adapter<CountryAdapter.CountryView>()
{

    var items = ArrayList<CountryModel>()
    // exampleListFull . exampleList

    init {
        items = countryList as ArrayList<CountryModel>
    }


    class CountryView(itemView : View):RecyclerView.ViewHolder(itemView)
    {
      var textName=itemView.findViewById<TextView>(R.id.name)
      var textCheck=itemView.findViewById<ImageView>(R.id.itemCheck)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryView {
       val view=LayoutInflater.from(parent.context).inflate(R.layout.country_list_layout,parent,false)
        return CountryAdapter.CountryView(view)
    }


    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: CountryView, position: Int) {
        holder.textName.setText(countryList.get(position).name)



        if (countryList.get(position).check) {
            holder.textName.setTextColor(Color.parseColor("#0C54BE"))
            holder.textCheck.setImageResource(R.drawable.solid_circle)
        }
        else{
            holder.textName.setTextColor(Color.parseColor("#808080"))
            holder.textCheck.setImageResource(R.drawable.circle_background)
        }
        holder.textCheck.setOnClickListener {
            listener.onCountryClicked(position)
        }

    }

    override fun getItemCount(): Int {
     return countryList.size
    }
}
interface CountryItemClicked {
    fun onCountryClicked( pos:Int)
}