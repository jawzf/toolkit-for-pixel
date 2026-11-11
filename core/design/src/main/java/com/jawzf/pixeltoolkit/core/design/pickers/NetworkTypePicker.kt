package com.jawzf.pixeltoolkit.core.design.pickers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil

enum class NetworkType {
    NETWORK_5G,
    NETWORK_4G,
    NETWORK_3G,
    NETWORK_OTHER,
}

@Composable
fun NetworkTypePicker(
    selectedTypes: Set<NetworkType>,
    onTypesSelected: (Set<NetworkType>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val labels =
        listOf(
            stringResource(R.string.network_type_5g),
            stringResource(R.string.network_type_4g),
            stringResource(R.string.network_type_3g),
            stringResource(R.string.network_type_other),
        )
    val types =
        listOf(
            NetworkType.NETWORK_5G,
            NetworkType.NETWORK_4G,
            NetworkType.NETWORK_3G,
            NetworkType.NETWORK_OTHER,
        )

    SegmentedTrack(
        count = labels.size,
        modifier =
            modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceBright,
                    shape = RoundedCornerShape(MaterialTheme.shapes.extraSmall.bottomEnd),
                ).padding(horizontal = 16.dp, vertical = 12.dp),
    ) { index ->
        val type = types[index]
        val selected = selectedTypes.contains(type)
        Segment(
            selected = selected,
            selectionModifier =
                Modifier.toggleable(
                    value = selected,
                    role = Role.Checkbox,
                    onValueChange = { checked ->
                        HapticUtil.performUIHaptic(view)
                        val newSelection =
                            if (checked) {
                                selectedTypes + type
                            } else {
                                selectedTypes - type
                            }
                        onTypesSelected(newSelection)
                    },
                ),
        ) {
            SegmentLabel(
                text = labels[index],
                selected = selected,
            )
        }
    }
}
