package com.blocking.entity

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import io.vertx.core.json.JsonObject

/**
@author Yu-Jing
@create 2023-07-07-下午 10:09
 */
class FoodMessageCodec: MessageCodec<Food, Food> {

    // encode: 將自定義的 Object 轉成 message 可以接受的格式，最簡單的方式
    // 1. 將自定義 object 手動轉成 JsonObject
    // 2. 將 JsonObject encode 成 string
    // 3. 將 string 放進 buffer 進行傳輸
    override fun encodeToWire(buffer: Buffer?, food: Food) {
        val jsonToEncode = JsonObject()
        jsonToEncode.put("name", food.name)
        jsonToEncode.put("price", food.price)
        jsonToEncode.put("sale", food.sale)

        // Encode object to string
        val jsonToStr: String = jsonToEncode.encode()

        // Length of JSON: is NOT characters count
        val length = jsonToStr.toByteArray().size

        // Write data into given buffer
        buffer!!.appendInt(length)
        buffer.appendString(jsonToStr)
    }

    // decode: 從 buffer
    override fun decodeFromWire(pos: Int, buffer: Buffer?): Food {
        // My custom message starting from this *position* of buffer
        var _pos: Int = pos

        // Length of JSON
        val length = buffer!!.getInt(_pos)


        // Get JSON string by it`s length
        // Jump 4 because getInt() == 4 bytes
        val jsonStr = buffer.getString(4.let { _pos += it; _pos }, length.let { _pos += it; _pos })
        val contentJson = JsonObject(jsonStr)

        // Get fields
        val name = contentJson.getString("name")
        val price = contentJson.getDouble("price")
        val sale = contentJson.getBoolean("sale")


        // We can finally create custom message object
        return Food(name, price, sale)
    }


    override fun name(): String {
        // Each codec must have a unique name.
        // This is used to identify a codec when sending a message and for unregistering codecs.
        println("call name")
        println(this.javaClass.simpleName)
        return this.javaClass.simpleName
    }

    override fun systemCodecID(): Byte {
        // Always -1
        return -1;
    }

    override fun transform(food: Food?): Food? {
        // If a message is sent *locally* across the event bus.
        // This example sends message just as is
        return food
    }
}