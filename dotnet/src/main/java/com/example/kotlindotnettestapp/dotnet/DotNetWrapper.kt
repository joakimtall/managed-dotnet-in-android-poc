package com.example.kotlindotnettestapp.dotnet

import android.util.Log

object DotNetWrapper {
    fun getDotnetHello(): String? {
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
            Log.e("DotNetWrapper", "Error calling .NET library", e)
            "Error: ${e.message}"
        }
        return helloMessage
    }
    fun getLineItemJson(): String? {
        return try {
            val ptr = DotNetLibrary.INSTANCE.get_line_item_json()
            if (ptr != null) {
                val result = ptr.getString(0, "UTF-8")
                DotNetLibrary.INSTANCE.free_string(ptr)
                result
            } else {
                "Error: Failed to get JSON from .NET"
            }
        } catch (e: Exception) {
            Log.e("DotNetWrapper", "Error calling .NET library", e)
            "Error: ${e.message}"
        }
    }
}