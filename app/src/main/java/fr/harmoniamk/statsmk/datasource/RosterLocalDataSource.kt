package fr.harmoniamk.statsmk.datasource

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.database.MKDatabase
import fr.harmoniamk.statsmk.database.entities.MKCRosterEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

interface RosterLocalDataSourceInterface {
    fun getAll(): Flow<List<MKCRosterEntity>>
    fun insert(roster: MKCRosterEntity): Flow<Unit>
    fun clear(): Flow<Unit>
}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
interface RosterLocalDataSourceModule {
    @Singleton
    @Binds
    fun bind(impl: RosterLocalDataSource): RosterLocalDataSourceInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class RosterLocalDataSource @Inject constructor(@ApplicationContext private val context: Context) :
    RosterLocalDataSourceInterface {

    private val dao = MKDatabase.getInstance(context).mkcRosterDao()

    override fun getAll(): Flow<List<MKCRosterEntity>> = dao.getAll()
    override fun insert(roster: MKCRosterEntity) = flow { emit(dao.insert(roster)) }
    override fun clear(): Flow<Unit> = flow { emit(dao.clear())}

}