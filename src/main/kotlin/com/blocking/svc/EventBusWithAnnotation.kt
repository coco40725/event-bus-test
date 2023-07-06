package com.blocking.svc

import com.blocking.entity.Person
import com.blocking.entity.PersonList
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


    @ConsumeEvent("person")
    @Blocking
    fun consumePOJO(age: Int): Person{
        println(Thread.currentThread().name + ", consume event")
        val person = Person()
        person.name = "person test"
        person.age = age
        person.money = age * 100.0

        Thread.sleep(10000)
        return person
    }


    @ConsumeEvent("persons")
    @Blocking
    fun consumePOJOList(age: Int): List<Person>{
        println(Thread.currentThread().name + ", consume event")
        val person1 = Person()
        val person2 = Person()
        person1.name = "person1 list "
        person1.age = age
        person1.money = age * 100.0

        person2.name = "person2 list"
        person2.age = age * 2
        person2.money = age * 2 * 100.0

        Thread.sleep(10000)
        return listOf(person1, person2)
    }

    @ConsumeEvent("personsInnerList")
    @Blocking
    fun consumePOJOListSimply(age: Int): PersonList{
        println(Thread.currentThread().name + ", consume event")
        val person1 = Person()
        val person2 = Person()
        person1.name = "person1 list "
        person1.age = age
        person1.money = age * 100.0

        person2.name = "person2 list"
        person2.age = age * 2
        person2.money = age * 2 * 100.0

        val personList = PersonList()
        personList.personList = listOf(person1, person2)

        Thread.sleep(10000)
        return personList
    }


}