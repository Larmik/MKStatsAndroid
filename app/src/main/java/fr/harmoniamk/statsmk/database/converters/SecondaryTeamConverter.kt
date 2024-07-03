package fr.harmoniamk.statsmk.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import fr.harmoniamk.statsmk.extension.fromJson
import fr.harmoniamk.statsmk.model.network.SecondaryTeam

class SecondaryTeamConverter {
    @TypeConverter
    fun fromSecondaryTeamList(value: List<SecondaryTeam>?): String = Gson().toJson(value)

    @TypeConverter
    fun toSecondaryTeamList(value: String?) =
        try {
            value?.let { Gson().fromJson<List<SecondaryTeam>>(it) }
        } catch (e: Exception) {
            arrayListOf()
        }
}