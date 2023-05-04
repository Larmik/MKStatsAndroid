package fr.harmoniamk.statsmk.repository

import android.content.Context
import android.net.ConnectivityManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

interface NetworkRepositoryInterface {
    val networkAvailable: Boolean
}

@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
interface NetworkRepositoryModule {
    @Binds
    fun bind(impl: NetworkRepository): NetworkRepositoryInterface
}

@ExperimentalCoroutinesApi
class NetworkRepository @Inject constructor(@ApplicationContext var context: Context) : NetworkRepositoryInterface  {

    override val networkAvailable: Boolean
        get()  {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }



}