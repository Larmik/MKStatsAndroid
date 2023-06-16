package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R

@Composable
fun MKButton(text: Int, enabled: Boolean = true, hasBackground: Boolean = true, onClick: () -> Unit) {
    val bgcolor = when (hasBackground) {
        true -> colorResource(id = R.color.harmonia_dark)
        else -> colorResource(id = R.color.transparent)
    }
    val textColor = when (hasBackground) {
        true -> colorResource(id = R.color.white)
        else -> colorResource(id = R.color.harmonia_dark)
    }
    val elevation = when (hasBackground) {
        true -> ButtonDefaults.elevation()
        else -> null
    }
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(5.dp),
        enabled = enabled,
        elevation = elevation,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = bgcolor,
            disabledBackgroundColor = colorResource(R.color.boo),
            contentColor = textColor,
            disabledContentColor = colorResource(R.color.white)
        ),
        content = {
            Text(
                text = stringResource(text)
            )
        }
    )
}

@Composable
@Preview
fun MKButtonPreviewEnabled() {
    MKButton(R.string.cr_er_une_war, true){}
}

@Composable
@Preview
fun MKButtonPreviewDisabled() {
    MKButton(R.string.cr_er_une_war, false){}
}

@Composable
@Preview
fun MKButtonPreviewNoBackground() {
    MKButton(R.string.cr_er_une_war, hasBackground = false){}
}
