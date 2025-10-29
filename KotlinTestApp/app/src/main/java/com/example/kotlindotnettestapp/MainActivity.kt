package com.example.kotlindotnettestapp

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val helloMessage = try {
            val ptr = DotNetLibrary.INSTANCE.create_hello()
            if (ptr != null) {
                val result = ptr.getString(0, "UTF-8")
                DotNetLibrary.INSTANCE.free_string(ptr)
                result
            } else {
                "Error: Failed to get message from .NET"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling .NET library", e)
            "Error: ${e.message}"
        }

        val helloView = findViewById<TextView>(R.id.helloView)
        helloView.text = helloMessage
        Log.d(TAG, "Received greeting from .NET: $helloMessage")
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
