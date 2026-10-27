package id.haikal.waktusholat.ui.monthly

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import id.haikal.waktusholat.R
import id.haikal.waktusholat.domain.model.Prayer
import id.haikal.waktusholat.ui.common.IndonesianDates
import id.haikal.waktusholat.ui.common.LocationRequiredCard
import id.haikal.waktusholat.ui.common.labelRes
import id.haikal.waktusholat.ui.common.toDisplayString
import id.haikal.waktusholat.ui.theme.Dimens
import id.haikal.waktusholat.ui.theme.MonthlyTableTokens
import java.time.YearMonth

@Composable
fun MonthlyScreen(
    modifier: Modifier = Modifier,
    onOpenCitySearch: () -> Unit = {},
) {
    val viewModel: MonthlyViewModel = viewModel(factory = MonthlyViewModel.Factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        MonthSwitcher(
            month = state.month,
            onPrevious = viewModel::previousMonth,
            onNext = viewModel::nextMonth,
        )
        when {
            state.loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            !state.hasLocation -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimens.Space4),
                    contentAlignment = Alignment.Center,
                ) {
                    LocationRequiredCard(
                        title = stringResource(R.string.home_no_location_title),
                        body = stringResource(R.string.home_no_location_body),
                        actionLabel = stringResource(R.string.home_pick_location),
                        onAction = onOpenCitySearch,
                    )
                }
            }

            else -> {
                MonthlyTable(state = state)
            }
        }
    }
}

@Composable
private fun MonthSwitcher(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Space2, vertical = Dimens.Space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.monthly_prev),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = IndonesianDates.monthYear(month),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.monthly_next),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MonthlyTable(state: MonthlyUiState) {
    val horizontalScroll = rememberScrollState()

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(Dimens.RadiusLg),
        modifier = Modifier
            .fillMaxSize()
            .padding(start = Dimens.Space4, end = Dimens.Space4, bottom = Dimens.Space4),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScroll),
        ) {
            TableHeaderRow()
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = Dimens.Space1),
                verticalArrangement = Arrangement.Top,
            ) {
                items(state.rows, key = { it.date }) { row ->
                    DayRow(row = row, isToday = row.date == state.today)
                }
            }
        }
    }
}

@Composable
private fun TableHeaderRow() {
    Row(
        modifier = Modifier
            .padding(horizontal = Dimens.Space1)
            .height(MonthlyTableTokens.headerHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.monthly_date_column),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(MonthlyTableTokens.dateColumnWidth),
        )
        Prayer.entries.forEach { prayer ->
            Text(
                text = stringResource(prayer.labelRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.width(MonthlyTableTokens.timeColumnWidth),
            )
        }
    }
}

@Composable
private fun DayRow(row: MonthlyRow, isToday: Boolean) {
    val background = if (isToday) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val foreground = if (isToday) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val timeStyle = MaterialTheme.typography.bodySmall.copy(
        fontFeatureSettings = "tnum",
        fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal,
    )

    Row(
        modifier = Modifier
            .background(background)
            .padding(horizontal = Dimens.Space1)
            .height(MonthlyTableTokens.rowHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${IndonesianDates.weekday(row.date)} ${row.date.dayOfMonth}",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = foreground,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.width(MonthlyTableTokens.dateColumnWidth),
        )
        Prayer.entries.forEach { prayer ->
            Text(
                text = row.times[prayer]?.toDisplayString() ?: "—",
                style = timeStyle,
                color = foreground,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.width(MonthlyTableTokens.timeColumnWidth),
            )
        }
    }
}
