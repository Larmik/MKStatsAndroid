package fr.harmoniamk.statsmk.datasource


import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.database.MKDatabase
import fr.harmoniamk.statsmk.database.entities.TopicEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

interface TopicLocalDataSourceInterface {
    fun getAll(): Flow<List<TopicEntity>>
    fun insert(topic: TopicEntity): Flow<Unit>
    fun delete(topic: String): Flow<Unit>

}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
interface TopicLocalDataSourceModule {
    @Singleton
    @Binds
    fun bind(impl: TopicLocalDataSource): TopicLocalDataSourceInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class TopicLocalDataSource @Inject constructor(@ApplicationContext private val context: Context) :
    TopicLocalDataSourceInterface {

    private val dao = MKDatabase.getInstance(context).topicDao()

    override fun getAll(): Flow<List<TopicEntity>> = dao.getAll()

    override fun insert(topic: TopicEntity): Flow<Unit> = flow {
        emit(dao.insert(topic))
    }

    override fun delete(topic: String): Flow<Unit> = flow {
        emit(dao.delete(topic))
    }

}