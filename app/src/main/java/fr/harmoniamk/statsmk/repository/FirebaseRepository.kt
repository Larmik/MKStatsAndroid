package fr.harmoniamk.statsmk.repository

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings.Secure
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.harmoniamk.statsmk.extension.toMapList
import fr.harmoniamk.statsmk.model.firebase.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import javax.inject.Inject

interface FirebaseRepositoryInterface{
    val deviceId: String?

    //Used only for migration
    fun getWars(): Flow<List<War>>
    fun getWarPositions(): Flow<List<WarPosition>>
    fun getWarTracks(): Flow<List<WarTrack>>

    //Write and edit methods
    fun writeUser(user: User): Flow<Unit>
    fun writeNewWar(war: NewWar): Flow<Unit>
    fun writeTeam(team: Team): Flow<Unit>

    //Get lists methods
    fun getUsers(): Flow<List<User>>
    fun getTeams(): Flow<List<Team>>
    fun getNewWars(): Flow<List<NewWar>>

    //Get objects methods
    fun getTeam(id: String): Flow<Team?>
    fun getNewWar(id: String): Flow<NewWar?>

    //Firebase event listeners methods
    fun listenToNewWars(): Flow<List<NewWar>>

    //delete methods
    fun deleteUser(user: User): Flow<Unit>
    fun deleteTeam(team: Team): Flow<Unit>
    fun deleteNewWar(war: NewWar): Flow<Unit>

}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(ApplicationComponent::class)
interface FirebaseRepositoryModule {
    @Binds
    fun bindRepository(impl: FirebaseRepository): FirebaseRepositoryInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class FirebaseRepository @Inject constructor(@ApplicationContext private val context: Context) : FirebaseRepositoryInterface {

    @SuppressLint("HardwareIds")
    override val deviceId = Secure.getString(context.contentResolver, Secure.ANDROID_ID)
    private val database  = Firebase.database.reference

    override fun writeUser(user: User) = flow {
        database.child("users").child(user.mid.toString()).setValue(user)
        emit(Unit)
    }

    override fun writeNewWar(war: NewWar): Flow<Unit> = flow {
        database.child("newWars").child(war.mid.toString()).setValue(war)
        emit(Unit)
    }

    override fun writeTeam(team: Team): Flow<Unit> = flow {
        database.child("teams").child(team.mid.toString()).setValue(team)
        emit(Unit)
    }

    override fun getUsers(): Flow<List<User>> = callbackFlow {
        database.child("users").get().addOnSuccessListener { snapshot ->
            val users: List<User> = snapshot.children
                .map { it.value as Map<*, *> }
                .map { User(
                    mid = it["mid"].toString(),
                    name = it["name"].toString(),
                    currentWar = it["currentWar"].toString(),
                    accessCode = it["accessCode"].toString().replace("-$deviceId", ""),
                    team = it["team"].toString(),
                    isAdmin = it["admin"].toString().toBoolean()
                ) }
            if (isActive) offer(users)
        }
        awaitClose {  }
    }

    override fun getTeams(): Flow<List<Team>> = callbackFlow {
        database.child("teams").get().addOnSuccessListener { snapshot ->
            val teams: List<Team> = snapshot.children
                .map { it.value as Map<*, *> }
                .map { Team(
                    mid = it["mid"].toString(),
                    name = it["name"].toString(),
                    shortName = it["shortName"].toString(),
                    accessCode = it["accessCode"].toString()
                ) }
            if (isActive) offer(teams)
        }
        awaitClose {  }
    }

    override fun getWars(): Flow<List<War>> = callbackFlow {
        database.child("wars").get().addOnSuccessListener { snapshot ->
            val wars: List<War> = snapshot.children
                .map { it.value as Map<*, *> }
                .map {map -> War(
                    mid = map["mid"].toString(),
                    playerHostId = map["playerHostId"].toString(),
                    name = map["name"].toString(),
                    teamHost = map["teamHost"].toString(),
                    scoreHost = map["scoreHost"].toString().toInt(),
                    teamOpponent = map["teamOpponent"].toString(),
                    scoreOpponent = map["scoreOpponent"].toString().toInt(),
                    trackPlayed = map["trackPlayed"].toString().toInt(),
                    updatedDate = map["updatedDate"].toString(),
                    createdDate = map["createdDate"].toString())
                }
            if (isActive) offer(wars)
        }
        awaitClose {  }
    }

    override fun getNewWars(): Flow<List<NewWar>> = callbackFlow {
        database.child("newWars").get().addOnSuccessListener { snapshot ->
            val wars: List<NewWar> = snapshot.children
                .map { it.value as Map<*, *> }
                .map {map -> NewWar(
                    mid = map["mid"].toString(),
                    playerHostId = map["playerHostId"].toString(),
                    name = map["name"].toString(),
                    teamHost = map["teamHost"].toString(),
                    teamOpponent = map["teamOpponent"].toString(),
                    createdDate = map["createdDate"].toString(),
                    warTracks = parseTracks(map["warTracks"]?.toMapList()))
                }
            if (isActive) offer(wars)
        }
        awaitClose {  }
    }

    override fun getWarPositions(): Flow<List<WarPosition>> = callbackFlow {
        database.child("warPositions").get().addOnSuccessListener { snapshot ->
            val wars: List<WarPosition> = snapshot.children
                .map { it.value as Map<*, *> }
                .map {map -> WarPosition(
                    mid = map["mid"].toString(),
                    warTrackId = map["warTrackId"].toString(),
                    playerId = map["playerId"].toString(),
                    position = map["position"].toString().toInt())
                }
            if (isActive) offer(wars)
        }
        awaitClose {  }
    }

    override fun getWarTracks(): Flow<List<WarTrack>> = callbackFlow {
        database.child("warTracks").get().addOnSuccessListener { snapshot ->
            val wars: List<WarTrack> = snapshot.children
                .map { it.value as Map<*, *> }
                .map {map -> WarTrack(
                    mid = map["mid"].toString(),
                    warId = map["warId"].toString(),
                    trackIndex = map["trackIndex"].toString().toInt(),
                    isOver = map["over"].toString().toBoolean(),
                    teamScore = map["teamScore"].toString().toInt())
                }
            if (isActive) offer(wars)
        }
        awaitClose {  }
    }

    override fun getTeam(id: String): Flow<Team?> = callbackFlow {
        database.child("teams").child(id).get().addOnSuccessListener { snapshot ->
            val map = (snapshot.value as? Map<*,*>)
                if (isActive) offer(
                    if (map == null) null
                    else Team(
                        mid = map["mid"].toString(),
                        accessCode = map["accessCode"].toString(),
                        name = map["name"].toString(),
                        shortName = map["shortName"].toString())
                )
        }
        awaitClose {  }
    }

    override fun getNewWar(id: String): Flow<NewWar?> = callbackFlow {
        database.child("newWars").child(id).get().addOnSuccessListener { snapshot ->
            val map = (snapshot.value as? Map<*,*>)
            if (isActive) offer(
                if (map == null) null
                else NewWar(
                    mid = map["mid"].toString(),
                    name = map["name"].toString(),
                    playerHostId = map["playerHostId"].toString(),
                    teamOpponent = map["teamOpponent"].toString(),
                    teamHost = map["teamHost"].toString(),
                    createdDate = map["createdDate"].toString(),
                    warTracks = parseTracks(map["warTracks"]?.toMapList())
                )
            )
        }
        awaitClose {  }
    }

    override fun listenToNewWars(): Flow<List<NewWar>> = callbackFlow {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val wars: List<NewWar> = dataSnapshot.child("newWars").children.map { it.value as Map<*, *> }.map {
                    NewWar(
                    mid = it["mid"].toString(),
                    name = it["name"].toString(),
                    playerHostId = it["playerHostId"].toString(),
                    teamOpponent = it["teamOpponent"].toString(),
                    teamHost = it["teamHost"].toString(),
                    createdDate = it["createdDate"].toString(),
                    warTracks = parseTracks(it["warTracks"]?.toMapList())
                )  }
                if (isActive) offer(wars)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        database.addValueEventListener(postListener)
        awaitClose {  }
    }

    override fun deleteUser(user: User) = flow {
        database.child("users").child(user.mid.toString()).removeValue()
        emit(Unit)
    }

    override fun deleteTeam(team: Team)= flow {
        database.child("teams").child(team.mid.toString()).removeValue()
        emit(Unit)
    }

    override fun deleteNewWar(war: NewWar) = flow {
        database.child("newWars").child(war.mid.toString()).removeValue()
        emit(Unit)
    }

    private fun parseTracks(array: List<Map<*,*>>?) =
        array?.map { track ->
                NewWarTrack(
                    mid = track["mid"].toString(),
                    trackIndex = track["trackIndex"].toString().toInt(),
                    warPositions = (track["warPositions"]?.toMapList())
                        ?.map {
                            NewWarPositions(
                                mid = it["mid"].toString(),
                                playerId = it["playerId"].toString(),
                                position = it["position"].toString().toInt()
                            )
                        }
                )
            }
}