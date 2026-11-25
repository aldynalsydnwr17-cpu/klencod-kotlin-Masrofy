package app.fnfmodmanager.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SearchField(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    FnfTextField(value, onValueChange, modifier, placeholder = placeholder, leading = Ic.Search)
}
