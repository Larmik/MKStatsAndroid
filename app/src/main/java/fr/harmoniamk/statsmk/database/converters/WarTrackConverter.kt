package fr.harmoniamk.statsmk.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import fr.harmoniamk.statsmk.extension.fromJson
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack

class WarTrackConverter {

    @TypeConverter
    fun fromWarTrackList(value: List<NewWarTrack>?) = Gson().toJson(value)

    @TypeConverter
    fun toWarTrackList(value: String?) =
        try {
            value?.let { Gson().fromJson<List<NewWarTrack>>(it) }
        } catch (e: Exception) {
            arrayListOf()
        }

}