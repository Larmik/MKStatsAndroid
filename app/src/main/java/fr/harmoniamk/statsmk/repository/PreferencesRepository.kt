package fr.harmoniamk.statsmk.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.network.MKCFullPlayer
import fr.harmoniamk.statsmk.model.network.MKCFullTeam
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
interface PreferencesRepositoryInterface {
    var currentWar: NewWar?
    var currentWarTrack: NewWarTrack?
    var currentTheme: Int
    var authEmail: String?
    var authPassword: String?
    var firstLaunch: Boolean
    var indivEnabled: Boolean
    var rosterOnly: Boolean
    var fcmToken: String?
    var mkcPlayer: MKCFullPlayer?
    var mkcTeam: MKCFullTeam?
    var role: Int
    var lastUpdate: String
    var mainColor: String
    var secondaryColor: String
    var mainTextColor: String
    var secondaryTextColor: String
    var isPendingPurchase: Boolean
    var coffees: Int
}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
interface PreferencesRepositoryModule {
    @Singleton
    @Binds
    fun bindRepository(impl: PreferencesRepository): PreferencesRepositoryInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class PreferencesRepository @Inject constructor(
    @ApplicationContext var context: Context
) : PreferencesRepositoryInterface {

    private val preferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        context.packageName + "_preferences",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override var currentWar: NewWar?
        get() = Gson().fromJson(preferences.getString("currentWar", null), NewWar::class.java)
        set(value) = preferences.edit().putString("currentWar", Gson().toJson(value)).apply()
    override var currentWarTrack: NewWarTrack?
        get() = Gson().fromJson(preferences.getString("currentWarTrack", null), NewWarTrack::class.java)
        set(value) = preferences.edit().putString("currentWarTrack", Gson().toJson(value)).apply()
    override var currentTheme: Int
        get() = preferences.getInt("currentTheme", R.style.AppThemeWaluigi)
        set(value) { preferences.edit().putInt("currentTheme", value).apply() }
    override var authEmail: String?
        get() = preferences.getString("authEmail", null)
        set(value) {preferences.edit().putString("authEmail", value).apply()}
    override var authPassword: String?
        get() = preferences.getString("authPassword", null)
        set(value) {preferences.edit().putString("authPassword", value).apply()}
    override var firstLaunch: Boolean
        get() = preferences.getBoolean("firstLaunch", true)
        set(value) = preferences.edit().putBoolean("firstLaunch", value).apply()
    override var indivEnabled: Boolean
        get() = preferences.getBoolean("indivEnabled", true)
        set(value) = preferences.edit().putBoolean("indivEnabled", value).apply()
    override var rosterOnly: Boolean
        get() = preferences.getBoolean("rosterOnly", false)
        set(value) = preferences.edit().putBoolean("rosterOnly", value).apply()
    override var fcmToken: String?
        get() = preferences.getString("fcmToken", null)
        set(value) {preferences.edit().putString("fcmToken", value).apply()}
    override var mkcPlayer: MKCFullPlayer?
        get() = Gson().fromJson(preferences.getString("mkcPlayer", null), MKCFullPlayer::class.java)
        set(value) = preferences.edit().putString("mkcPlayer", Gson().toJson(value)).apply()
    override var mkcTeam: MKCFullTeam?
        get() = Gson().fromJson(preferences.getString("mkcTeam", null), MKCFullTeam::class.java)
        set(value) = preferences.edit().putString("mkcTeam", Gson().toJson(value)).apply()
    override var role: Int
        get() = preferences.getInt("role", 0)
        set(value) {preferences.edit().putInt("role", value).apply()}
    override var lastUpdate: String
        get() = preferences.getString("lastUpdate", "").orEmpty()
        set(value) {preferences.edit().putString("lastUpdate", value).apply()}
    override var mainColor: String
        get() = preferences.getString("mainColor", "B0E0E6") ?: "B0E0E6"
        set(value) {preferences.edit().putString("mainColor", value).apply()}
    override var secondaryColor: String
        get() = preferences.getString("secondaryColor", "051C3F") ?: "051C3F"
        set(value) {preferences.edit().putString("secondaryColor", value).apply()}
    override var mainTextColor: String
        get() = preferences.getString("mainTextColor", "000000") ?: "000000"
        set(value) {preferences.edit().putString("mainTextColor", value).apply()}
    override var secondaryTextColor: String
        get() = preferences.getString("secondaryTextColor", "FFFFFF") ?: "FFFFFF"
        set(value) {preferences.edit().putString("secondaryTextColor", value).apply()}
    override var isPendingPurchase: Boolean
        get() = preferences.getBoolean("isPendingPurchase", false)
        set(value) = preferences.edit().putBoolean("isPendingPurchase", value).apply()
    override var coffees: Int
        get() = preferences.getInt("coffees", 0)
        set(value) {preferences.edit().putInt("coffees", value).apply()}
}