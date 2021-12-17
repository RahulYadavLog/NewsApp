package com.example.newsfresh

import android.app.Application

class MyApplication :Application() {
  companion object {
  public  var selectPublisherList : HashMap<String, String>
            = HashMap<String, String> ()
  }
}