package com.example.kotlindotnettestapp

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.roydammarell.dotnetandroid.HelloAndroidService

class MainActivity : AppCompatActivity() {

    private val helloService by lazy { HelloAndroidService() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val helloMessage = helloService.createHello()
        val helloView = findViewById<TextView>(R.id.helloView)
        helloView.text = helloMessage
        Log.d(TAG, "Received greeting from .NET: $helloMessage")
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
