package com.example.newsfresh

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsfresh.activity.NewsDetailsActivity
import com.example.newsfresh.model.News
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class NewsListAdapter(var context: Context,private val listener: NewsItemClicked,private var countryList: ArrayList<News>): RecyclerView.Adapter<NewsViewHolder>(),Filterable{

    lateinit var context1:Context
    var items = ArrayList<News>()
    // exampleListFull . exampleList

    init {
        context1=context
        items = countryList as ArrayList<News>
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        val viewHolder = NewsViewHolder(view)

        return viewHolder
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val currentItem = items[position]

                holder.titleView.text = currentItem.title
                holder.author.text = currentItem.author
                holder.time.text = covertTimeToText(currentItem.time)
                Glide.with(holder.itemView.context).load(currentItem.imageUrl).into(holder.image)

holder.linear.setOnClickListener {
    var intent= Intent(context1, NewsDetailsActivity::class.java)
    intent.putExtra("image",currentItem.imageUrl)
    intent.putExtra("title",currentItem.title)
    intent.putExtra("desc",currentItem.desc)
    intent.putExtra("time",currentItem.time)
    intent.putExtra("source",currentItem.publisher)
    context1.startActivity(intent)
}
          holder.textChrome.setOnClickListener {
              listener.onItemClicked(items.get(position))

          }
            }

    fun updateNews(updatedNews: ArrayList<News>) {
        items.clear()
        items.addAll(updatedNews)

        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    items = countryList as ArrayList<News>
                } else {
                    val resultList = ArrayList<News>()
                    for (row in countryList) {
                        if (row.title.toLowerCase().contains(constraint.toString().toLowerCase())) {
                            resultList.add(row)
                        }
                    }
                    items= resultList
                }
                val filterResults = FilterResults()
                filterResults.values = items
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                items = results?.values as ArrayList<News>
                notifyDataSetChanged()
            }
        }
    }
    fun covertTimeToText(dataDate: String?): String? {
        var convTime: String? = null
        val prefix = ""
        val suffix = "Ago"
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val pasTime: Date = dateFormat.parse(dataDate)
            val nowTime = Date()
            val dateDiff: Long = nowTime.getTime() - pasTime.getTime()
            val second: Long = TimeUnit.MILLISECONDS.toSeconds(dateDiff)
            val minute: Long = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
            val hour: Long = TimeUnit.MILLISECONDS.toHours(dateDiff)
            val day: Long = TimeUnit.MILLISECONDS.toDays(dateDiff)
            if (second < 60) {
                convTime = "$second Seconds $suffix"
            } else if (minute < 60) {
                convTime = "$minute Minutes $suffix"
            } else if (hour < 24) {
                convTime = "$hour Hours $suffix"
            } else if (day >= 7) {
                convTime = if (day > 360) {
                    (day / 360).toString() + " Years " + suffix
                } else if (day > 30) {
                    (day / 30).toString() + " Months " + suffix
                } else {
                    (day / 7).toString() + " Week " + suffix
                }
            } else if (day < 7) {
                convTime = "$day Days $suffix"
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            Log.e("ConvTimeE","")
        }
        return convTime
    }

}

class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val titleView: TextView = itemView.findViewById(R.id.title)
    val image: ImageView = itemView.findViewById(R.id.image)
    val author: TextView = itemView.findViewById(R.id.author)
    val time: TextView = itemView.findViewById(R.id.time)
    val textChrome: TextView = itemView.findViewById(R.id.textChrome)
    val linear: LinearLayout = itemView.findViewById(R.id.linear)
}

interface NewsItemClicked {
    fun onItemClicked(item: News)
}