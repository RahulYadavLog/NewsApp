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
import com.example.newsfresh.model.PublisherModel

class PublisherAdapter(context:Context, private var publisherList: ArrayList<PublisherModel>, private val listener: PublisherItemClicked):RecyclerView.Adapter<PublisherAdapter.PublisherView>()
{

    var items = ArrayList<PublisherModel>()
    // exampleListFull . exampleList

    init {
        items = publisherList as ArrayList<PublisherModel>
    }


    class PublisherView(itemView : View):RecyclerView.ViewHolder(itemView)
    {
      var textName=itemView.findViewById<TextView>(R.id.name)
      var textCheck=itemView.findViewById<ImageView>(R.id.itemCheck)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublisherView {
       val view=LayoutInflater.from(parent.context).inflate(R.layout.country_list_layout,parent,false)
        return PublisherAdapter.PublisherView(view)
    }


    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: PublisherView, position: Int) {
        holder.textName.setText(publisherList.get(position).name)



        if (publisherList.get(position).check) {
            holder.textName.setTextColor(Color.parseColor("#0C54BE"))
            holder.textCheck.setImageResource(R.drawable.solid_circle)
        }
        else{
            holder.textName.setTextColor(Color.parseColor("#808080"))
            holder.textCheck.setImageResource(R.drawable.circle_background)
        }
        holder.textCheck.setOnClickListener {
            listener.onPublisherClicked(position)
        }

    }

    override fun getItemCount(): Int {
     return publisherList.size
    }
}
interface PublisherItemClicked {
    fun onPublisherClicked( pos:Int)
}