package fr.harmoniamk.statsmk.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import fr.harmoniamk.statsmk.extension.fromJson
import fr.harmoniamk.statsmk.model.firebase.Penalty

class PenaltyConverter {

    @TypeConverter
    fun fromPenaltyList(value: List<Penalty>?) = Gson().toJson(value)


    @TypeConverter
    fun toPenaltyList(value: String?): List<Penalty>? = try {
            value?.let { Gson().fromJson<List<Penalty>>(it) }
        } catch (e: Exception) {
            arrayListOf()
        }

}