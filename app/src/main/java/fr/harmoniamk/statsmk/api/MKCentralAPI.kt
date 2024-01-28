package fr.harmoniamk.statsmk.api

import fr.harmoniamk.statsmk.model.network.MKCFullPlayer
import fr.harmoniamk.statsmk.model.network.MKCFullTeam
import fr.harmoniamk.statsmk.model.network.MKCPlayerList
import fr.harmoniamk.statsmk.model.network.MKCTeamResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MKCentralAPI {

    companion object {
        const val baseUrl = "https://www.mariokartcentral.com/mkc/api/registry/"
    }

    @GET("teams/category/150cc")
    fun getTeams() : Call<MKCTeamResponse>

    @GET("teams/category/historical")
    fun getHistoricalTeams() : Call<MKCTeamResponse>

    @GET("players/{id}")
    fun getPlayer(@Path("id") id: String?) : Call<MKCFullPlayer>

    @GET("teams/{id}")
    fun getTeam(@Path("id") id: String?) : Call<MKCFullTeam>

    @GET("players/category/150cc")
    fun searchPlayers(@Query("search") search: String) : Call<MKCPlayerList>

}