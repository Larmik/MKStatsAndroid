package fr.harmoniamk.statsmk.compose.screen

import android.app.Activity
import android.view.Gravity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.activity.MainActivity
import fr.harmoniamk.statsmk.activity.MainViewModel
import fr.harmoniamk.statsmk.compose.ui.HtmlText
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKButton
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.viewModel.CoffeePurchaseState
import fr.harmoniamk.statsmk.compose.viewModel.CoffeeViewModel
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel
import fr.harmoniamk.statsmk.extension.getActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterialApi::class, ExperimentalCoroutinesApi::class, FlowPreview::class)
@Composable
fun CoffeeScreen(viewModel: CoffeeViewModel = hiltViewModel()) {
    val context = LocalContext.current

    val mainViewModel: MainViewModel by lazy { ViewModelProvider(context.getActivity() as MainActivity)[MainViewModel::class.java] }
    val colorViewModel: ColorsViewModel = hiltViewModel()

    val state = mainViewModel.sharedCoffeeState.collectAsState()
    val total = viewModel.total.collectAsState()
    val ownTotal = viewModel.ownTotal.collectAsState()
    val lastCoffee = viewModel.lastCoffee.collectAsState()
    val lastOwnCoffee = viewModel.lastOwnCoffee.collectAsState()
    val usersList = viewModel.coffeeUsersList.collectAsState()

    MKBaseScreen(title = "Offrir un café") {
        Column(Modifier.background(color = colorViewModel.secondaryColor), horizontalAlignment = Alignment.CenterHorizontally) {
            MKText(
                text = "N'hésitez pas à me soutenir si vous aimez l'application.",
                newTextColor = colorViewModel.secondaryTextColor,
                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Row {
                CoffeeButton(Modifier.weight(1f), "a_coffee", viewModel::startBilling)
                CoffeeButton(Modifier.weight(1f), "three_coffees", viewModel::startBilling)
            }
            Row {
                CoffeeButton(Modifier.weight(1f), "five_coffees", viewModel::startBilling)
                CoffeeButton(Modifier.weight(1f), "ten_coffees", viewModel::startBilling)
            }
            if (viewModel.isGod)
                MKButton("Regarder une vidéo") {
                    viewModel.showAd(context)
                }
        }
        if (state.value is CoffeePurchaseState.Pending)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.coffee),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                MKText(text = R.string.coffee_in_progress, modifier = Modifier.padding(10.dp))
            }
        ownTotal.value?.let { totalOwn ->
            Column(
                Modifier
                    .height(170.dp)
                    .fillMaxWidth()
                    .background(
                        color = colorViewModel.secondaryColorAlphaed,
                        shape = RoundedCornerShape(5.dp)
                    ), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        when {
                            totalOwn < 15 -> Image(
                                modifier = Modifier.size(100.dp),
                                painter = painterResource(R.drawable.coffee_level_1),
                                contentDescription = null
                            )

                            totalOwn in 15..29 -> Image(
                                modifier = Modifier.size(100.dp),
                                painter = painterResource(R.drawable.coffee_level_2),
                                contentDescription = null
                            )

                            totalOwn in 30..59 -> Image(
                                modifier = Modifier.size(100.dp),
                                painter = painterResource(R.drawable.coffee_level_3),
                                contentDescription = null
                            )

                            totalOwn in 60..99 -> Image(
                                modifier = Modifier.size(100.dp),
                                painter = painterResource(R.drawable.coffee_level_4),
                                contentDescription = null
                            )

                            totalOwn > 100 -> Image(
                                modifier = Modifier.size(100.dp),
                                painter = painterResource(R.drawable.coffee_level_5),
                                contentDescription = null
                            )
                        }
                        MKText(
                            text = when {
                                totalOwn < 15 -> "Niveau 1"
                                totalOwn in 15..29 -> "Niveau 2"
                                totalOwn in 30..59 -> "Niveau 3"
                                totalOwn in 60..99 -> "Niveau 4"
                                totalOwn > 100 -> "Niveau 5"
                                else -> ""
                            }, font = R.font.montserrat_bold
                        )
                    }
                    lastOwnCoffee.value?.let {
                        Column(
                            Modifier
                                .padding(10.dp)
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val label = when {
                                totalOwn > 1 -> "cafés ensemble"
                                else -> "café ensemble"
                            }
                            MKText(text = "Nous avons pris")
                            MKText(
                                text = totalOwn.toString(),
                                font = R.font.orbitron_semibold,
                                fontSize = 18
                            )
                            MKText(text = label, fontSize = 12)
                            Spacer(Modifier.size(20.dp))
                            MKText(text = "Le dernier en date était le", fontSize = 12)
                            MKText(text = it, fontSize = 16, font = R.font.montserrat_bold)
                        }
                    }
                }

                val remaining = when {
                    totalOwn < 15 -> 15 - totalOwn
                    totalOwn in 15..29 -> 30 - totalOwn
                    totalOwn in 30..59 -> 60 - totalOwn
                    totalOwn in 60..99 -> 100 - totalOwn
                    else -> -1
                }
                val remainingHtml = when (remaining > 1) {
                    true -> "Encore <b>${remaining} cafés</b> pour passer au niveau suivant."
                    else -> "Encore <b>${remaining} café</b> pour passer au niveau suivant."
                }
                HtmlText(
                    html = remainingHtml,
                    gravity = Gravity.CENTER,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                    textStyle = TextStyle.Default.copy(
                        fontSize = TextUnit(12f, TextUnitType.Sp),
                        textAlign = TextAlign.Center
                    )
                )

            }


        }


        Row(Modifier.height(100.dp), verticalAlignment = Alignment.CenterVertically) {
            total.value?.let {
                Column(
                    Modifier
                        .padding(10.dp)
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            colorViewModel.secondaryColorAlphaed,
                            shape = RoundedCornerShape(5.dp)
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    MKText(text = it.toString(), font = R.font.orbitron_semibold, fontSize = 18)
                    MKText(
                        text = when (it > 1) {
                            true -> "cafés offerts"
                            else -> "café offert"
                        }, font = R.font.montserrat_bold
                    )
                    MKText(text = "par la communauté", fontSize = 12)
                }
            }
            lastCoffee.value?.let {
                Column(
                    Modifier
                        .padding(10.dp)
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            colorViewModel.secondaryColorAlphaed,
                            shape = RoundedCornerShape(5.dp)
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    MKText(text = "Dernier café offert le", fontSize = 12)
                    MKText(text = it.second, fontSize = 16, font = R.font.montserrat_bold)
                    MKText(text = "par", fontSize = 12)
                    MKText(text = it.first, font = R.font.montserrat_bold)
                }
            }
        }
        usersList.value.takeIf { it.isNotEmpty() }?.let { list ->
            MKText(text = "Merci à tous ceux qui soutiennent le projet :")
            LazyColumn {
                items(list) {
                    CoffeeUserItem(it)
                }
            }
        }

    }
}

