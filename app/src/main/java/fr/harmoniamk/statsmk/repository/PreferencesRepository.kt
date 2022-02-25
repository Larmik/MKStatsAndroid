package fr.harmoniamk.statsmk.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.harmoniamk.statsmk.database.model.Team
import fr.harmoniamk.statsmk.database.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
interface PreferencesRepositoryInterface {
    var hasCurrentTournament: Boolean
    var currentUser: User?
    var currentTeam: Team?
}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(ApplicationComponent::class)
interface PreferencesRepositoryModule {
    @Binds
    fun bindRepository(impl: PreferencesRepository): PreferencesRepositoryInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class PreferencesRepository @Inject constructor(
    @ApplicationContext var context: Context
) : PreferencesRepositoryInterface {

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override var hasCurrentTournament: Boolean
        get() = preferences.getBoolean("hasCurrentTournament", false)
        set(value) = preferences.edit().putBoolean("hasCurrentTournament", value).apply()
    override var currentUser: User?
        get() = Gson().fromJson(preferences.getString("currentUser", null), User::class.java)
        set(value) = preferences.edit().putString("currentUser", Gson().toJson(value)).apply()
    override var currentTeam: Team?
        get() = Gson().fromJson(preferences.getString("currentTeam", null), Team::class.java)
        set(value) = preferences.edit().putString("currentTeam", Gson().toJson(value)).apply()
}