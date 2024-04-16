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
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel
import fr.harmoniamk.statsmk.enums.MenuItems

@Composable
fun MKListItem(item: MenuItems, separator: Boolean = true, onNavigate: (String) -> Unit, onClick: (MenuItems) -> Unit) {
    val colorViewModel: ColorsViewModel = hiltViewModel()
    Column(Modifier.fillMaxWidth().padding(horizontal = 5.dp).clickable {
        when (item.route) {
            null -> onClick(item)
            else -> onNavigate(item.route)
        }
    }) {
        MKText(text = item.titleRes, modifier = Modifier.padding(vertical = if (separator) 20.dp else 15.dp), maxLines = 1, newTextColor = colorViewModel.mainTextColor)
        if (separator)
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(color = colorViewModel.mainTextColor
                    )
            )
    }
}

@Preview
@Composable
fun MKListItemPreview() {
    MKListItem(MenuItems.ManagePlayers(), onNavigate = {}) { }
}