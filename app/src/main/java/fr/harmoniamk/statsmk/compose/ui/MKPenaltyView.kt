package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.model.firebase.Penalty

@Composable
fun MKPenaltyView(modifier: Modifier = Modifier, penalties: List<Penalty>?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        penalties?.takeIf { it.isNotEmpty() }?.let {
            MKText(
                text = R.string.p_nalit_s,
                font = R.font.montserrat_bold,

                modifier = Modifier.padding(bottom = 10.dp),
                fontSize = 12
            )
            penalties.forEach {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MKText(text = (it.teamShortName ?: it.teamName).orEmpty(), fontSize = 12)
                    MKText(
                        text = String.format(
                            stringResource(id = R.string.minus_placeholder),
                            it.amount.toString()
                        ), fontSize = 12
                    )
                }
            }
        }
    }
}