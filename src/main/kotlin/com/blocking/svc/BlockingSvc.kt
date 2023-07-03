package com.blocking.svc

import jakarta.enterprise.context.ApplicationScoped

/**
@author Yu-Jing
@create 2023-07-03-5:22 PM
 */
@ApplicationScoped
class BlockingSvc {

    fun blockingFun5s(num: Int): Int{
        Thread.sleep(5000)
        return num * 2

    }

    fun blockingFun30s(num: Int): Int{
        Thread.sleep(30000)
        return num * 2
    }
}