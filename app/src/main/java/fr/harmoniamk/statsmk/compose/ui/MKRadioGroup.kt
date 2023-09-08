package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.harmoniamk.statsmk.enums.UserRole

@Composable
fun MKRadioGroup(defaultOption: Any, options: List<Any>, onSelected: (Int) -> Unit) {
    val defaultRole = defaultOption as? UserRole
    val roles = options as? List<UserRole>

    val (selectedOption, onOptionSelected) = remember { mutableStateOf(defaultRole) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        roles?.forEach { role ->
            Row(modifier = Modifier.selectable(
                        selected = (role == selectedOption),
                        onClick = {
                            onOptionSelected(role)
                            onSelected(role.ordinal)
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (role == selectedOption),
                    onClick = {
                        onOptionSelected(role)
                        onSelected(role.ordinal)
                    }
                )
                MKText(text = role.labelId)
            }
        }
    }

}