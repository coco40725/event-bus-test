package com.blocking.svc

import io.quarkus.vertx.ConsumeEvent
import io.smallrye.common.annotation.Blocking
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped

/**
@author Yu-Jing
@create 2023-07-05-4:49 PM
 */
@ApplicationScoped
class EventBusWithAnnotation {

    @ConsumeEvent("hello")
    @Blocking
    fun consume(map: Map<String, Int>): Int {
        println(Thread.currentThread().name + ", consume event")
        val num = map["num"]?:0
        val times = map["times"]?:0
        val result = num * times

        Thread.sleep(10000)

        return result
    }


    @ConsumeEvent("hello1")
    @Blocking
    fun consumeUni(map: Map<String, Int>): Uni<Int> {
        println(Thread.currentThread().name + ", consume event")
        val num = map["num"]?:0
        val times = map["times"]?:0
        val result = num * times

        Thread.sleep(10000)

        return Uni.createFrom().item(result)
    }

}