package fr.harmoniamk.statsmk.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.firebase.*
import fr.harmoniamk.statsmk.model.local.MKWar
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

interface FirebaseRepositoryInterface{
    //Write and edit methods
    fun writeUser(user: User): Flow<Unit>
    fun writeNewWar(war: NewWar): Flow<Unit>
    fun writeCurrentWar(war: NewWar): Flow<Unit>
    fun writeTeam(team: Team): Flow<Unit>
    fun writeDispo(dispo: WarDispo): Flow<Unit>

    //Get lists methods
    fun getUsers(): Flow<List<User>>
    fun getTeams(): Flow<List<Team>>
    fun getNewWars(teamId: String): Flow<List<NewWar>>
    fun getCurrentWar(teamId: String): Flow<MKWar?>
    fun getDispos(): Flow<List<WarDispo>>

    //Firebase event listeners methods
    fun listenToCurrentWar(): Flow<MKWar?>

    //delete methods
    fun deleteUser(user: User): Flow<Unit>
    fun deleteTeam(team: Team): Flow<Unit>
    fun deleteNewWar(war: MKWar): Flow<Unit>
    fun deleteCurrentWar(): Flow<Unit>

}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
interface FirebaseRepositoryModule {
    @Binds
    @Singleton
    fun bindRepository(impl: FirebaseRepository): FirebaseRepositoryInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class FirebaseRepository @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface, private val databaseRepository: DatabaseRepositoryInterface, private val remoteConfigRepository: RemoteConfigRepositoryInterface) : FirebaseRepositoryInterface, CoroutineScope {

    private val database  = Firebase.database.reference

    override fun writeUser(user: User) = flow {
        Log.d("MKDebugOnly", "FirebaseRepository writeUser ${user.name}")
        database.child("users").child(user.mid).setValue(user)
        emit(Unit)
    }.flatMapLatest { databaseRepository.writeUser(user) }

    override fun writeNewWar(war: NewWar): Flow<Unit> = flow {
        preferencesRepository.currentTeam?.mid?.let {
            Log.d("MKDebugOnly", "FirebaseRepository writeNewWar ${war.mid}")
            database.child("newWars").child(it).child(war.mid).setValue(war)
            emit(Unit)
        }
    }

    override fun writeCurrentWar(war: NewWar): Flow<Unit> = flow {
        preferencesRepository.currentTeam?.mid?.let {
            Log.d("MKDebugOnly", "FirebaseRepository writeCurrentWar ${war.mid}")
            database.child("currentWars").child(it).setValue(war)
            emit(Unit)
        }
    }

    override fun writeTeam(team: Team): Flow<Unit> = flow {
        Log.d("MKDebugOnly", "FirebaseRepository writeTeam ${team.name}")
        database.child("teams").child(team.mid).setValue(team)
        emit(Unit)
    }.flatMapLatest { databaseRepository.writeTeam(team) }

    override fun writeDispo(dispo: WarDispo) = flow {
        val index = when (dispo.dispoHour) {
            18 -> 0
            20 -> 1
            21 -> 2
            22 -> 3
            23 -> 4
            else -> -1
        }
        database.child("dispos").child(preferencesRepository.currentTeam?.mid ?: "").child(index.toString()).setValue(dispo)
        emit(Unit)
    }

    override fun getUsers(): Flow<List<User>> = callbackFlow {
        Log.d("MKDebugOnly", "FirebaseRepository getUsers")
        database.child("users").get().addOnSuccessListener { snapshot ->
            val users: List<User> = snapshot.children
                .map { it.value as Map<*, *> }
                .map { User(
                    mid = it["mid"].toString(),
                    name = it["name"].toString(),
                    currentWar = it["currentWar"].toString(),
                    team = it["team"].toString(),
                    role = it["role"].toString().toIntOrNull(),
                    picture = it["picture"].toString(),
                    formerTeams = it["formerTeams"].toStringList(),
                    allyTeams = it["allyTeams"].toStringList(),
                    friendCode = it["friendCode"].toString(),
                    discordId = it["discordId"].toString(),
                    mkcId = it["mkcId"].toString()
                ) }
            if (isActive) trySend(users)
        }
        awaitClose {  }
    }

    override fun getTeams(): Flow<List<Team>> = callbackFlow {
        Log.d("MKDebugOnly", "FirebaseRepository getTeams")
        database.child("teams").get().addOnSuccessListener { snapshot ->
            val teams: List<Team> = snapshot.children
                .map { it.value as Map<*, *> }
                .map { Team(
                    mid = it["mid"].toString(),
                    name = it["name"].toString(),
                    shortName = it["shortName"].toString(),
                ) }
            if (isActive) trySend(teams)
        }
        awaitClose {  }
    }

    override fun getNewWars(teamId: String): Flow<List<NewWar>> = callbackFlow {
        Log.d("MKDebugOnly", "FirebaseRepository getNewWars")
         database.child("newWars").child(teamId).get().addOnSuccessListener { snapshot ->
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
            if (isActive) trySend(wars)
        }
        awaitClose {  }
    }

    override fun getCurrentWar(teamId: String): Flow<MKWar?>  = callbackFlow {
        Log.d("MKDebugOnly", "FirebaseRepository getCurrentWar")
          database.child("currentWars").child(teamId).get().addOnSuccessListener { snapshot ->
              (snapshot.value as? Map<*,*>)?.let { value ->
                  launch {
                      val war = NewWar(
                          mid = value["mid"].toString(),
                          playerHostId = value["playerHostId"].toString(),
                          teamOpponent = value["teamOpponent"].toString(),
                          teamHost = value["teamHost"].toString(),
                          createdDate = value["createdDate"].toString(),
                          warTracks = value["warTracks"].toMapList().parseTracks(),
                          penalties = value["penalties"].toMapList().parsePenalties(),
                          isOfficial = value["official"].toString().toBoolean()
                      ).withName(databaseRepository).firstOrNull()
                      if (isActive) trySend(war)


                  }
              } ?: if (isActive) trySend(null)

        }
        awaitClose {  }
    }

    override fun getDispos(): Flow<List<WarDispo>> = callbackFlow {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val wars: List<WarDispo> = dataSnapshot.child("dispos").child(preferencesRepository.currentTeam?.mid ?: "").children
                    .mapNotNull { it.value as? Map<*, *> }
                    .map {map -> WarDispo(
                        dispoHour = map["dispoHour"].toString().toInt(),
                        dispoPlayers = map["dispoPlayers"]?.toMapList().parsePlayerDispos(),
                        opponentId = map["opponentId"]?.toString(),
                        details = map["details"]?.toString(),
                        lineUp = map["lineUp"]?.toMapList().parseLineUp(),
                        host = map["host"]?.toString()
                    )
                    }
                if (isActive) trySend(wars)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        database.addValueEventListener(postListener)
        awaitClose { database.removeEventListener(postListener) }
    }

    override fun listenToCurrentWar(): Flow<MKWar?> = callbackFlow {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                launch {
                    Log.d("MKDebugOnly", "FirebaseRepository listenCurrentWar")
                    val war = when (val value = dataSnapshot.child("currentWars").child(preferencesRepository.mkcTeam?.id.orEmpty()).value as? Map<*,*>) {
                        null -> null
                        else -> NewWar(
                            mid = value["mid"].toString(),
                            playerHostId = value["playerHostId"].toString(),
                            teamOpponent = value["teamOpponent"].toString(),
                            teamHost = value["teamHost"].toString(),
                            createdDate = value["createdDate"].toString(),
                            warTracks = value["warTracks"].toMapList().parseTracks(),
                            penalties = value["penalties"].toMapList().parsePenalties(),
                            isOfficial = value["official"].toString().toBoolean()
                        ).withName(databaseRepository).firstOrNull()
                    }
                    if (isActive) trySend(war)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        database.addValueEventListener(postListener)
        awaitClose { database.removeEventListener(postListener) }
    }


    override fun deleteUser(user: User) = flow {
        Log.d("MKDebugOnly", "FirebaseRepository deleteUser ${user.name}")
        database.child("users").child(user.mid).removeValue()
        emit(Unit)
    }.flatMapLatest { databaseRepository.deleteUser(user) }

    override fun deleteTeam(team: Team) = flow {
        Log.d("MKDebugOnly", "FirebaseRepository deleteTeam ${team.name}")
        database.child("teams").child(team.mid).removeValue()
        emit(Unit)
    }.flatMapLatest { databaseRepository.deleteTeam(team) }

    override fun deleteNewWar(war: MKWar) = flow {
        war.war?.let {war ->
            preferencesRepository.currentTeam?.mid?.let {
                Log.d("MKDebugOnly", "FirebaseRepository deleteNewWar ${war.mid}")
                database.child("newWars").child(it).child(war.mid).removeValue()
                emit(Unit)
            }

        }
    }.flatMapLatest { databaseRepository.deleteWar(war) }

    override fun deleteCurrentWar() = flow {
        preferencesRepository.currentTeam?.mid?.let {
            Log.d("MKDebugOnly", "FirebaseRepository deleteCurrentWar")
            database.child("currentWars").child(it).removeValue()
            emit(Unit)
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO
}