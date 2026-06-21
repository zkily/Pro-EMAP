package com.example.smart_emap.data.model

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

/** mes_defect_by_item：数量のみ、または { qty, at } オブジェクトの両方を受け付ける */
@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class MesDefectByItem

class MesDefectByItemMapAdapter : JsonAdapter<Map<String, Int>?>() {
    override fun fromJson(reader: JsonReader): Map<String, Int>? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> {
                reader.nextNull<Any>()
                null
            }
            JsonReader.Token.BEGIN_OBJECT -> readObject(reader)
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    private fun readObject(reader: JsonReader): Map<String, Int> {
        val out = linkedMapOf<String, Int>()
        reader.beginObject()
        while (reader.hasNext()) {
            val key = reader.nextName()
            when (reader.peek()) {
                JsonReader.Token.NULL -> reader.nextNull<Any>()
                JsonReader.Token.NUMBER -> {
                    val qty = reader.nextInt().coerceAtLeast(0)
                    if (qty > 0) out[key] = qty
                }
                JsonReader.Token.BEGIN_OBJECT -> {
                    reader.beginObject()
                    var qty = 0
                    while (reader.hasNext()) {
                        when (reader.nextName()) {
                            "qty", "quantity", "count" -> {
                                qty = when (reader.peek()) {
                                    JsonReader.Token.NULL -> {
                                        reader.nextNull<Any>()
                                        0
                                    }
                                    JsonReader.Token.NUMBER -> reader.nextInt().coerceAtLeast(0)
                                    else -> {
                                        reader.skipValue()
                                        0
                                    }
                                }
                            }
                            else -> reader.skipValue()
                        }
                    }
                    reader.endObject()
                    if (qty > 0) out[key] = qty
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return out
    }

    override fun toJson(writer: JsonWriter, value: Map<String, Int>?) {
        if (value == null) {
            writer.nullValue()
            return
        }
        writer.beginObject()
        for ((k, v) in value) {
            if (v > 0) {
                writer.name(k)
                writer.value(v)
            }
        }
        writer.endObject()
    }
}

object MesDefectByItemAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (annotations.none { it is MesDefectByItem }) return null
        if (Types.getRawType(type) != Map::class.java) return null
        return MesDefectByItemMapAdapter()
    }
}