@Composable
fun CoffeeButton(
    modifier: Modifier = Modifier,
    productId: String,
    onClick: (Activity, String) -> Unit
) {
    val colorsViewModel: ColorsViewModel = hiltViewModel()
    val activity = LocalContext.current.getActivity()
    val numberLabel = when (productId) {
        "three_coffees" -> "x3"
        "five_coffees" -> "x5"
        "ten_coffees" -> "x10"
        else -> ""
    }
    val label = when (productId) {
        "a_coffee" -> "Offrir un café"
        "three_coffees" -> "Offrir trois cafés"
        "five_coffees" -> "Offrir cinq cafés"
        "ten_coffees" -> "Offrir dix cafés"
        else -> ""
    }
    Column(
        modifier
            .padding(5.dp)
            .background(colorsViewModel.secondaryColorAlphaed, shape = RoundedCornerShape(5.dp))
            .clickable {
                activity?.let {
                    onClick(it, productId)
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.size(5.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(R.drawable.coffee),
                contentDescription = null,
                modifier = Modifier.size(25.dp)
            )
            productId.takeIf { it != "a_coffee" }?.let {
                MKText(text = numberLabel, font = R.font.orbitron_semibold, newTextColor = colorsViewModel.secondaryTextColor)
            }

        }
        MKText(text = label, font = R.font.roboto, newTextColor = colorsViewModel.secondaryTextColor)
        Spacer(Modifier.size(5.dp))
    }
}

@Composable
fun CoffeeUserItem(pair: Pair<String, Int>) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                pair.second < 15 -> Image(
                    modifier = Modifier.size(25.dp),
                    painter = painterResource(R.drawable.coffee_level_1),
                    contentDescription = null
                )

                pair.second in 15..29 -> Image(
                    modifier = Modifier.size(25.dp),
                    painter = painterResource(R.drawable.coffee_level_2),
                    contentDescription = null
                )

                pair.second in 30..59 -> Image(
                    modifier = Modifier.size(25.dp),
                    painter = painterResource(R.drawable.coffee_level_3),
                    contentDescription = null
                )

                pair.second in 60..99 -> Image(
                    modifier = Modifier.size(25.dp),
                    painter = painterResource(R.drawable.coffee_level_4),
                    contentDescription = null
                )

                pair.second > 100 -> Image(
                    modifier = Modifier.size(25.dp),
                    painter = painterResource(R.drawable.coffee_level_5),
                    contentDescription = null
                )
            }
            MKText(
                text = when {
                    pair.second < 15 -> "Niveau 1"
                    pair.second in 15..29 -> "Niveau 2"
                    pair.second in 30..59 -> "Niveau 3"
                    pair.second in 60..99 -> "Niveau 4"
                    pair.second > 100 -> "Niveau 5"
                    else -> ""
                }, fontSize = 10
            )
        }

        MKText(text = pair.first, fontSize = 18, font = R.font.montserrat_bold)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.coffee),
                contentDescription = null,
                modifier = Modifier.size(25.dp)
            )
            MKText(text = "x${pair.second}", font = R.font.orbitron_semibold)
        }
    }

}