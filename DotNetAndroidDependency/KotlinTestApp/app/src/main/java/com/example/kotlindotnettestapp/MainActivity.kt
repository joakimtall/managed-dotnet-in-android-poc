package com.example.kotlindotnettestapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val helloService by lazy { DotNetServiceFactory.createHelloService() }
    private val exceptionService by lazy { DotNetServiceFactory.createExceptionService() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val helloMessage = helloService.createHello()
        val helloView = findViewById<TextView>(R.id.helloView)
        helloView.text = helloMessage
        Log.d(TAG, "Received greeting from .NET: $helloMessage")

        findViewById<Button>(R.id.throwButton).setOnClickListener {
            try {
                exceptionService.throwNullReferenceException()
            } catch (t: Throwable) {
                Log.e(TAG, "Exception thrown from .NET layer", t)
                Toast.makeText(
                    this,
                    t.message ?: t::class.java.simpleName,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
