package com.jawzf.pixeltoolkit.ui.features.apps.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.ui.core.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.ui.core.sheets.ToolkitBottomSheet
import com.jawzf.pixeltoolkit.utils.HapticUtil

@Composable
fun StandbyAppsMoveSheet(
    onDismissRequest: () -> Unit,
    onBucketSelected: (Int) -> Unit,
) {
    val view = LocalView.current

    val buckets =
        listOf(
            10 to R.string.standby_bucket_active,
            20 to R.string.standby_bucket_working_set,
            30 to R.string.standby_bucket_frequent,
            40 to R.string.standby_bucket_rare,
            45 to R.string.standby_bucket_restricted,
        )

    ToolkitBottomSheet(
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier =
                Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.standby_apps_move_sheet_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            RoundedCardContainer(
                spacing = 2.dp,
                cornerRadius = 24.dp,
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    itemsIndexed(buckets) { _, (bucketCode, titleRes) ->
                        ListItem(
                            checked = false,
                            onCheckedChange = {
                                HapticUtil.performVirtualKeyHaptic(view)
                                onBucketSelected(bucketCode)
                            },
                            onLongClick = null,
                            enabled = true,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            leadingContent = null,
                            supportingContent = null,
                            trailingContent = {
                                RadioButton(
                                    selected = false,
                                    onClick = null,
                                )
                            },
                            colors =
                                ListItemDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.surfaceBright,
                                ),
                            contentPadding =
                                PaddingValues(
                                    horizontal = 16.dp,
                                    vertical = 16.dp,
                                ),
                            content = {
                                Text(
                                    text = stringResource(titleRes),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}
