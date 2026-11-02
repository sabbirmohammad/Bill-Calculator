package com.bangla.electricitybill.data.backup

import com.bangla.electricitybill.core.util.DateUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.math.BigDecimal
import java.time.LocalDateTime

class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {
    override fun write(out: JsonWriter, value: LocalDateTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(DateUtils.toEpochMilli(value))
        }
    }

    override fun read(reader: JsonReader): LocalDateTime? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        val epochMilli = reader.nextLong()
        return DateUtils.fromEpochMilli(epochMilli)
    }
}

class BigDecimalAdapter : TypeAdapter<BigDecimal>() {
    override fun write(out: JsonWriter, value: BigDecimal?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.toPlainString())
        }
    }

    override fun read(reader: JsonReader): BigDecimal? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        val str = reader.nextString()
        return BigDecimal(str)
    }
}

object BackupGson {
    fun create(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
            .registerTypeAdapter(BigDecimal::class.java, BigDecimalAdapter())
            .setPrettyPrinting()
            .create()
    }
}
