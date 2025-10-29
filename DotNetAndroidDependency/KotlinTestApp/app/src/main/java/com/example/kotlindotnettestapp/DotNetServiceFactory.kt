package com.example.kotlindotnettestapp

import com.roydammarell.dotnetandroid.ExceptionAndroidService
import com.roydammarell.dotnetandroid.HelloAndroidService

object DotNetServiceFactory {
    fun createHelloService(): HelloAndroidService = HelloAndroidService()

    fun createExceptionService(): ExceptionAndroidService = ExceptionAndroidService()
}
