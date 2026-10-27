package id.haikal.waktusholat.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import id.haikal.waktusholat.R
import id.haikal.waktusholat.ui.theme.Dimens
import id.haikal.waktusholat.ui.theme.NextBadgeTokens

@Composable
fun NextBadge(modifier: Modifier = Modifier) {
    Surface(
        color = NextBadgeTokens.background,
        contentColor = NextBadgeTokens.foreground,
        shape = CircleShape,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.home_next_badge),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = Dimens.Space2, vertical = Dimens.Space1),
        )
    }
}
