package fr.harmoniamk.statsmk.datasource

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.database.MKDatabase
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.local.MKWar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface WarLocalDataSourceInterface {
    fun getAll(): Flow<List<MKWar>>
    fun getById(id: String): Flow<MKWar>
    fun insert(wars: List<MKWar>): Flow<Unit>
    fun insert(war: MKWar): Flow<Unit>
    fun delete(war: MKWar): Flow<Unit>
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

    override fun getAll(): Flow<List<MKWar>> = dao.getAll().map { list -> list.map { it.toMKWar() } }

    override fun getById(id: String): Flow<MKWar> = dao.getById(id).map { it.toMKWar() }

    override fun insert(wars: List<MKWar>): Flow<Unit> = flow { emit(dao.bulkInsert(wars.map { it.war?.toEntity(it.name) })) }

    override fun insert(war: MKWar): Flow<Unit> = flow { emit(dao.insert(war.war?.toEntity(war.name))) }

    override fun delete(war: MKWar): Flow<Unit> = flow { emit(dao.delete(war.war?.toEntity(war.name))) }

}