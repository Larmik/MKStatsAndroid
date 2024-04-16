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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel


 @Composable
fun MKHeaderScreen(title: Any?, subTitle: Any? = null, verticalArrangement: Arrangement.Vertical = Arrangement.Top, content: @Composable ColumnScope.() -> Unit) {
     val colorsViewModel: ColorsViewModel = hiltViewModel()
     Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
        .fillMaxHeight()
        .background(
            color = colorsViewModel.mainColor
        ), verticalArrangement = verticalArrangement
    ) {
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier
            .fillMaxWidth()
            .background(color = colorsViewModel.secondaryColor)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = title ?: "", font = R.font.montserrat_bold, fontSize = 20, newTextColor = colorsViewModel.secondaryTextColor, modifier = Modifier.padding(15.dp))
                subTitle?.let { MKText(text = subTitle, font = R.font.montserrat_regular, fontSize = 16, newTextColor = colorsViewModel.secondaryTextColor, modifier = Modifier.padding(top = 5.dp, bottom = 10.dp)) }
            }
        }
        content()
    }
}