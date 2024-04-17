package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel
import fr.harmoniamk.statsmk.extension.toTeamColor
import fr.harmoniamk.statsmk.model.network.MKCTeam

@Composable
fun MKCTeamItem(team: MKCTeam? = null, onClick: (String) -> Unit) {
    val colorsViewModel: ColorsViewModel = hiltViewModel()
    Card(
        elevation = 0.dp,
        backgroundColor = colorsViewModel.secondaryColorAlphaed,
        modifier = Modifier.padding(bottom = 5.dp).clickable { team?.let { onClick(it.team_id) } },
    ) {
        Row(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .defaultMinSize(minWidth = 70.dp)
                    .background(
                        color = team?.team_color.toTeamColor(),
                        shape = RoundedCornerShape(5.dp)
                    )
            )
            {
                MKText(
                    text = team?.team_tag.orEmpty(),
                    fontSize = 16,
                    font = R.font.montserrat_bold,
                    newTextColor = colorsViewModel.secondaryTextColor,
                    modifier = Modifier.padding(5.dp)
                )
            }
            MKText(
                text = team?.team_name.orEmpty(),
                font = R.font.montserrat_bold,
                fontSize = 18,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
@Preview
fun MKCTeamItemPreview() {
    MKCTeamItem(
        MKCTeam(
            "123",
            "Harmonia",
            "HR",
            6,
            "approved",
            "not_recruiting",
            0,
            16,
            "123456",
            "123456"
        )
    ) {}
}