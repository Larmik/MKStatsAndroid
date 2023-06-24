package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R

@Composable
fun MKHeaderScreen(title: Any, subTitle: Any? = null, verticalArrangement: Arrangement.Vertical = Arrangement.Top, content: @Composable ColumnScope.() -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
        .fillMaxHeight()
        .background(
            color = colorResource(
                id = R.color.harmonia_clear
            )
        ), verticalArrangement = verticalArrangement
    ) {
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(
                    id = R.color.harmonia_dark
                )
            )) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = title, font = R.font.montserrat_bold, fontSize = 20, textColor = R.color.white, modifier = Modifier.padding(15.dp))
                subTitle?.let { MKText(text = subTitle, font = R.font.montserrat_regular, fontSize = 16, textColor = R.color.white, modifier = Modifier.padding(top = 5.dp, bottom = 10.dp)) }
            }
        }
        content()
    }

}