package com.jawzf.pixeltoolkit.core.design.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitShapes
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitTheme

/** Quiet Utility search field: white, 14 dp corners, accent border when focused, round clear button. */
@Composable
fun ToolkitSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = { SearchFieldIcon() },
    showClear: Boolean = query.isNotEmpty(),
    onClear: () -> Unit = { onQueryChange("") },
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val c = ToolkitTheme.colors
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.heightIn(min = 48.dp),
        placeholder = { Text(placeholder, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = leadingIcon,
        trailingIcon =
            if (showClear) {
                { ClearButton(onClear) }
            } else {
                null
            },
        singleLine = true,
        shape = ToolkitShapes.field,
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = c.accent,
                unfocusedBorderColor = c.fieldBorder,
                focusedContainerColor = c.surface,
                unfocusedContainerColor = c.surface,
                cursorColor = c.accent,
                focusedTextColor = c.text,
                unfocusedTextColor = c.text,
                focusedPlaceholderColor = c.textSecondary,
                unfocusedPlaceholderColor = c.textSecondary,
                focusedLeadingIconColor = c.textSecondary,
                unfocusedLeadingIconColor = c.textSecondary,
            ),
    )
}

/** Default leading search icon for search fields. */
@Composable
fun SearchFieldIcon() {
    Icon(
        painter = painterResource(R.drawable.rounded_search_24),
        contentDescription = null,
        modifier = Modifier.size(20.dp),
    )
}

@Composable
private fun ClearButton(onClear: () -> Unit) {
    val c = ToolkitTheme.colors
    val view = LocalView.current
    IconButton(
        onClick = {
            HapticUtil.performVirtualKeyHaptic(view)
            onClear()
        },
        modifier = Modifier.size(36.dp),
        colors =
            IconButtonDefaults.iconButtonColors(
                containerColor = c.neutralFill,
                contentColor = c.textSecondary,
            ),
    ) {
        Icon(
            painter = painterResource(R.drawable.rounded_close_24),
            contentDescription = stringResource(R.string.action_clear_search),
            modifier = Modifier.size(16.dp),
        )
    }
}
