package fr.harmoniamk.statsmk.repository

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings.Secure
import android.util.Log
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
import fr.harmoniamk.statsmk.extension.parsePenalties
import fr.harmoniamk.statsmk.extension.parseTracks
import fr.harmoniamk.statsmk.extension.toMapList
import fr.harmoniamk.statsmk.model.firebase.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import javax.inject.Inject

interface FirebaseRepositoryInterface{
    val deviceId: String?

    //Write and edit methods
    fun writeUser(user: User): Flow<Unit>
    fun writeNewWar(war: NewWar): Flow<Unit>
    fun writeTeam(team: Team): Flow<Unit>

    //Get lists methods
    fun getUsers(): Flow<List<User>>
    fun getTeams(): Flow<List<Team>>
    fun getNewWars(): Flow<List<NewWar>>

    //Get objects methods
    fun getNewWar(id: String): Flow<NewWar?>
    fun getPositions(warId: String, trackId: String): Flow<List<NewWarPositions>>

    //Firebase event listeners methods
    fun listenToNewWars(): Flow<List<NewWar>>
    fun listenToUsers(): Flow<List<User>>

    //delete methods
    fun deleteUser(user: User): Flow<Unit>
    fun deleteTeam(team: Team): Flow<Unit>
    fun deleteNewWar(warId: String): Flow<Unit>

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
class FirebaseRepository @Inject constructor(@ApplicationContext private val context: Context, private val databaseRepository: DatabaseRepositoryInterface) : FirebaseRepositoryInterface {

    @SuppressLint("HardwareIds")
    override val deviceId = Secure.getString(context.contentResolver, Secure.ANDROID_ID)
    private val database  = Firebase.database.reference

    override fun writeUser(user: User) = flow {
        database.child("users").child(user.mid).setValue(user)
        emit(Unit)
    }.flatMapLatest { databaseRepository.writeUser(user) }

    override fun writeNewWar(war: NewWar): Flow<Unit> = flow {
        database.child("newWars").child(war.mid.toString()).setValue(war)
        emit(Unit)
    }

    override fun writeTeam(team: Team): Flow<Unit> = flow {
        database.child("teams").child(team.mid).setValue(team)
        emit(Unit)
    }.flatMapLatest { databaseRepository.writeTeam(team) }

    override fun getUsers(): Flow<List<User>> = callbackFlow {
        Log.d("FirebaseRepository", "getUsers")
        database.child("users").get().addOnSuccessListener { snapshot ->
            val users: List<User> = snapshot.children
                .map { it.value as Map<*, *> }
                .map { User(
                    mid = it["mid"].toString(),
                    name = it["name"].toString(),
                    currentWar = it["currentWar"].toString(),
                    team = it["team"].toString(),
                    role = it["role"].toString().toIntOrNull(),
                    picture = it["picture"].toString()
                ) }
            if (isActive) offer(users)
        }
        awaitClose {  }
    }

    override fun getTeams(): Flow<List<Team>> = callbackFlow {
        Log.d("FirebaseRepository", "getTeams")
        database.child("teams").get().addOnSuccessListener { snapshot ->
            val teams: List<Team> = snapshot.children
                .map { it.value as Map<*, *> }
                .map { Team(
                    mid = it["mid"].toString(),
                    name = it["name"].toString(),
                    shortName = it["shortName"].toString(),
                    hasLeader = it["hasLeader"].toString().toBoolean()
                ) }
            if (isActive) offer(teams)
        }
        awaitClose {  }
    }

    override fun getNewWars(): Flow<List<NewWar>> = callbackFlow {
        Log.d("FirebaseRepository", "getNewWars")
        database.child("newWars").get().addOnSuccessListener { snapshot ->
            val wars: List<NewWar> = snapshot.children
                .map { it.value as Map<*, *> }
                .map {map -> NewWar(
                    mid = map["mid"].toString(),
                    playerHostId = map["playerHostId"].toString(),
                    teamHost = map["teamHost"].toString(),
                    teamOpponent = map["teamOpponent"].toString(),
                    createdDate = map["createdDate"].toString(),
                    warTracks = map["warTracks"].toMapList().parseTracks(),
                    penalties = map["penalties"].toMapList().parsePenalties(),
                    isOfficial = map["official"].toString().toBoolean()
                )
                }
            if (isActive) offer(wars)
        }
        awaitClose {  }
    }



