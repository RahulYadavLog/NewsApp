package com.example.newsfresh.activity

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.newsfresh.*
import com.example.newsfresh.MyApplication.Companion.selectPublisherList
import com.example.newsfresh.model.CountryModel
import com.example.newsfresh.model.News
import com.example.newsfresh.model.PublisherModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import com.example.newsfresh.EndlessRecyclerOnScrollListener
import com.example.newsfresh.PaginationListener.Companion.PAGE_START


class MainActivity : AppCompatActivity(), NewsItemClicked, CountryItemClicked,
    PublisherItemClicked ,SwipeRefreshLayout.OnRefreshListener{
    var page = 1
    var limit: Int = 10
    private var pageNum = 0
     var strCode:String="in"
     var spinnerSort:String="Newest"
    var isFilter=false
    var isCountryFilter=false
    var isloding = false
    private var currentPage: Int = PAGE_START
    private var isLastPage = false
    private var lastPage = false
    private val totalPage = 10
    var start:Int=0
    var end: Int=10
    private var isLoading = false
    var itemCount = 0
    val numberList: MutableList<String> = ArrayList()
    lateinit var mAdapter: NewsListAdapter
    private var requestQueue: RequestQueue? = null
    lateinit var layoutManager: LinearLayoutManager
    var tempArrayList = ArrayList<News>()
    var uniqueArrayList = ArrayList<News>()
    val newsArray = ArrayList<News>()
    var countyList = listOf("Australia","Egypt ","USA","India","United Arab Emirates","Argentina","Austria")
    var countycodeList = listOf("au","eg ","us","in","ae","ar","at")
    var publisherList = ArrayList<String>()
    var uniquepublisherList = ArrayList<String>()
    lateinit var countryAdapter: CountryAdapter
    lateinit var cLayoutManager: LinearLayoutManager
    var countryArrayList = ArrayList<CountryModel>()
    var countryData = ArrayList<CountryModel>()

    lateinit var publisherAdapter: PublisherAdapter
    lateinit var pLayoutManager: LinearLayoutManager
    var publisherArrayList = ArrayList<PublisherModel>()
      var publisherHashMap : HashMap<String, String>
            = HashMap<String, String> ()
    var publisherData = ArrayList<PublisherModel>()
    lateinit var  mBottomSheetDialog:BottomSheetDialog
    lateinit var  newsSource:JSONObject
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar()
        requestQueue = Volley.newRequestQueue(this)
        swipeRef.setOnRefreshListener(this);
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        fetchData1(strCode)
        mAdapter = NewsListAdapter(this,this, newsArray)
        recyclerView.adapter = mAdapter
        countryName.setText("india")

        location.setOnClickListener {
            openBottomSheetCountry()
        }
        fab_publisher.setOnClickListener {
            openBottomSheetPublisher()
            publisherAdapter.notifyDataSetChanged()
        }

        val newsOrder = resources.getStringArray(R.array.NewsOrder)

        // access the spinner
        val spinner = findViewById<Spinner>(R.id.spinner)
        if (spinner != null) {
            val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, newsOrder)
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View, position: Int, id: Long) {

                    spinnerSort=newsOrder[position]
                    refresh()
                         }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }


        editSearch.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {

        }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mAdapter?.filter?.filter(s)

            }

        })



        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (isFilter ==false) {
                    if (lastPage == false) {

                        if (dy > 0) {
                            val visibleItemCount: Int = layoutManager.childCount
                            val postVisibleItem: Int =
                                layoutManager.findFirstCompletelyVisibleItemPosition()
                            val total: Int = mAdapter.itemCount
                            if (!isloding) {
                                if (visibleItemCount + postVisibleItem >= total) {
                                    page++
                                    fetchData1(strCode)
                                }
                            }
                        }

                        super.onScrolled(recyclerView, dx, dy)
                    }
                }
            }
        })

    }



    override fun onItemClicked(item: News) {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(item.url))
    }



    @RequiresApi(Build.VERSION_CODES.M)
    private fun fetchData1(code:String) {
        //volly library
        try {

        isloding = true
        progressBar.visibility = View.VISIBLE

        swipeRef.setEnabled(false)
        swipeRef.setRefreshing(true)
        var online: Boolean = isOnline(this)
        if (online) {

            progressBar.visibility = View.VISIBLE
            mainlayout.visibility = View.VISIBLE
            nointernet.visibility = View.GONE


            if (isCountryFilter==true)
            {
                selectPublisherList.clear()
                publisherArrayList.clear()
                isCountryFilter=false
            }
            strCode = code
            if (page==1)
            {
                if(isFilter==true) {
                    start=0
                    page=1
                    isFilter==false
                    publisherList.clear()
                    publisherHashMap.clear()
                }
            }
            else if (page>1&&isFilter==true){
                start=0
                page=1
                isFilter==false
                publisherList.clear()
                publisherHashMap.clear()
            }

//        newsArray.clear()
//       newsArray.clear()
            val url =
                "http://newsapi.org/v2/top-headlines?country=$code&apiKey=7c10f759c3974e7f91cea03560ab0475&page=${page}"
            //making a request
            val jsonObjectRequest = object : JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                Response.Listener {
                    val newsJsonArray = it.getJSONArray("articles")
                    if (newsJsonArray.length()==0)
                    {
                        lastPage=true
                    }
                    else{
                        Toast.makeText(this,"${page}Page Data",Toast.LENGTH_LONG).show()
                    }
                    for (i in 0 until newsJsonArray.length()) {
                        val newsJsonObject = newsJsonArray.getJSONObject(i)

                        lastPage=false
                        newsSource = newsJsonObject.getJSONObject("source")
                        val news = News(
                            newsJsonObject.getString("title"),
                            newsJsonObject.getString("author"),
                            newsJsonObject.getString("url"),
                            newsJsonObject.getString("urlToImage"),
                            newsJsonObject.getString("publishedAt"),
                            newsSource.getString("name"),
                            newsJsonObject.getString("description")

                        )
                        publisherList.add(newsSource.getString("name"))
                        newsArray.add(news)

                    }
//                if(tempArrayList.size>0) {
//                    tempArrayList.clear()
//                }
                    try {


                    if (MyApplication.selectPublisherList.size > 0) {
                        tempArrayList.clear()
                        for (i in 0 until publisherList.size) {
                            if (MyApplication.selectPublisherList.containsValue(publisherList.get(i))) {

                                tempArrayList.add(newsArray.get(i))
                            }
                        }
                    } else if (lastPage==false){
                        tempArrayList= (newsArray).toSet().toList() as ArrayList<News>

                    }
                    }catch (e:Exception)

                    {

                    }

                    if (lastPage==false && isFilter==false) {

                        if (spinnerSort=="Newest")
                        {
                            ascending()
                            mAdapter.updateNews(uniqueArrayList)

                        }
                        else
                        {
                            descending()
                            mAdapter.updateNews(uniqueArrayList)

                        }
//                        uniqueArrayList= (tempArrayList)
                    }
                    else if (isFilter==true){
                        if (spinnerSort=="Newest")
                        {
                            ascending()
                            mAdapter.updateNews(uniqueArrayList)

                        }
                        else
                        {
                            descending()
                            mAdapter.updateNews(uniqueArrayList)

                        }
//                        uniqueArrayList= (tempArrayList)
                    }
                    start=tempArrayList.size
                    end=start+10
                    mAdapter.notifyDataSetChanged()
                    progressBar.visibility = View.GONE
                    isloding = false
                    isLoading=false
                    isFilter=false
                    swipeRef.setRefreshing(false)
                    swipeRef.setEnabled(true)


                },
                Response.ErrorListener {
                }

            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["User-Agent"] = "Mozilla/5.0"
                    return headers
                }
            }

            MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
        }
        else{
            swipeRef.setRefreshing(false)
            swipeRef.setEnabled(true)
            nointernet.visibility = View.VISIBLE
            mainlayout.visibility = View.GONE

        }
    }catch (e:Exception)
    {
        isFilter==false
        selectPublisherList.clear()
        publisherList.clear()
        publisherArrayList.clear()
        publisherHashMap.clear()
        newsArray.clear()
        tempArrayList.clear()
        page=1
       fetchData1(strCode)

    }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getPage() {

        isloding = true
        progressBar.visibility = View.VISIBLE
        val start: Int = (page - 1) * limit
        val end: Int = (page) * limit
      fetchData1(strCode)
        Handler().postDelayed(
            {
                if (::mAdapter.isInitialized) {
                    mAdapter.notifyDataSetChanged()
                } else {
                    mAdapter = NewsListAdapter(this, this,newsArray)
                    recyclerView.adapter = mAdapter

                }
                progressBar.visibility = View.GONE

            }, 5000
        )
    }



    private fun openBottomSheetCountry() {
         mBottomSheetDialog = BottomSheetDialog(this
                , R.style.AppBottomSheetDialogTheme
         )
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_county, null)
        mBottomSheetDialog.setContentView(sheetView)
    var countryRecyclerView:RecyclerView=sheetView.findViewById(R.id.country_recyclerview)
        countryData.clear()
        if (countryArrayList.size>0) {

        }
        else
        {
            for (i in 0 until countyList.size) {
                val country = CountryModel(countyList.get(i), countycodeList.get(i), false)
                countryData.add(country)
            }
        }
        countryArrayList.addAll(countryData)
        cLayoutManager= LinearLayoutManager(this)
        countryRecyclerView.layoutManager = cLayoutManager
