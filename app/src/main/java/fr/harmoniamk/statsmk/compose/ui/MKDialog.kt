package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.harmoniamk.statsmk.R

@Composable
fun MKDialog(text: Any? = null, isLoading: Boolean? = null) {
    Dialog(onDismissRequest = { }) {
        Column(
            Modifier
                .background(
                    color = colorResource(id = R.color.white),
                    shape = RoundedCornerShape(5.dp)
                )
                .padding(vertical = 10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            text?.let { MKText(text = it, modifier = Modifier.padding(vertical = 10.dp)) }
            isLoading?.takeIf { it }?.let { CircularProgressIndicator(modifier = Modifier.padding(vertical = 10.dp)) }
        }
    }
}

@Preview
@Composable
fun MKDialogPreview() {
    MKDialog(text = "Création de la war en cours, veuillez patienter...", isLoading = true)
}