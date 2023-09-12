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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MKBaseScreen(title: Any, subTitle: Any? = null, verticalArrangement: Arrangement.Vertical = Arrangement.Top,  state: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden), sheetContent: (@Composable ColumnScope.() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
   when (sheetContent) {
       null -> MKHeaderScreen(title, subTitle, verticalArrangement) { content() }
       else -> ModalBottomSheetLayout(sheetContent = sheetContent, sheetState = state) {
           MKHeaderScreen(title, subTitle, verticalArrangement) { content() }
       }
   }
}