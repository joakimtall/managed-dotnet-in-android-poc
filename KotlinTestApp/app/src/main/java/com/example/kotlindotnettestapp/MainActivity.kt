package com.example.kotlindotnettestapp

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlindotnettestapp.dotnet.DotNetWrapper

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val helloMessage = DotNetWrapper.getDotnetHello()

        val helloView = findViewById<TextView>(R.id.helloView)
        helloView.text = helloMessage
        Log.d(TAG, "Received greeting from .NET: $helloMessage")
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}


