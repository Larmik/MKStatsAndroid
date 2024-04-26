package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel

@Composable
fun MKText(modifier: Modifier = Modifier, text: Any, font: Int = R.font.montserrat_regular, fontSize: Int = 14, textColor: Int? = null, newTextColor: Color? = null, maxLines: Int = Integer.MAX_VALUE, textAlign: TextAlign = TextAlign.Center) {
    val targetTextSizeHeight = TextUnit(fontSize.toFloat(), TextUnitType.Sp)
    val textSize = remember { mutableStateOf(targetTextSizeHeight) }
    val colorViewModel: ColorsViewModel = hiltViewModel()
    val color = when {
        newTextColor != null -> newTextColor
        textColor != null -> colorResource(textColor)
        else -> colorViewModel.mainTextColor
    }
    Text(
        text = when (text) {
            is Int -> stringResource(id = text)
            else -> text.toString()
        },
        fontFamily = FontFamily(Font(font)),
        modifier = modifier,
        textAlign = textAlign,
        fontSize = textSize.value,
        color =  color,
        overflow = TextOverflow.Ellipsis,
        maxLines = maxLines
    )
}