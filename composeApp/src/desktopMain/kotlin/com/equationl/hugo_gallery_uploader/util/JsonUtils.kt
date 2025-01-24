package com.equationl.hugo_gallery_uploader.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.File

object JsonUtils {
    val gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(File::class.java, FileTypeAdapter())
            .create()
    }

    fun Any.toJson(): String {
        return gson.toJson(this)
    }

    inline fun <reified T> String.fromJson(): T? {
        return try {
            gson.fromJson(this, T::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun <T> String.fromJsonList(cls: Class<T>?): ArrayList<T> {
        val mList = ArrayList<T>()

        if (this.isBlank()) {
            return mList
        }

        val array = JsonParser.parseString(this).asJsonArray
        if (array != null && array.size() > 0) {
            for (elem in array) {
                mList.add(gson.fromJson(elem, cls))
            }
        }
        return mList
    }

    class FileTypeAdapter : TypeAdapter<File>() {

        override fun write(out: JsonWriter, value: File) {
            out.value(value.absolutePath)
        }

        override fun read(`in`: JsonReader): File {
            val path = `in`.nextString()
            return File(path)
        }
    }
}

