package fr.harmoniamk.statsmk.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import fr.harmoniamk.statsmk.extension.fromJson
import fr.harmoniamk.statsmk.model.firebase.NewWarPositions

class WarPositionConverter {


    @TypeConverter
    fun fromWarPositionList(value: List<NewWarPositions>?): String =  Gson().toJson(value)


    @TypeConverter
    fun toWarPositionList(value: String?): List<NewWarPositions>? = try {
            value?.let { Gson().fromJson<List<NewWarPositions>>(it) }
        } catch (e: Exception) {
            arrayListOf()
        }

}