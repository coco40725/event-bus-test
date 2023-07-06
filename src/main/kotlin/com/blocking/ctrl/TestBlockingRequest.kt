package com.blocking.ctrl

import com.blocking.entity.Person
import com.blocking.entity.PersonList
import com.blocking.svc.*
import io.quarkus.mongodb.runtime.dns.MongoDnsClientProvider.vertx
import io.smallrye.common.annotation.NonBlocking
import io.smallrye.mutiny.Uni
import io.vertx.core.DeploymentOptions
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import io.vertx.mutiny.core.eventbus.EventBus
import io.vertx.mutiny.core.eventbus.Message

/**
@author Yu-Jing
@create 2023-07-03-5:19 PM
 */
@Path("/block")
class TestBlockingRequest @Inject constructor(
        private val  blockingSvc: BlockingSvc,
        private val acceptorVerticle: AcceptorVerticle,
        private val workerVerticle: WorkerVerticle,
        private val eventBusWithAnnotation: EventBusWithAnnotation,
        private val bus: EventBus
){

    @GET
    @Path("/default")
    @NonBlocking
    @Produces(MediaType.TEXT_PLAIN)
    fun blockRequestTest1(): Uni<Int>{
        var num = 12
        num = blockingSvc.blockingFun5s(num)
        return Uni.createFrom().item(num)
    }


    @GET
    @Path("/execute5")
    @NonBlocking
    @Produces(MediaType.TEXT_PLAIN)
    fun blockRequestTest2(): Uni<Int>{
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

}