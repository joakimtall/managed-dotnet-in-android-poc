package com.example.kotlindotnettestapp.dotnet

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer

interface DotNetLibrary : Library {
    fun create_hello(): Pointer?
    fun free_string(ptr: Pointer?)

    companion object {
        val INSTANCE: DotNetLibrary by lazy {
            Native.load("DotNetAndroidLib", DotNetLibrary::class.java)
        }
    }
}
