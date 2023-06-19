package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.enums.ListItems

@Composable
fun MKListItem(item: ListItems, onClick: (String) -> Unit) {
    Column(Modifier.padding(horizontal = 5.dp).clickable { onClick(item.route) }) {
        MKText(text = item.titleRes, modifier = Modifier.padding(vertical = 20.dp))
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(color = colorResource(id = R.color.white)
                )
        )
    }
}

@Preview
@Composable
fun MKListItemPreview() {
    MKListItem(ListItems.manage_players) { }
}