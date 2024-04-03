package fr.harmoniamk.statsmk.datasource

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.database.MKDatabase
import fr.harmoniamk.statsmk.model.local.MKWar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface WarLocalDataSourceInterface {
    fun getAll(): Flow<List<MKWar>>
    fun getById(id: String): Flow<MKWar>
    fun insert(wars: List<MKWar>): Flow<Unit>
    fun insert(war: MKWar): Flow<Unit>
    fun delete(war: MKWar): Flow<Unit>
    fun clear(): Flow<Unit>
}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
interface WarLocalDataSourceModule {
    @Binds
    @Singleton
    fun bind(impl: WarLocalDataSource): WarLocalDataSourceInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class WarLocalDataSource @Inject constructor(@ApplicationContext private val context: Context) :
    WarLocalDataSourceInterface {

    private val dao = MKDatabase.getInstance(context).warDao()

    override fun getAll(): Flow<List<MKWar>> = dao.getAll().map { list -> list.map { it.toMKWar() } }.flowOn(Dispatchers.IO)

    override fun getById(id: String): Flow<MKWar> = dao.getById(id).filterNotNull().map { it.toMKWar() }.flowOn(Dispatchers.IO)

    override fun insert(wars: List<MKWar>): Flow<Unit> = flow { emit(dao.bulkInsert(wars.map { it.war?.toEntity(it.name) })) }

    override fun insert(war: MKWar): Flow<Unit> = flow { emit(dao.insert(war.war?.toEntity(war.name))) }

    override fun delete(war: MKWar): Flow<Unit> = flow { emit(dao.delete(war.war?.toEntity(war.name))) }

    override fun clear(): Flow<Unit> = flow { emit(dao.clear()) }

}