    override fun getNewWar(id: String): Flow<NewWar?> = callbackFlow {
        Log.d("FirebaseRepository", "getNewWar")
        database.child("newWars").child(id).get().addOnSuccessListener { snapshot ->
            val map = (snapshot.value as? Map<*,*>)
            if (isActive) offer(
                if (map == null) null
                else NewWar(
                    mid = map["mid"].toString(),
                    playerHostId = map["playerHostId"].toString(),
                    teamOpponent = map["teamOpponent"].toString(),
                    teamHost = map["teamHost"].toString(),
                    createdDate = map["createdDate"].toString(),
                    warTracks = map["warTracks"].toMapList().parseTracks(),
                    penalties = map["penalties"].toMapList().parsePenalties(),
                    isOfficial = map["official"].toString().toBoolean()
                )
            )
        }
        awaitClose {  }
    }

    override fun getPositions(warId: String, trackId: String): Flow<List<NewWarPositions>> = callbackFlow {
        Log.d("FirebaseRepository", "getPositions")
        database.child("newWars").child(warId).child("warTracks").get().addOnSuccessListener { snapshot ->
            val tracks: List<NewWarTrack> = snapshot.children
                .map { it.value as Map<*, *> }
                .map {map -> NewWarTrack(
                    mid = map["mid"].toString(),
                    trackIndex = map["trackIndex"].toString().toInt(),
                    warPositions = GsonBuilder().serializeNulls().create().fromJson(map["warPositions"].toString(), object: TypeToken<List<NewWarPositions>>(){}.type ),
                    shocks = GsonBuilder().serializeNulls().create().fromJson(map["shocks"].toString(), object: TypeToken<List<Shock>>(){}.type ))
                }
            tracks.singleOrNull { it.mid == trackId }?.warPositions?.let {
                if (isActive) offer(it)
            }

        }
        awaitClose {  }
    }

    override fun listenToNewWars(): Flow<List<NewWar>> = callbackFlow {
        Log.d("FirebaseRepository", "listenNewWars")
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val wars: List<NewWar> = dataSnapshot.child("newWars").children.map { it.value as Map<*, *> }.map {
                    NewWar(
                    mid = it["mid"].toString(),
                    playerHostId = it["playerHostId"].toString(),
                    teamOpponent = it["teamOpponent"].toString(),
                    teamHost = it["teamHost"].toString(),
                    createdDate = it["createdDate"].toString(),
                    warTracks = it["warTracks"].toMapList().parseTracks(),
                        penalties = it["penalties"].toMapList().parsePenalties(),
                        isOfficial = it["official"].toString().toBoolean()
                    )
                  }
                if (isActive) offer(wars)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        database.addValueEventListener(postListener)
        awaitClose {  }
    }

    override fun listenToUsers(): Flow<List<User>> = callbackFlow {
        Log.d("FirebaseRepository", "listenUsers")
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val users: List<User> = dataSnapshot.child("users").children.map { it.value as Map<*, *> }.map {
                    User(
                        mid = it["mid"].toString(),
                        name = it["name"].toString(),
                        team = it["team"].toString(),
                        currentWar = it["currentWar"].toString(),
                        role = it["role"].toString().toInt(),
                        picture = it["picture"].toString()
                    )  }
                if (isActive) offer(users)
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
    }.flatMapLatest { databaseRepository.deleteUser(user) }

    override fun deleteTeam(team: Team)= flow {
        database.child("teams").child(team.mid.toString()).removeValue()
        emit(Unit)
    }.flatMapLatest { databaseRepository.deleteTeam(team) }

    override fun deleteNewWar(warId: String) = flow {
        database.child("newWars").child(warId).removeValue()
        emit(Unit)
    }
}