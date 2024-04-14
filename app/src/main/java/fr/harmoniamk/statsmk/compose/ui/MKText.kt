package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R

private const val TEXT_SCALE_REDUCTION_INTERVAL = 0.9f
@Composable
fun MKText(modifier: Modifier = Modifier, text: Any, font: Int = R.font.montserrat_regular, fontSize: Int = 14, textColor: Int = R.color.black, maxLines: Int = Integer.MAX_VALUE, textAlign: TextAlign = TextAlign.Center) {
    val targetTextSizeHeight = TextUnit(fontSize.toFloat(), TextUnitType.Sp)
    val textSize = remember { mutableStateOf(targetTextSizeHeight) }
    Text(
        text = when (text) {
            is Int -> stringResource(id = text)
            else -> text.toString()
        },
        fontFamily = FontFamily(Font(font)),
        modifier = modifier,
        textAlign = textAlign,
        fontSize = textSize.value,
        color = colorResource(id = textColor),
        overflow = TextOverflow.Ellipsis,
        maxLines = maxLines
    )
}