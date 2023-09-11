package fr.harmoniamk.statsmk.compose.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.viewModel.Filter
import fr.harmoniamk.statsmk.compose.viewModel.FilterSortViewModel
import fr.harmoniamk.statsmk.compose.viewModel.Sort
import fr.harmoniamk.statsmk.enums.SortType
import fr.harmoniamk.statsmk.extension.isTrue

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FilterSortScreen(viewModel: FilterSortViewModel = hiltViewModel(), sort: Sort, filter: Filter, onDismiss: () -> Unit, onSorted: (SortType) -> Unit) {

    val sortState = viewModel.sortState.collectAsState()
    val filterState = viewModel.filterState.collectAsState()

    BackHandler {
        onDismiss()
    }

    MKBaseScreen(title = R.string.options_de_tri) {

        sort.list.takeIf { it.isNotEmpty() }?.let { list ->
            MKText(
                text = "Trier",
                modifier = Modifier.padding(10.dp),
                font = R.font.montserrat_bold,
                fontSize = 16
            )
            Row(Modifier.padding(10.dp)) {
                list.forEach {
                    val isSelected =
                        sortState.value == it || (sortState.value == null && it == list.first())
                    val bgColor = when (isSelected) {
                        true -> R.color.harmonia_dark
                        else -> R.color.transparent_white
                    }
                    val textColor = when (isSelected) {
                        true -> R.color.white
                        else -> R.color.harmonia_dark
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clickable {
                                viewModel.setSortType(it)
                                onSorted(it)
                           },
                        elevation = 0.dp,
                        backgroundColor = colorResource(id = bgColor)
                    ) {
                            Row(
                                Modifier.wrapContentWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MKText(text = it.resId, textColor = textColor)

                            }

                    }
                    Spacer(Modifier.width(1.dp))
                }
            }
        }

        filter.list.takeIf { it.isNotEmpty() }?.let {
            MKText(
                text = "Filtrer",
                modifier = Modifier.padding(10.dp),
                font = R.font.montserrat_bold,
                fontSize = 16
            )
            Row(Modifier.padding(10.dp)) {
                it.forEach {
                    val isSelected = filterState.value?.contains(it)
                    val bgColor = when (isSelected) {
                        true -> R.color.harmonia_dark
                        else -> R.color.transparent_white
                    }
                    val textColor = when (isSelected) {
                        true -> R.color.white
                        else -> R.color.harmonia_dark
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clickable { viewModel.switchFilter(it, isSelected.isTrue) },
                        elevation = 0.dp,
                        backgroundColor = colorResource(id = bgColor)
                    ) {

                            Row(
                                Modifier.wrapContentWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MKText(text = it.resId, textColor = textColor)

                            }
                        }
                    Spacer(Modifier.width(1.dp))

                }
            }
        }
    }

}