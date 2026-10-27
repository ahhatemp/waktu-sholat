package id.haikal.waktusholat.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import id.haikal.waktusholat.R
import id.haikal.waktusholat.domain.HijriDateFormatter
import id.haikal.waktusholat.ui.common.IndonesianDates
import id.haikal.waktusholat.ui.common.LocationRequiredCard
import id.haikal.waktusholat.ui.common.NextBadge
import id.haikal.waktusholat.ui.common.formatCountdown
import id.haikal.waktusholat.ui.common.labelRes
import id.haikal.waktusholat.ui.common.toDisplayString
import id.haikal.waktusholat.ui.theme.Dimens
import id.haikal.waktusholat.ui.theme.NextPrayerCardTokens
import id.haikal.waktusholat.ui.theme.PrayerRowTokens

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onOpenCitySearch: () -> Unit = {},
) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(state = state, onOpenCitySearch = onOpenCitySearch, modifier = modifier)
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onOpenCitySearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        HomeHeader(onEditLocation = onOpenCitySearch)
        Box(Modifier.fillMaxSize()) {
            when {
                state.loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.locationName == null -> {
                    LocationRequiredCard(
                        title = stringResource(R.string.home_no_location_title),
                        body = stringResource(R.string.home_no_location_body),
                        actionLabel = stringResource(R.string.home_pick_location),
                        onAction = onOpenCitySearch,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(Dimens.Space4),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = Dimens.Space4,
                            end = Dimens.Space4,
                            bottom = Dimens.Space6,
                        ),
                        verticalArrangement = Arrangement.spacedBy(Dimens.Space3),
                    ) {
                        item {
                            val next = state.next
                            if (next == null) {
                                ScheduleUnavailableCard()
                            } else {
                                NextPrayerCard(state = state, next = next)
                            }
                        }
                        items(state.slots, key = { it.prayer }) { slot ->
                            val isNext = state.next?.let { !it.isTomorrow && it.prayer == slot.prayer } == true
                            val isCurrent = !isNext && state.current == slot.prayer
                            val isPast = !isNext && !isCurrent &&
                                state.now?.let { slot.time.isBefore(it) } == true
                            PrayerRowItem(
                                slot = slot,
                                isNext = isNext,
                                isCurrent = isCurrent,
                                isPast = isPast,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(onEditLocation: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = Dimens.Space4,
                end = Dimens.Space1,
                top = Dimens.Space3,
                bottom = Dimens.Space2,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onEditLocation) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(R.string.home_change_location),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NextPrayerCard(state: HomeUiState, next: NextPrayerInfo) {
    val date = state.date ?: return
    val location = state.locationName ?: return
    val prayerName = stringResource(next.prayer.labelRes)
    val hours = next.remainingSeconds / 3600
    val minutes = (next.remainingSeconds % 3600) / 60
    val countdownDescription = when {
        hours > 0 -> stringResource(
            R.string.home_countdown_hours,
            prayerName,
            hours.toInt(),
            minutes.toInt(),
        )

        minutes > 0 -> stringResource(
            R.string.home_countdown_minutes,
            prayerName,
            minutes.toInt(),
        )

        else -> stringResource(R.string.home_countdown_soon, prayerName)
    }

    Surface(
        color = NextPrayerCardTokens.background,
        contentColor = NextPrayerCardTokens.foreground,
        shape = RoundedCornerShape(Dimens.RadiusLg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(NextPrayerCardTokens.padding)) {
            NextBadge()
            Spacer(Modifier.height(Dimens.Space3))
            Text(
                text = prayerName,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = next.time.toDisplayString(),
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(Dimens.Space2))
            Text(
                text = formatCountdown(next.remainingSeconds),
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = countdownDescription
                },
            )
            Spacer(Modifier.height(Dimens.Space4))
            Text(
                text = stringResource(
                    R.string.home_location_date,
                    location,
                    IndonesianDates.full(date),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = HijriDateFormatter.format(date),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun PrayerRowItem(
    slot: PrayerSlot,
    isNext: Boolean,
    isCurrent: Boolean,
    isPast: Boolean,
) {
    val highlighted = isNext || isCurrent
    val background = if (highlighted) {
        PrayerRowTokens.backgroundNext
    } else {
        PrayerRowTokens.background
    }
    val foreground = when {
        highlighted -> PrayerRowTokens.foregroundNext
        isPast -> PrayerRowTokens.foregroundPast
        else -> PrayerRowTokens.foreground
    }
    val nameStyle = MaterialTheme.typography.titleMedium.let { style ->
        if (highlighted) style.copy(fontWeight = FontWeight.SemiBold) else style
    }
    val timeStyle = MaterialTheme.typography.bodyLarge.copy(
        fontFeatureSettings = "tnum",
        fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Medium,
    )

    Surface(
        color = background,
        contentColor = foreground,
        shape = RoundedCornerShape(Dimens.RadiusMd),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = PrayerRowTokens.height),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = PrayerRowTokens.padding,
                vertical = Dimens.Space2,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(slot.prayer.labelRes),
                style = nameStyle,
                modifier = Modifier.weight(1f),
            )
            if (isNext) {
                NextBadge()
                Spacer(Modifier.width(Dimens.Space2))
            }
            Text(
                text = slot.time.toDisplayString(),
                style = timeStyle,
            )
        }
    }
}

@Composable
private fun ScheduleUnavailableCard() {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(Dimens.RadiusLg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.home_schedule_unavailable),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(Dimens.Space6),
        )
    }
}
