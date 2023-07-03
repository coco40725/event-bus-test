package com.blocking.ctrl

import com.blocking.svc.BlockingSvc
import io.quarkus.mongodb.runtime.dns.MongoDnsClientProvider.vertx
import io.smallrye.common.annotation.NonBlocking
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.subscription.UniEmitter
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
        private val  blockingSvc: BlockingSvc
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
}