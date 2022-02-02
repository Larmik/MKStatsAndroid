package fr.harmoniamk.statsmk.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import fr.harmoniamk.statsmk.database.firebase.model.FirebaseObject
import fr.harmoniamk.statsmk.database.firebase.model.Team
import fr.harmoniamk.statsmk.database.firebase.model.User
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

    fun getUsers(): Flow<List<User>>
    fun getTeams(): Flow<List<Team>>

    fun getTeam(id: String): Flow<Team?>

    fun listenToUsers(): Flow<List<User>>
    fun listenToTeams(): Flow<List<Team>>
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
class FirebaseRepository @Inject constructor() : FirebaseRepositoryInterface {

    private val database  = Firebase.database.reference

    override fun writeUser(user: User) = flow {
        database.child("users").child(user.mid.toString()).setValue(user)
        emit(Unit)
    }


    override fun listenToUsers(): Flow<List<User>> = callbackFlow {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val users: List<User> = dataSnapshot.child("users").children.map { it.value as Map<*, *> }.map { User(mid = it.get("mid").toString(), name = it.get("name").toString()) }
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
                    accessCode = it["accessCode"].toString(),
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

    override fun getTeam(id: String): Flow<Team?> = callbackFlow {
        database.child("teams").child(id).get().addOnSuccessListener { snapshot ->
            val map = (snapshot.value as? Map<*,*>)
                if (isActive) offer(if (map == null) null else Team(mid = map["mid"].toString(), accessCode = map["accessCode"].toString(), name = map["name"].toString(), shortName = map["shortName"].toString()))

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


}