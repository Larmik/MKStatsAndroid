package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import fr.harmoniamk.statsmk.R

@Composable
fun MKText(modifier: Modifier = Modifier, text: Any, font: Int = R.font.montserrat_regular, fontSize: Int = 14, textColor: Int = R.color.harmonia_dark) {
    Text(
        text = when (text) {
            is Int -> stringResource(id = text)
            else -> text.toString()
        },
        fontFamily = FontFamily(Font(font)),
        modifier = modifier,
        textAlign = TextAlign.Center,
        fontSize = TextUnit(fontSize.toFloat(), TextUnitType.Sp),
        color = colorResource(id = textColor)
    )
}