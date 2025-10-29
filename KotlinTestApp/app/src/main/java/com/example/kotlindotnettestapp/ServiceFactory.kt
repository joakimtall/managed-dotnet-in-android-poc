package com.example.kotlindotnettestapp

import com.roydammarell.dotnetandroid.HelloAndroidService
import com.roydammarell.dotnetandroid.ExceptionAndroidService

object ServiceFactory {
    fun createHelloService(): IHelloService {
        return HelloServiceWrapper(HelloAndroidService())
    }

    fun createExceptionService(): IExceptionService {
        return ExceptionServiceWrapper(ExceptionAndroidService())
    }
}

class HelloServiceWrapper(private val service: HelloAndroidService) : IHelloService {
    override fun createHello(): String {
        return service.createHello()
    }
}

class ExceptionServiceWrapper(private val service: ExceptionAndroidService) : IExceptionService {
    override fun throwNullReferenceException() {
        service.throwNullReferenceException()
    }
}
