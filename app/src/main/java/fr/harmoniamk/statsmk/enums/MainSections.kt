package fr.harmoniamk.statsmk.enums

import androidx.fragment.app.Fragment
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.fragment.SettingsFragment
import fr.harmoniamk.statsmk.fragment.TimeTrialFragment
import fr.harmoniamk.statsmk.fragment.TournamentFragment
import fr.harmoniamk.statsmk.fragment.TrackListFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

enum class MainSections(val label: Int, val fragment: Fragment) {
    @FlowPreview
    @ExperimentalCoroutinesApi
    TOURNAMENT(R.string.tournament, TournamentFragment()),
    //TIME_TRIAL(R.string.time_trial, TimeTrialFragment()),
    TEAM_WAR(R.string.team_war, TimeTrialFragment()),
    SETTINGS(R.string.settings, SettingsFragment()),
  //  TRACK_LIST(R.string.tracks, TrackListFragment()),
}