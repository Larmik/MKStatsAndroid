package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.FAQ
import javax.inject.Inject

@HiltViewModel
class FAQViewModel @Inject constructor(
): ViewModel() {

    val faqCells = listOf(
        FAQ(R.string.faq_1_title, R.string.faq_1_content),
        FAQ(R.string.faq_2_title, R.string.faq_2_content),
        FAQ(R.string.faq_3_title, R.string.faq_3_content),
        FAQ(R.string.faq_4_title, R.string.faq_4_content),
        FAQ(R.string.faq_5_title, R.string.faq_5_content),
        FAQ(R.string.faq_6_title, R.string.faq_6_content),
        FAQ(R.string.faq_7_title, R.string.faq_7_content),
        FAQ(R.string.faq_8_title, R.string.faq_8_content),
        FAQ(R.string.faq_9_title, R.string.faq_9_content),

    )

}