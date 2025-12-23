package com.navibharat.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromJsonStringMap(value: String?): Map<String, Double>? {
        if (value == null) return null
        val type = object : TypeToken<Map<String, Double>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun toJsonStringMap(value: Map<String, Double>?): String? {
        return if (value == null) null else gson.toJson(value)
    }

    @TypeConverter
    fun fromJsonList(value: String?): List<String>? {
        if (value == null) return null
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun toJsonList(value: List<String>?): String? {
        return if (value == null) null else gson.toJson(value)
    }
}