//        fetchData1()
        countryAdapter = CountryAdapter(this, countryArrayList,this)
        countryRecyclerView.adapter = countryAdapter

        mBottomSheetDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun openBottomSheetPublisher() {
        mBottomSheetDialog = BottomSheetDialog(this
            , R.style.AppBottomSheetDialogTheme
        )
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_publisher, null)
        mBottomSheetDialog.setContentView(sheetView)
        var countryRecyclerView:RecyclerView=sheetView.findViewById(R.id.country_recyclerview)
        var btnApply: Button =sheetView.findViewById(R.id.btnApply)
        publisherData.clear()
        btnApply.setOnClickListener {
            isFilter=true
            tempArrayList.clear()
            newsArray.clear()
            fetchData1(strCode)
            mAdapter.notifyDataSetChanged()
            mBottomSheetDialog.dismiss()


        }
        if (publisherArrayList.size>0) {

        }
        else
        {
            uniquepublisherList= publisherList.toSet().toList() as ArrayList<String>
            for (i in 0 until uniquepublisherList.size) {

                publisherHashMap.put(uniquepublisherList.get(i),uniquepublisherList.get(i))

            }
            for ((k, v) in publisherHashMap)
            {
                val country = PublisherModel(k, false)
                publisherData.add(country)
            }
        }
        publisherArrayList.addAll(publisherData)
        pLayoutManager= LinearLayoutManager(this)
        countryRecyclerView.layoutManager = pLayoutManager
        publisherAdapter = PublisherAdapter(this,publisherArrayList,this)
        countryRecyclerView.adapter = publisherAdapter

        mBottomSheetDialog.show()
    }

    fun toolbar()
    {
        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        toolbar?.title = "Androidly"
        toolbar?.subtitle = "Sub"
        toolbar?.navigationIcon = ContextCompat.getDrawable(this,
            R.drawable.ic_baseline_arrow_back_24
        )
        toolbar?.setNavigationOnClickListener { Toast.makeText(applicationContext,"Navigation icon was clicked",Toast.LENGTH_SHORT).show() }
    }

    override fun onPublisherClicked(pos: Int)
    {  if (publisherArrayList.get(pos).check) {
        publisherArrayList.get(pos).check=false
        selectPublisherList.remove(publisherArrayList.get(pos).name)

    } else {
        selectPublisherList.put(publisherArrayList.get(pos).name,publisherArrayList.get(pos).name)
        publisherArrayList.get(pos).check=true
    }
        publisherAdapter.notifyDataSetChanged()


//            mBottomSheetDialog.dismiss()
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCountryClicked(pos: Int) {
        if (countryArrayList.get(pos).check) {
            countryArrayList.get(pos).check=false
            isFilter=false
        } else {
            for (i in 0 until countryArrayList.size)
            {
                countryArrayList.get(i).check=false
            }
            isFilter=true
            isCountryFilter=true
            mBottomSheetDialog.dismiss()
            countryArrayList.get(pos).check=true
        }

        newsArray.clear()
        tempArrayList.clear()
        selectPublisherList.clear()
        fetchData1(countryArrayList.get(pos).code)
        countryName.setText(countryArrayList.get(pos).name)
        countryAdapter.notifyDataSetChanged()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRefresh() {
        refresh()
        }
    fun descending()
    {
        var sortedList = tempArrayList.sortedWith(compareBy({ it.time }))

        for (obj in sortedList) {
            uniqueArrayList.add(obj)
        }
        mAdapter.notifyDataSetChanged()
    }

    fun ascending()
    {
        uniqueArrayList=tempArrayList
        mAdapter.notifyDataSetChanged()

    }
    @RequiresApi(Build.VERSION_CODES.M)
    fun refresh()
    {
        selectPublisherList.clear()
        publisherArrayList.clear()
        itemCount = 0
        page=1
        currentPage = PAGE_START
        isLastPage = false
        isCountryFilter=false
        newsArray.clear()
        tempArrayList.clear()
        fetchData1(strCode)

    }
}