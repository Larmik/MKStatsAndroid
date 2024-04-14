package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MKBaseScreen(title: Any, subTitle: Any? = null, verticalArrangement: Arrangement.Vertical = Arrangement.Top, state: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden), sheetContent: (@Composable ColumnScope.() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    when (sheetContent) {
        null -> MKHeaderScreen(
            title = title,
            subTitle = subTitle,
            verticalArrangement = verticalArrangement
        ) { content() }
        else -> ModalBottomSheetLayout(sheetContent = sheetContent, sheetState = state) {
            MKHeaderScreen(
                title = title,
                subTitle = subTitle,
                verticalArrangement = verticalArrangement
            ) { content() }
        }
    }
}