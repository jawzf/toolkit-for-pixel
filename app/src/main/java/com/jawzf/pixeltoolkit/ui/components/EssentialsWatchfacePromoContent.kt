package com.jawzf.pixeltoolkit.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jawzf.pixeltoolkit.R
import com.jawzf.pixeltoolkit.viewmodels.WatchViewModel

@Composable
fun EssentialsWatchfacePromoContent(
    modifier: Modifier = Modifier,
    watchViewModel: WatchViewModel = viewModel(),
) {
    val context = LocalContext.current

    StackedActionCard(
        iconRes = R.drawable.rounded_watch_24,
        title = stringResource(R.string.watchface_promo_step3_title),
        description = stringResource(R.string.watchface_promo_step3_desc),
        actionLabel = stringResource(R.string.watchface_promo_step3_action),
        actionIconRes = R.drawable.rounded_open_in_new_24,
        onAction = { watchViewModel.openWatchfacePlayStoreOnWatch(context) },
        modifier = modifier.fillMaxWidth(),
    )
}
