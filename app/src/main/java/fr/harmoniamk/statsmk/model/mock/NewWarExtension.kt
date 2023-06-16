package fr.harmoniamk.statsmk.model.mock

import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.NewWarPositions
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack

fun NewWar.Companion.mock() = NewWar(
    mid = "mid",
    playerHostId = "",
    teamHost = "",
    teamOpponent = "",
    createdDate = "16/03/2023 - 20h01",
    warTracks = listOf(
        NewWarTrack.mock(),
        NewWarTrack.mock(),
        NewWarTrack.mock(),
        NewWarTrack.mock(),
    ),
    penalties = listOf(),
    isOfficial = false
)
fun NewWarTrack.Companion.mock() = NewWarTrack(
    mid = "",
    trackIndex = 20,
    warPositions = listOf(
        NewWarPositions.mock(1),
        NewWarPositions.mock(2),
        NewWarPositions.mock(5),
        NewWarPositions.mock(7),
        NewWarPositions.mock(9),
        NewWarPositions.mock(12)
    ),
    shocks = listOf()
)
fun NewWarPositions.Companion.mock(position: Int) = NewWarPositions(
    mid = "mid",
    playerId = "playerId",
    position = position
)
