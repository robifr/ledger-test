/**
 * Copyright (c) 2024 Robi
 *
 * Ledger is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ledger is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ledger. If not, see <https://www.gnu.org/licenses/>.
 */

package com.robifr.ledger.ui.dashboard.viewmodel;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import com.robifr.ledger.assetbinding.chart.ChartBinding;
import com.robifr.ledger.assetbinding.chart.ChartData;
import com.robifr.ledger.assetbinding.chart.ChartUtil;
import com.robifr.ledger.data.display.QueueDate;
import com.robifr.ledger.data.model.ProductOrderModel;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.ui.dashboard.DashboardRevenue;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMediatorLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DashboardRevenueViewModel {
  @NonNull private final DashboardViewModel _viewModel;

  @NonNull
  private final SafeMutableLiveData<DashboardRevenue.OverviewType> _displayedChart =
      new SafeMutableLiveData<>(DashboardRevenue.OverviewType.RECEIVED_INCOME);

  @NonNull
  private final SafeMutableLiveData<IncomeChartModel> _chartModel =
      new SafeMutableLiveData<>(
          new IncomeChartModel(List.of(), List.of(), List.of(), List.of(), Set.of()));

  @NonNull
  private final SafeMediatorLiveData<BigDecimal> _receivedIncome =
      new SafeMediatorLiveData<>(BigDecimal.ZERO);

  @NonNull
  private final SafeMediatorLiveData<BigDecimal> _projectedIncome =
      new SafeMediatorLiveData<>(BigDecimal.ZERO);

  public DashboardRevenueViewModel(@NonNull DashboardViewModel viewModel) {
    this._viewModel = Objects.requireNonNull(viewModel);

    this._receivedIncome.addSource(
        this._viewModel._queues().toLiveData(),
        queues ->
            this._receivedIncome.setValue(
                queues.stream()
                    // Received income are from the completed queues.
                    .filter(queue -> queue.status() == QueueModel.Status.COMPLETED)
                    .flatMap(queue -> queue.productOrders().stream())
                    .map(ProductOrderModel::totalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)));
    this._projectedIncome.addSource(
        this._viewModel._queues().toLiveData(),
        queues ->
            this._projectedIncome.setValue(
                queues.stream()
                    .flatMap(queue -> queue.productOrders().stream())
                    .map(ProductOrderModel::totalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)));
  }

  @NonNull
  public SafeLiveData<DashboardRevenue.OverviewType> displayedChart() {
    return this._displayedChart;
  }

  @NonNull
  public SafeLiveData<IncomeChartModel> chartModel() {
    return this._chartModel;
  }

  @NonNull
  public SafeLiveData<BigDecimal> receivedIncome() {
    return this._receivedIncome;
  }

  @NonNull
  public SafeLiveData<BigDecimal> projectedIncome() {
    return this._projectedIncome;
  }

  public void onDisplayedChartChanged(@NonNull DashboardRevenue.OverviewType overviewType) {
    Objects.requireNonNull(overviewType);

    this._displayedChart.setValue(overviewType);
  }

  public void onDisplayReceivedIncomeChart() {
    this._onDisplayChart(
        List.of(
            DashboardRevenue.OverviewType.PROJECTED_INCOME.unselectedResourceColor(),
            DashboardRevenue.OverviewType.RECEIVED_INCOME.selectedResourceColor()));
  }

  public void onDisplayProjectedIncomeChart() {
    this._onDisplayChart(
        List.of(
            DashboardRevenue.OverviewType.PROJECTED_INCOME.selectedResourceColor(),
            DashboardRevenue.OverviewType.RECEIVED_INCOME.unselectedResourceColor()));
  }

  private void _onDisplayChart(@NonNull @ColorRes List<Integer> colors) {
    Objects.requireNonNull(colors);

    final ZonedDateTime startDate =
        this._viewModel.date().getValue().range() == QueueDate.Range.ALL_TIME
            // Remove unnecessary dates.
            ? this._viewModel._queues().getValue().stream()
                .map(QueueModel::date)
                .min(Instant::compareTo)
                .orElse(this._viewModel.date().getValue().dateStart().toInstant())
                .atZone(ZoneId.systemDefault())
            : this._viewModel.date().getValue().dateStart();
    final ZonedDateTime endDate = this._viewModel.date().getValue().dateEnd();

    final Map<Pair<String, DashboardRevenue.OverviewType>, BigDecimal> rawDataSummed =
        new LinkedHashMap<>();
    final int yAxisTicks = 6; // Defined in `createPercentageLinearScale()`. It includes zero.
    BigDecimal maxValue = BigDecimal.valueOf(yAxisTicks - 1);

    // Sum the values if the date and overview type are equal.
    // The queues also have to be sorted by date because D3.js draws everything in order.
    for (QueueModel queue :
        this._viewModel._queues().getValue().stream()
            .sorted(Comparator.comparing(QueueModel::date))
            .collect(Collectors.toList())) {
      final String formattedDate =
          ChartUtil.toDateTime(
              queue.date().atZone(ZoneId.systemDefault()), new Pair<>(startDate, endDate));

      // Received income are from the completed queue only.
      if (queue.status() == QueueModel.Status.COMPLETED) {
        final BigDecimal receivedIncomeData =
            rawDataSummed.merge(
                new Pair<>(formattedDate, DashboardRevenue.OverviewType.RECEIVED_INCOME),
                queue.grandTotalPrice(),
                BigDecimal::add);
        maxValue = maxValue.max(receivedIncomeData);
      }

      final BigDecimal projectedIncomeData =
          rawDataSummed.merge(
              new Pair<>(formattedDate, DashboardRevenue.OverviewType.PROJECTED_INCOME),
              queue.grandTotalPrice(),
              BigDecimal::add);
      maxValue = maxValue.max(projectedIncomeData);
    }

    final List<ChartData.Multiple<String, Double, String>> formattedData = new ArrayList<>();

    for (var rawData : rawDataSummed.entrySet()) {
      formattedData.add(
          new ChartData.Multiple<>(
              rawData.getKey().first,
              // Convert to percent because D3.js can't handle big decimal.
              ChartUtil.toPercentage(rawData.getValue(), maxValue, yAxisTicks),
              rawData.getKey().second.toString()));
    }

    this._chartModel.setValue(
        new IncomeChartModel(
            // Both domains must be the same as the formatted ones.
            ChartUtil.toDateTimeDomain(new Pair<>(startDate, endDate)),
            ChartUtil.toPercentageLinearDomain(maxValue, yAxisTicks),
            formattedData,
            colors,
            Set.of(
                DashboardRevenue.OverviewType.PROJECTED_INCOME,
                DashboardRevenue.OverviewType.RECEIVED_INCOME)));
  }

  /**
   * @see ChartBinding#renderStackedBarChart
   */
  public static record IncomeChartModel(
      @NonNull List<String> xAxisDomain,
      @NonNull List<String> yAxisDomain,
      @NonNull List<ChartData.Multiple<String, Double, String>> data,
      @NonNull @ColorRes List<Integer> colors,
      @NonNull Set<DashboardRevenue.OverviewType> groupInOrder) {
    public IncomeChartModel {
      Objects.requireNonNull(xAxisDomain);
      Objects.requireNonNull(yAxisDomain);
      Objects.requireNonNull(data);
      Objects.requireNonNull(colors);
      Objects.requireNonNull(groupInOrder);
    }
  }
}
