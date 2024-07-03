package fr.harmoniamk.statsmk.compose.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.FAQCell
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.viewModel.FAQViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FAQScreen(viewModel: FAQViewModel = hiltViewModel()) {
    val context = LocalContext.current
    MKBaseScreen(title = "Aide & Contact") {
        LazyColumn {
            items(viewModel.faqCells) { faq ->
                FAQCell(faq, onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://discord.gg/${viewModel.discordCode}")
                        )
                    )
                }.takeIf { faq.message == R.string.empty })
            }
        }
    }
}