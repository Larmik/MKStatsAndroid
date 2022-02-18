package fr.harmoniamk.statsmk.repository

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings.Secure
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.harmoniamk.statsmk.database.firebase.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import javax.inject.Inject

interface FirebaseRepositoryInterface{
    fun writeUser(user: User): Flow<Unit>
    fun writeWar(war: War): Flow<Unit>
    fun writeWarTrack(track: WarTrack): Flow<Unit>
    fun writeWarPosition(position: WarPosition): Flow<Unit>
    fun writeTeam(team: Team): Flow<Unit>

    fun getUsers(): Flow<List<User>>
    fun getTeams(): Flow<List<Team>>
    fun getWars(): Flow<List<War>>
    fun getWarPositions(): Flow<List<WarPosition>>
    fun getWarTracks(): Flow<List<WarTrack>>

    fun getTeam(id: String): Flow<Team?>
    fun getWar(id: String): Flow<War?>
    fun getWarTrack(id: String): Flow<WarTrack?>

    fun listenToUsers(): Flow<List<User>>
    fun listenToTeams(): Flow<List<Team>>
    fun listenToWars(): Flow<List<War>>
    fun listenToWarTracks(): Flow<List<WarTrack>>
    fun listenToWarPositions(): Flow<List<WarPosition>>

    fun deleteWarPosition(position: WarPosition): Flow<Unit>
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
    private val deviceId = Secure.getString(context.contentResolver, Secure.ANDROID_ID)
    private val database  = Firebase.database.reference

    override fun writeUser(user: User) = flow {
        val authUser = user.apply { this.accessCode = "${this.accessCode}-$deviceId" }
        database.child("users").child(authUser.mid.toString()).setValue(authUser)
        emit(Unit)
    }

    override fun writeWar(war: War) = flow {
        database.child("wars").child(war.mid.toString()).setValue(war)
        emit(Unit)
    }

    override fun writeWarTrack(track: WarTrack): Flow<Unit> = flow {
        database.child("warTracks").child(track.mid.toString()).setValue(track)
        emit(Unit)
    }

    override fun writeWarPosition(position: WarPosition): Flow<Unit> = flow {
        database.child("warPositions").child(position.mid.toString()).setValue(position)
        emit(Unit)
    }

    override fun writeTeam(team: Team): Flow<Unit> = flow {
        database.child("teams").child(team.mid.toString()).setValue(team)
        emit(Unit)
    }

    override fun listenToUsers(): Flow<List<User>> = callbackFlow {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val users: List<User> = dataSnapshot.child("users").children
                    .map { it.value as Map<*, *> }
                    .map { User(
                        mid = it["mid"].toString(),
                        name = it["name"].toString(),
                        team = it["team"].toString(),
                        currentWar = it["currentWar"].toString())
                    }
                if (isActive) offer(users)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        database.addValueEventListener(postListener)
        awaitClose {  }
    }

    override fun getUsers(): Flow<List<User>> = callbackFlow {
        database.child("users").get().addOnSuccessListener { snapshot ->
            val users: List<User> = snapshot.children
                .map { it.value as Map<*, *> }
                .map { User(
                    mid = it["mid"].toString(),
                    name = it["name"].toString(),
                    accessCode = it["accessCode"].toString().replace("-$deviceId", ""),
                    team = it["team"].toString()
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

    override fun getWar(id: String): Flow<War?> = callbackFlow {
        database.child("wars").child(id).get().addOnSuccessListener { snapshot ->
            val map = (snapshot.value as? Map<*,*>)
            if (isActive) offer(
                if (map == null) null
                else War(
                    mid = map["mid"].toString(),
                    name = map["name"].toString(),
                    playerHostId = map["playerHostId"].toString(),
                    teamOpponent = map["teamOpponent"].toString(),
                    scoreOpponent = map["scoreOpponent"].toString().toInt(),
                    teamHost = map["teamHost"].toString(),
                    scoreHost = map["scoreHost"].toString().toInt(),
                    trackPlayed = map["trackPlayed"].toString().toInt(),
                    createdDate = map["createdDate"].toString(),
                    updatedDate = map["updatedDate"].toString()
                ))
        }
        awaitClose {  }
    }

    override fun getWarTrack(id: String): Flow<WarTrack?> = callbackFlow {
        database.child("warTracks").child(id).get().addOnSuccessListener { snapshot ->
            val map = (snapshot.value as? Map<*,*>)
            if (isActive) offer(
                if (map == null) null
                else WarTrack(
                    mid = map["mid"].toString(),
                    warId = map["warId"].toString(),
                    trackIndex = map["trackIndex"].toString().toInt(),
                    isOver = map["over"].toString().toBoolean(),
                    teamScore = map["teamScore"].toString().toInt()
                ))
        }
        awaitClose {  }
    }


    override fun listenToTeams(): Flow<List<Team>> = callbackFlow {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val teams: List<Team> = dataSnapshot.child("teams").children.map { it.value as Map<*, *> }.map {  Team(
                    mid = it["mid"].toString(),
                    name = it["name"].toString(),
                    shortName = it["shortName"].toString(),
                    accessCode = it["accessCode"].toString()
                )  }
                if (isActive) offer(teams)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        database.addValueEventListener(postListener)
        awaitClose {  }
    }

    override fun listenToWars(): Flow<List<War>> = callbackFlow {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val wars: List<War> = dataSnapshot.child("wars").children.map { it.value as Map<*, *> }.map {  War(
                    mid = it["mid"].toString(),
                    name = it["name"].toString(),
                    playerHostId = it["playerHostId"].toString(),
                    teamOpponent = it["teamOpponent"].toString(),
                    scoreOpponent = it["scoreOpponent"].toString().toInt(),
                    teamHost = it["teamHost"].toString(),
                    scoreHost = it["scoreHost"].toString().toInt(),
                    trackPlayed = it["trackPlayed"].toString().toInt(),
                    createdDate = it["createdDate"].toString(),
                    updatedDate = it["updatedDate"].toString()

                )  }
                if (isActive) offer(wars)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        database.addValueEventListener(postListener)
        awaitClose {  }
    }

    override fun listenToWarTracks(): Flow<List<WarTrack>> = callbackFlow {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tracks: List<WarTrack> = dataSnapshot.child("warTracks").children.map { it.value as Map<*, *> }.map {  WarTrack(
                    mid = it["mid"].toString(),
                    warId = it["warId"].toString(),
                    trackIndex = it["trackIndex"].toString().toInt(),
                    isOver = it["over"].toString().toBoolean(),
                    teamScore = it["teamScore"].toString().toInt()
                )  }
                if (isActive) offer(tracks)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        database.addValueEventListener(postListener)
        awaitClose {  }
    }

    override fun listenToWarPositions(): Flow<List<WarPosition>> = callbackFlow {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val positions: List<WarPosition> = dataSnapshot.child("warPositions").children.map { it.value as Map<*, *> }.map {  WarPosition(
                    mid = it["mid"].toString(),
                    warTrackId = it["warTrackId"].toString(),
                    playerId = it["playerId"].toString(),
                    position = it["position"].toString().toInt()

                )  }
                if (isActive) offer(positions)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        database.addValueEventListener(postListener)
        awaitClose {  }
    }

    override fun deleteWarPosition(position: WarPosition): Flow<Unit> = flow {
        database.child("warPositions").child(position.mid.toString()).removeValue()
        emit(Unit)
    }


}