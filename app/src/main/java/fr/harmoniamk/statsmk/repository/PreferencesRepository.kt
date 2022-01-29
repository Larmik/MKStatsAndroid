package fr.harmoniamk.statsmk.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
interface PreferencesRepositoryInterface {
    var hasCurrentTournament: Boolean

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
}