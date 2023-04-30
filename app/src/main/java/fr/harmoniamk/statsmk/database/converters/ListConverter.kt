package fr.harmoniamk.statsmk.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import fr.harmoniamk.statsmk.extension.fromJson

class ListConverter {

    @TypeConverter
    fun fromList(value: List<String>?) = Gson().toJson(value)


    @TypeConverter
    fun toList(value: String?): List<String>? = try {
        value?.let { Gson().fromJson<List<String>>(it) }
    } catch (e: Exception) {
        arrayListOf()
    }
}