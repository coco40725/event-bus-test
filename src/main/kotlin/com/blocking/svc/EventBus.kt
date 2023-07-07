package com.blocking.svc

import com.blocking.entity.Food
import com.blocking.entity.FoodMessageCodec
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import jakarta.enterprise.context.ApplicationScoped

/**
@author Yu-Jing
@create 2023-07-05-9:12 AM
 */

/**
 * 順序流程： (request-response pattern.)
 * 1. acceptor 會接收 request, 然後把需要處理的資料包裝成 event 丟給 (調用 request function) eventbus
 * 2. eventbus 接收到這個 event後, worker 會領取這個 event (調用 consume function), 然後進行處理
 * 3. 待 worker 資料處理完畢後, 會調用 reply 方法，告知 acceptor 東西已經做好了並把東西傳給 acceptor
 * 4. acceptor 則可以使用 onComplete 來獲取 worker 傳來的結果
 */
class EventBus {
}

/**
 * Standard Verticles:
 * they are always executed using an event loop thread
 */
@ApplicationScoped
class AcceptorVerticle : AbstractVerticle(){
    override fun start() {
        println(Thread.currentThread().name + ", Start Acceptor...")

        // '調用' bus
        val bus = this.vertx.eventBus()

        // 發送消息給 bus, 我們需要 worker 幫我們計算 num * times 再等待 10s  並回傳結果
        bus.request<Int>("my/first/address", mapOf("num" to 2, "times" to 3))
                .onComplete{ar ->
                    if (ar.succeeded()) {
                        println("Received reply, thread ${Thread.currentThread().name}: " + ar.result().body());
                    }
                }
    }

}
@ApplicationScoped
class WorkerVerticle : AbstractVerticle(){
    var address: String = ""
    override fun start() {
        println(Thread.currentThread().name + ", Start Worker...")

        // '調用 bus'
        val bus = vertx.eventBus()

        // 取得消息
        val consumer = bus.consumer<Map<String, Int>>(this.address)
        consumer.handler{message ->
            println("thread ${Thread.currentThread().name}:I am the worker, and get the message from Acceptor: ${message.body()}")
            val map = message.body()
            val num = map["num"]?:0
            val times = map["times"]?:0
            val result = num * times

            Thread.sleep(10000)

            // 完成，結果要回傳給 bus
            message.reply(result)
        }
    }
}

@ApplicationScoped
class WorkerVerticleWithFood: AbstractVerticle(){
    var address: String = ""
    override fun start() {
        println(Thread.currentThread().name + ", Start Worker...")

        // '調用 bus'
        val bus = vertx.eventBus()


        // 取得消息
        val consumer = bus.consumer<Food>(this.address)
        consumer.handler{message ->
            println("thread ${Thread.currentThread().name}:I am the worker, and get the message from Acceptor: ${message.body()}")
            val food = message.body()
            val name = food.name
            val price = food.price
            val sale = food.sale

            Thread.sleep(10000)

            // 完成，結果要回傳給 bus
            message.reply("Acceptor give me the $name , and its price is $price. Sale: $sale")
        }
    }
}

@ApplicationScoped
class WorkerVerticleWithFoodList: AbstractVerticle(){
    var address: String = ""
    override fun start() {
        println(Thread.currentThread().name + ", Start Worker...")

        // '調用 bus'
        val bus = vertx.eventBus()


        // 取得消息
        val consumer = bus.consumer<List<Food>>(this.address)
        consumer.handler{message ->
            println("thread ${Thread.currentThread().name}:I am the worker, and get the message from Acceptor: ${message.body()}")
            val food = message.body()
            val name = food[0].name
            val price = food[0].price
            val sale = food[0].sale

            Thread.sleep(10000)

            // 完成，結果要回傳給 bus
            message.reply("Acceptor give me the (First!!) $name , and its price is $price. Sale: $sale")
        }
    }
}