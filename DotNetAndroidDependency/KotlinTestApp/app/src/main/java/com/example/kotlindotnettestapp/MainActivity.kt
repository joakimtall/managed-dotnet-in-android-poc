package com.example.kotlindotnettestapp

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.util.Log

class MainActivity : Activity() {
    private val TAG = "MainActivity"
    private lateinit var helloService: IHelloService
    private lateinit var exceptionService: IExceptionService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            helloService = ServiceFactory.createHelloService()
            exceptionService = ServiceFactory.createExceptionService()

            val helloView = findViewById<TextView>(R.id.helloView)
            helloView.text = helloService.createHello()
            Log.d(TAG, "Successfully called C# service: ${helloView.text}")

            findViewById<Button>(R.id.throwButton).setOnClickListener {
                Log.d(TAG, "About to call C# exception method")
                exceptionService.throwNullReferenceException()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing services", e)
            findViewById<TextView>(R.id.helloView).text = "Error: ${e.message}"
        }
    }
}
