package com.jawzf.pixeltoolkit.feature.input.ime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.components.ConfigSliderItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.modifiers.highlight
import com.jawzf.pixeltoolkit.core.design.pickers.Segment
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentLabel
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentedTrack
import com.jawzf.pixeltoolkit.feature.input.InputState
import kotlinx.coroutines.delay

@Composable
fun KeyboardSettingsUI(
    state: InputState,
    isKeyboardEnabled: Boolean,
    isKeyboardSelected: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    highlightSetting: String? = null,
) {
    val context = LocalContext.current
    val view = LocalView.current
    var text by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            onRefresh()
            delay(2000)
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Keyboard Setup Section
        if (!isKeyboardEnabled || !isKeyboardSelected) {
            Text(
                text = stringResource(R.string.label_keyboard_setup),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!isKeyboardEnabled) {
                    Button(
                        onClick = { state.openImeSettings(context) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.rounded_settings_24),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.btn_enable_keyboard))
                    }
                } else if (!isKeyboardSelected) {
                    Button(
                        onClick = { state.showImePicker(context) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.rounded_keyboard_24),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.btn_select_keyboard))
                    }
                }
            }
        }

        // Test Field
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(stringResource(R.string.test_keyboard_hint)) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(24.dp),
        )

        // Customization
        Text(
            text = stringResource(R.string.feat_system_keys),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        RoundedCardContainer {
            ConfigSliderItem(
                title = stringResource(R.string.label_keyboard_height),
                value = state.keyboardHeight.floatValue,
                onValueChange = {
                    state.setKeyboardHeight(it, context)
                    HapticUtil
                        .performSliderHaptic(view)
                },
                valueRange = 200f..600f,
                steps = 0,
                modifier = Modifier.highlight(highlightSetting == "keyboard_height"),
            )

            ConfigSliderItem(
                title = stringResource(R.string.label_keyboard_bottom_padding),
                value = state.keyboardBottomPadding.floatValue,
                onValueChange = {
                    state.setKeyboardBottomPadding(it, context)
                    HapticUtil
                        .performSliderHaptic(view)
                },
                valueRange = 0f..100f,
                steps = 0,
                modifier = Modifier.highlight(highlightSetting == "keyboard_bottom_padding"),
            )

            ConfigSliderItem(
                title = stringResource(R.string.label_keyboard_roundness),
                value = state.keyboardRoundness.floatValue,
                onValueChange = {
                    state.setKeyboardRoundness(it, context)
                    HapticUtil
                        .performSliderHaptic(view)
                },
                valueRange = 4f..30f,
                steps = 0,
                modifier = Modifier.highlight(highlightSetting == "keyboard_roundness"),
            )
        }

        RoundedCardContainer {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = stringResource(R.string.label_keyboard_shape),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                val shapes =
                    listOf(
                        R.string.shape_round to 0,
                        R.string.shape_flat to 1,
                        R.string.shape_inverse to 2,
                    )

                SegmentedTrack(count = shapes.size) { index ->
                    val (labelRes, value) = shapes[index]
                    val selected = state.keyboardShape.intValue == value
                    Segment(
                        selected = selected,
                        selectionModifier =
                            Modifier.selectable(
                                selected = selected,
                                role = Role.RadioButton,
                                onClick = { state.setKeyboardShape(value, context) },
                            ),
                    ) {
                        SegmentLabel(
                            text = stringResource(labelRes),
                            selected = selected,
                        )
                    }
                }
            }
        }

        RoundedCardContainer {
            IconToggleItem(
                iconRes = R.drawable.rounded_keyboard_arrow_down_24,
                title = stringResource(R.string.label_keyboard_functions_bottom),
                isChecked = state.isKeyboardFunctionsBottom.value,
                onCheckedChange = { state.setKeyboardFunctionsBottom(it, context) },
                modifier = Modifier.highlight(highlightSetting == "keyboard_functions_bottom"),
            )

            ConfigSliderItem(
                title = stringResource(R.string.label_keyboard_functions_padding),
                value = state.keyboardFunctionsPadding.floatValue,
                onValueChange = {
                    state.setKeyboardFunctionsPadding(it, context)
                    HapticUtil
                        .performSliderHaptic(view)
                },
                valueRange = 0f..100f,
                steps = 0,
                modifier = Modifier.highlight(highlightSetting == "keyboard_functions_padding"),
            )
        }

        RoundedCardContainer {
            IconToggleItem(
                iconRes = R.drawable.rounded_mobile_vibrate_24,
                title = stringResource(R.string.label_keyboard_haptics),
                isChecked = state.isKeyboardHapticsEnabled.value,
                onCheckedChange = { state.setKeyboardHapticsEnabled(it, context) },
                modifier = Modifier.highlight(highlightSetting == "keyboard_haptics"),
            )

            if (state.isKeyboardHapticsEnabled.value) {
                ConfigSliderItem(
                    title = stringResource(R.string.label_keyboard_haptic_strength),
                    value = state.keyboardHapticStrength.floatValue,
                    onValueChange = {
                        state.setKeyboardHapticStrength(it, context)
                    },
                    modifier = Modifier.highlight(highlightSetting == "keyboard_haptic_strength"),
                )
            }

            IconToggleItem(
                iconRes = R.drawable.rounded_nightlight_24,
                title = stringResource(R.string.label_keyboard_always_dark),
                isChecked = state.isKeyboardAlwaysDark.value,
                onCheckedChange = { state.setKeyboardAlwaysDark(it, context) },
                modifier = Modifier.highlight(highlightSetting == "keyboard_always_dark"),
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_invert_colors_24,
                title = stringResource(R.string.label_keyboard_pitch_black),
                isChecked = state.isKeyboardPitchBlack.value,
                onCheckedChange = { state.setKeyboardPitchBlack(it, context) },
                modifier = Modifier.highlight(highlightSetting == "keyboard_pitch_black"),
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_content_paste_24,
                title = stringResource(R.string.label_keyboard_clipboard_enabled),
                isChecked = state.isKeyboardClipboardEnabled.value,
                onCheckedChange = { state.setKeyboardClipboardEnabled(it, context) },
                modifier = Modifier.highlight(highlightSetting == "keyboard_clipboard_enabled"),
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_keyboard_24,
                title = stringResource(R.string.label_keyboard_long_press_symbols),
                isChecked = state.isLongPressSymbolsEnabled.value,
                onCheckedChange = { state.setLongPressSymbolsEnabled(it, context) },
                modifier = Modifier.highlight(highlightSetting == "keyboard_long_press_symbols"),
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_keyboard_24,
                title = stringResource(R.string.label_keyboard_accented_characters),
                isChecked = state.isAccentedCharactersEnabled.value,
                onCheckedChange = { state.setAccentedCharactersEnabled(it, context) },
                enabled = state.isLongPressSymbolsEnabled.value,
                modifier = Modifier.highlight(highlightSetting == "keyboard_accented_characters"),
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_book_2_24,
                title = "User Dictionary (Learn words)",
                isChecked = state.isUserDictionaryEnabled.value,
                onCheckedChange = { state.setUserDictionaryEnabled(it, context) },
                modifier = Modifier.highlight(highlightSetting == "user_dictionary_enabled"),
            )

            if (state.isUserDictionaryEnabled.value) {
                IconToggleItem(
                    iconRes = R.drawable.rounded_settings_24,
                    title = "Manage Learned Words",
                    isChecked = false,
                    showToggle = false,
                    onCheckedChange = { state.isUserDictionarySheetVisible.value = true },
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        if (state.isUserDictionarySheetVisible.value) {
            UserDictionaryBottomSheet(
                state = state,
                onDismissRequest = { state.isUserDictionarySheetVisible.value = false },
            )
        }
    }
}
