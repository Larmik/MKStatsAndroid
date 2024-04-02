package fr.harmoniamk.statsmk.datasource

import android.util.Log
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.api.MKCentralAPI
import fr.harmoniamk.statsmk.api.RetrofitUtils
import fr.harmoniamk.statsmk.model.network.MKCFullPlayer
import fr.harmoniamk.statsmk.model.network.MKCFullTeam
import fr.harmoniamk.statsmk.model.network.MKCPlayer
import fr.harmoniamk.statsmk.model.network.MKCPlayerList
import fr.harmoniamk.statsmk.model.network.MKCTeam
import fr.harmoniamk.statsmk.model.network.MKCTeamResponse
import fr.harmoniamk.statsmk.model.network.NetworkResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import retrofit2.Call
import retrofit2.Callback
import javax.inject.Inject
import javax.inject.Singleton

interface MKCentralNetworkDataSourceInterface {
    fun getTeams(category: String): Flow<NetworkResponse<List<MKCTeam>>>
    fun getTeam(id: String): Flow<NetworkResponse<MKCFullTeam>>
    fun getPlayer(id: String): Flow<NetworkResponse<MKCFullPlayer>>
    fun searchPlayers(search: String): Flow<NetworkResponse<List<MKCPlayer>>>
}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
interface MKCentralNetworkDataSourceModule {
    @Singleton
    @Binds
    fun bind(impl: MKCentralNetworkDataSource): MKCentralNetworkDataSourceInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class MKCentralNetworkDataSource @Inject constructor() : MKCentralNetworkDataSourceInterface {

    override fun getTeams(category: String): Flow<NetworkResponse<List<MKCTeam>>> = callbackFlow {
        val call = RetrofitUtils.createRetrofit(apiClass = MKCentralAPI::class.java, url = MKCentralAPI.baseUrl,).getTeams(category)
        call.enqueue(object : Callback<MKCTeamResponse> {

            override fun onResponse(
                call: Call<MKCTeamResponse>,
                response: retrofit2.Response<MKCTeamResponse>
            ) {
                Log.d("MKDebugOnly", "MKC teams onResponse: ")
                val result = response.body()
                trySend(NetworkResponse.Success(result?.data.orEmpty()))
            }

            override fun onFailure(call: Call<MKCTeamResponse>, t: Throwable) {
                Log.d("MKDebugOnly", "MKC team onFailure: ${t.message}")
                trySend(NetworkResponse.Error(t.message.orEmpty()))
            }

        })
        awaitClose { call.cancel() }
    }


    override fun getTeam(id: String): Flow<NetworkResponse<MKCFullTeam>>  = callbackFlow {
        val call = RetrofitUtils.createRetrofit(apiClass = MKCentralAPI::class.java, url = MKCentralAPI.baseUrl,).getTeam(id)
        call.enqueue(object : Callback<MKCFullTeam> {

            override fun onResponse(
                call: Call<MKCFullTeam>,
                response: retrofit2.Response<MKCFullTeam>
            ) {
                Log.d("MKDebugOnly", "MKC team onResponse: ")
                response.body()?.let {
                    trySend(NetworkResponse.Success(it))
                }
            }

            override fun onFailure(call: Call<MKCFullTeam>, t: Throwable) {
                Log.d("MKDebugOnly", "MKC team onFailure: ${t.message}")
                trySend(NetworkResponse.Error(t.message.orEmpty()))
            }

        })
        awaitClose { call.cancel() }
    }


    override fun getPlayer(id: String): Flow<NetworkResponse<MKCFullPlayer>>  = callbackFlow {
        val call = RetrofitUtils.createRetrofit(apiClass = MKCentralAPI::class.java, url = MKCentralAPI.baseUrl).getPlayer(id)
        call.enqueue(object : Callback<MKCFullPlayer> {

            override fun onResponse(
                call: Call<MKCFullPlayer?>,
                response: retrofit2.Response<MKCFullPlayer?>
            ) {
                Log.d("MKDebugOnly", "MKC player onResponse: ")
                when (response.code()) {
                    200 ->  response.body()?.let {
                        trySend(NetworkResponse.Success(it))
                    }
                    else -> trySend(NetworkResponse.Error(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<MKCFullPlayer>, t: Throwable) {
                Log.d("MKDebugOnly", "MKC player onFailure: ${t.message}")
                trySend(NetworkResponse.Error(t.message.orEmpty()))
            }

        })
        awaitClose { call.cancel() }
    }

    override fun searchPlayers(search: String): Flow<NetworkResponse<List<MKCPlayer>>> = callbackFlow {
        val call = RetrofitUtils.createRetrofit(apiClass = MKCentralAPI::class.java, url = MKCentralAPI.baseUrl,).searchPlayers(search)
        call.enqueue(object : Callback<MKCPlayerList> {

            override fun onResponse(
                call: Call<MKCPlayerList?>,
                response: retrofit2.Response<MKCPlayerList?>
            ) {
                Log.d("MKDebugOnly", "MKC player list onResponse: ")
                when (response.code()) {
                    200 ->  response.body()?.let {
                        trySend(NetworkResponse.Success(it.data))
                    }
                    else -> trySend(NetworkResponse.Success(listOf()))
                }
            }

            override fun onFailure(call: Call<MKCPlayerList>, t: Throwable) {
                Log.d("MKDebugOnly", "MKC player list onFailure: ${t.message}")
                trySend(NetworkResponse.Success(listOf()))
            }

        })
        awaitClose { call.cancel() }
    }

}