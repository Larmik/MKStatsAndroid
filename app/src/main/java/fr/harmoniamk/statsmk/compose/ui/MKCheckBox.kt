package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R

@Composable
fun MKCheckBox(text: Int, onValue: (Boolean) -> Unit) {
    val checkedState = remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checkedState.value, onCheckedChange = {
            checkedState.value = it
            onValue(it)
        })
        Spacer(modifier = Modifier.width(10.dp))
        MKText(text = text)
    }
}

@Preview(showBackground = true)
@Composable
fun MKCheckboxPreview() {
    MKCheckBox(text = R.string.war_officielle, onValue = {})
}