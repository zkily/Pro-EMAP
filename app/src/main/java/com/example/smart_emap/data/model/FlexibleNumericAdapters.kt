package com.example.smart_emap.data.model

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

/** API が数値フィールドを文字列で返す場合にも解析できる Int? アダプタ */
private class FlexibleNullableIntJsonAdapter : JsonAdapter<Int?>() {
    override fun fromJson(reader: JsonReader): Int? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> {
                reader.nextNull<Any>()
                null
            }
            JsonReader.Token.NUMBER -> reader.nextInt()
            JsonReader.Token.STRING -> parseIntString(reader.nextString())
            JsonReader.Token.BOOLEAN -> if (reader.nextBoolean()) 1 else 0
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    override fun toJson(writer: JsonWriter, value: Int?) {
        if (value == null) writer.nullValue() else writer.value(value)
    }

    private fun parseIntString(raw: String): Int? {
        val s = raw.trim()
        if (s.isEmpty()) return null
        s.toIntOrNull()?.let { return it }
        s.toDoubleOrNull()?.let { return it.toInt() }
        return null
    }
}

object FlexibleIntAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (Types.getRawType(type) != Int::class.javaObjectType) return null
        return FlexibleNullableIntJsonAdapter()
    }
}
