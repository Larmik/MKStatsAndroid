package fr.harmoniamk.statsmk.datasource

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.harmoniamk.statsmk.database.MKDatabase
import fr.harmoniamk.statsmk.model.firebase.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

interface UserLocalDataSourceInterface {
    fun getAll(): Flow<List<User>>
    fun getById(id: String): Flow<User>
    fun insert(users: List<User>): Flow<Unit>
    fun insert(user: User): Flow<Unit>
    fun delete(user: User): Flow<Unit>
    fun clear(): Flow<Unit>

}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(ApplicationComponent::class)
interface UserLocalDataSourceModule {
    @Binds
    fun bind(impl: UserLocalDataSource): UserLocalDataSourceInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class UserLocalDataSource @Inject constructor(@ApplicationContext private val context: Context) :
    UserLocalDataSourceInterface {

    private val dao = MKDatabase.getInstance(context).userDao()

    override fun getAll(): Flow<List<User>> = dao.getAll().map { list -> list.map { User(it) } }

    override fun getById(id: String): Flow<User> = dao.getById(id).map {  User(it)  }

    override fun insert(users: List<User>): Flow<Unit> = flow { emit(dao.bulkInsert(users.map { it.toEntity() })) }

    override fun insert(user: User): Flow<Unit> = flow { emit(dao.insert(user.toEntity())) }

    override fun delete(user: User): Flow<Unit> = flow { emit(dao.delete(user.toEntity())) }

    override fun clear(): Flow<Unit> = flow { emit(dao.clear()) }


}