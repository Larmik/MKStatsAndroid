package fr.harmoniamk.statsmk.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import fr.harmoniamk.statsmk.extension.fromJson
import fr.harmoniamk.statsmk.model.firebase.Shock

class ShockConverter {

    @TypeConverter
    fun fromShockList(value: List<Shock>?): String = Gson().toJson(value)


    @TypeConverter
    fun toShockList(value: String?): List<Shock>? = try {
            value?.let { Gson().fromJson<List<Shock>>(it) }
        } catch (e: Exception) {
            arrayListOf()
        }

}