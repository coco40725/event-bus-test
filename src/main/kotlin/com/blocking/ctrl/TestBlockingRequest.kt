package com.blocking.ctrl

import com.blocking.entity.*
import com.blocking.svc.*
import io.quarkus.mongodb.runtime.dns.MongoDnsClientProvider.vertx
import io.smallrye.common.annotation.NonBlocking
import io.smallrye.mutiny.Uni
import io.vertx.core.DeploymentOptions
import io.vertx.mutiny.core.eventbus.EventBus
import jakarta.annotation.PostConstruct
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

/**
@author Yu-Jing
@create 2023-07-03-5:19 PM
 */
@Path("/block")
class TestBlockingRequest @Inject constructor(
        private val  blockingSvc: BlockingSvc,
        private val acceptorVerticle: AcceptorVerticle,
        private val workerVerticle: WorkerVerticle,
        private val workerVerticleWithFood: WorkerVerticleWithFood,
        private val workerVerticleWithFoodList: WorkerVerticleWithFoodList,
        private val bus: EventBus
){

    @PostConstruct
    fun initialize() {
        println("Initialization executed.")
        val foodCodec = FoodMessageCodec()
        vertx.eventBus().unregisterCodec(foodCodec.name())
        vertx.eventBus().registerDefaultCodec(Food::class.java, foodCodec)
    }

    @GET
    @Path("/default")
    @NonBlocking
    @Produces(MediaType.TEXT_PLAIN)
    fun blockRequestTest1(): Uni<Int>{
        println("Request accepted: thread ${Thread.currentThread().name}")
        var num = 12
        num = blockingSvc.blockingFun5s(num)
        return Uni.createFrom().item(num)
    }


    @GET
    @Path("/execute5")
    @NonBlocking
    @Produces(MediaType.TEXT_PLAIN)
    fun blockRequestTest2(): Uni<Int>{
        println("Request accepted: thread ${Thread.currentThread().name}")
        val num = 12
        return Uni.createFrom().emitter { em ->
            vertx.executeBlocking<Int> { promise ->
                val result = blockingSvc.blockingFun5s(num) // sleep 5s, 再對 num 乘以 2 並輸出
                promise.complete(result)
            }.onComplete { res ->
                if (res.succeeded()) {
                    println("The result is: " + res.result())
                    em.complete(res.result())
                } else {
                    em.fail(res.cause())
                }
            }
        }
    }


    @GET
    @Path("/execute30")
    @NonBlocking
    @Produces(MediaType.TEXT_PLAIN)
    fun blockRequestTest3(): Uni<Int>{
        val num = 12
        return Uni.createFrom().emitter { em ->
            vertx.executeBlocking<Int> { promise ->
                val result = blockingSvc.blockingFun30s(num) // sleep 30s, 再對 num 乘以 2 並輸出
                promise.complete(result)
            }.onComplete { res ->
                if (res.succeeded()) {
                    println("The result is: " + res.result())
                    em.complete(res.result())
                } else {
                    em.fail(res.cause())
                }
            }
        }
    }

    @GET
    @Path("/eventbus1")
    @NonBlocking
    @Produces(MediaType.TEXT_PLAIN)
    fun blockRequestTest4(){
        println("Request accepted: thread ${Thread.currentThread().name}")
        workerVerticle.address = "my/first/address"
        vertx.deployVerticle(workerVerticle, DeploymentOptions().setWorker(true))

        vertx.deployVerticle(acceptorVerticle)
                .onComplete{res ->
                    if (res.succeeded()) {
                        println("Deployment id is: " + res.result());

                    } else {
                        println("Deployment failed!");
                    }
                }
    }

    @GET
    @Path("/eventbus2")
    @NonBlocking
    @Produces(MediaType.TEXT_PLAIN)
    fun blockRequestTest5(): Uni<Int>{
        println("Request accepted: thread ${Thread.currentThread().name}")
        val map = mapOf("num" to 2, "times" to 4)
        val bus = vertx.eventBus()
        workerVerticle.address = "sendfrom/controller"
        vertx.deployVerticle(workerVerticle, DeploymentOptions().setWorker(true))

        return Uni.createFrom().emitter{ em ->
            bus.request<Int>("sendfrom/controller", map)
                    .onComplete{res ->
                        if (res.succeeded()) {
                            println("Deployment id is: " + res.result());
                            println("thread name ${Thread.currentThread().name} result: ${res.result().body()}")
                            em.complete(res.result().body())
                        } else {
                            println("fail ${res.cause()}");
                        }
                    }
        }
    }

    @GET
    @Path("/eventbus3")
    @NonBlocking
    @Produces(MediaType.TEXT_PLAIN)
    fun blockRequestTest6(): Uni<Int> {
        return bus.request<Int>("hello", mapOf("num" to 2, "times" to 14))
                .onItem().transform{ it.body()}
    }

    @GET
    @Path("/eventbus4")
    @NonBlocking
    @Produces(MediaType.TEXT_PLAIN)
    fun blockRequestTest7(): Uni<Int>{
        println("request: ${Thread.currentThread().name}")
        return bus.request<Int>("hello1", mapOf("num" to 2, "times" to 22))
            .onItem().transform{ it.body()}
    }

    @GET
    @Path("/eventbus4_1")
    @NonBlocking
    @Produces(MediaType.TEXT_PLAIN)
    fun blockRequestTest7_1(): Uni<Int>{
        println("request: ${Thread.currentThread().name}")
        return bus.request<Int>("hello2", listOf(1,3))
            .onItem().transform{ it.body()}
    }


    /**
     * Person 可以作為 consume 的message 送出
     */
    @GET
    @Path("/eventbus5")
    @NonBlocking
    @Produces(MediaType.TEXT_PLAIN)
    fun blockRequestTest8(): Uni<Person>{
        println("request: ${Thread.currentThread().name}")
        return bus.request<Person>("person", 12)
            .onItem().transform{ it.body()}
    }


    /**
     * List<Person>  “不”可以作為 consume 的message 送出，會出現 (RECIPIENT_FAILURE,8185) java.lang.RuntimeException: java.io.NotSerializableException: com.blocking.entity.Person
     */
    @GET
    @Path("/eventbus6")
    @NonBlocking
    @Produces(MediaType.TEXT_PLAIN)
    fun blockRequestTest9(): Uni<List<Person>>{
        println("request: ${Thread.currentThread().name}")
        return bus.request<List<Person>>("persons", 30)
            .onItem().transform{ it.body()}
    }


    /**
     * 我們將  List<Person> 做為 PersonList 的一個屬性，然後就能作為 message 傳過來了，
     * 最後在這邊轉換時再調整回 List<Person>
     */
    @GET
    @Path("/eventbus7")
    @NonBlocking
    @Produces(MediaType.TEXT_PLAIN)
    fun blockRequestTest10(): Uni<List<Person>>{
        println("request: ${Thread.currentThread().name}")
        return bus.request<PersonList>("personsInnerList", 20)
            .onItem().transform{ it.body().personList}
    }


    @GET
    @Path("/eventbus8")
    @NonBlocking
    @Produces(MediaType.APPLICATION_JSON)
    fun blockRequestTest11(): Uni<String> {
        println("request: ${Thread.currentThread().name}")
        val food = Food("orange", 333.0, true)
        val bus = vertx.eventBus()


        workerVerticleWithFood.address = "my/food"
        vertx.deployVerticle(workerVerticleWithFood, DeploymentOptions().setWorker(true))

        return Uni.createFrom().emitter{ em ->
            bus.request<String>("my/food", food)
                .onComplete{res ->
                    if (res.succeeded()) {
                        println("Deployment id is: " + res.result());
                        println("thread name ${Thread.currentThread().name} result: ${res.result().body()}")
                        em.complete(res.result().body())
                    } else {
                        println("fail ${res.cause()}");
                    }
                }
        }
    }


    @GET
    @Path("/eventbus9")
    @NonBlocking
    @Produces(MediaType.APPLICATION_JSON)
    fun blockRequestTest12(): Uni<String> {
        println("request: ${Thread.currentThread().name}")
        val food = Food("orange", 333.0, true)
        val food1 = Food("apple", 111.0, true)

        val bus = vertx.eventBus()


        workerVerticleWithFoodList.address = "my/foodList"
        vertx.deployVerticle(workerVerticleWithFoodList, DeploymentOptions().setWorker(true))

        return Uni.createFrom().emitter{ em ->
            bus.request<String>("my/foodList", listOf(food1, food))
                .onComplete{res ->
                    if (res.succeeded()) {
                        println("Deployment id is: " + res.result());
                        println("thread name ${Thread.currentThread().name} result: ${res.result().body()}")
                        em.complete(res.result().body())
                    } else {
                        println("fail ${res.cause()}");
                    }
                }
        }
    }

}