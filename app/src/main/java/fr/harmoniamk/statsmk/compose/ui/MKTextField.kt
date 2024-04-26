package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextFieldDefaults.indicatorLine
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R

@ExperimentalMaterialApi
@Composable
fun MKTextField(
    modifier: Modifier = Modifier.fillMaxWidth(),
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeHolderRes: Int,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    keyboardActions: KeyboardActions = KeyboardActions()
) {
    val colors = TextFieldDefaults.textFieldColors(
        textColor = colorResource(id = R.color.black),
        cursorColor = colorResource(id = R.color.black),
        trailingIconColor = colorResource(id = R.color.black),
        backgroundColor = colorResource(id = R.color.white),
        focusedIndicatorColor = colorResource(id = R.color.transparent),
        unfocusedIndicatorColor = colorResource(id = R.color.transparent),
        disabledIndicatorColor = colorResource(id = R.color.transparent)
    )
    val textColor = textStyle.color.takeOrElse { colors.textColor(true).value }
    val interactionSource = remember { MutableInteractionSource() }
    BasicTextField(
        value = value,
        modifier = modifier
            .padding(vertical = 5.dp)
            .background(colors.backgroundColor(true).value, RoundedCornerShape(5.dp))
            .indicatorLine(true, false, interactionSource, colors)
            .defaultMinSize(
                minWidth = TextFieldDefaults.MinWidth,
                minHeight = 50.dp
            ),
        onValueChange = onValueChange,
        enabled = true,
        readOnly = false,
        textStyle = TextStyle(color = textColor, fontFamily = FontFamily(Font(R.font.montserrat_regular)), fontSize = TextUnit(14f, TextUnitType.Sp)),
        cursorBrush = SolidColor(colors.cursorColor(false).value),
        visualTransformation = when (keyboardType) {
            KeyboardType.Password -> PasswordVisualTransformation()
            else -> VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        keyboardActions =  keyboardActions,
        interactionSource = interactionSource,
        singleLine = true,
        maxLines = 1,
        decorationBox = @Composable { innerTextField ->
            TextFieldDefaults.TextFieldDecorationBox(
                value = value.text,
                visualTransformation = VisualTransformation.None,
                innerTextField = innerTextField,
                placeholder = {
                    MKText(
                        text = stringResource(id = placeHolderRes),
                        textColor = R.color.hint,
                    fontSize = 13
                    )
                },
                label = label,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                singleLine = true,
                enabled = true,
                interactionSource = interactionSource,
                colors = colors,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            )
        }
    )
}

@Preview
@Composable
@ExperimentalMaterialApi
fun MKTextFieldPreview() {
    MKTextField(value = TextFieldValue(""), placeHolderRes = R.string.rechercher_un_advsersaire, onValueChange = {})
}
