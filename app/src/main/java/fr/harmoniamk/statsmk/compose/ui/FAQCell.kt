package fr.harmoniamk.statsmk.compose.ui

import android.view.Gravity
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel
import fr.harmoniamk.statsmk.extension.MKHtml.fromHtml


data class FAQ(val title: Int, val message: Int)

@Composable
fun FAQCell(faq: FAQ, onClick: (() -> Unit)? = null) {
    val colorsViewModel: ColorsViewModel = hiltViewModel()
    val expandTransition = remember {
        expandVertically(
            expandFrom = Alignment.Top,
            animationSpec = tween(150)
        ) + fadeIn(
            animationSpec = tween(150)
        )
    }
    val collapseTransition = remember {
        shrinkVertically(
            shrinkTowards = Alignment.Top,
            animationSpec = tween(150)
        ) + fadeOut(
            animationSpec = tween(150)
        )
    }
    val rotation = remember { Animatable(0f) }
    val isExpanded = remember { mutableStateOf(false) }
    LaunchedEffect(isExpanded.value) {
        rotation.animateTo(
            targetValue = when (isExpanded.value) {
                true -> 180f
                false -> 0f
            }, animationSpec = tween(durationMillis = 150)
        )
    }
    Card(Modifier.padding(5.dp), backgroundColor = colorsViewModel.secondaryColorAlphaed,
        elevation = 0.dp
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick ?: { isExpanded.value = !isExpanded.value}), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                MKText(text = stringResource(faq.title), fontSize = 16)
                Image(painterResource(R.drawable.arrowdown), contentDescription = null, modifier = Modifier
                    .size(25.dp)
                    .rotate(rotation.value), colorFilter = ColorFilter.tint(colorsViewModel.mainTextColor))
            }
            AnimatedVisibility(
                visible = isExpanded.value,
                enter = expandTransition,
                exit = collapseTransition
            ) {
                HtmlText(html = stringResource(faq.message), modifier = Modifier.padding(10.dp))
            }
        }
    }
}


@Composable
fun HtmlText(
    modifier: Modifier = Modifier,
    html: String,
    textStyle: TextStyle = TextStyle.Default.copy(textAlign = TextAlign.Start),
    gravity: Int = Gravity.START
) {
    val colorViewModel: ColorsViewModel = hiltViewModel()
    AndroidView(
        modifier = modifier,
        update = { it.text = fromHtml(it.context, html) },
        factory = { context ->
            val font = ResourcesCompat.getFont(context, R.font.montserrat_regular)
            TextView(context).apply {
                textSize = textStyle.fontSize.value
                setTextColor(colorViewModel.htmlTextColor)
                setGravity(gravity)
                typeface = font
            }
        }
    )
}

