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

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import com.robifr.ledger.assetbinding.chart.ChartBinding;
import com.robifr.ledger.assetbinding.chart.ChartData;
import com.robifr.ledger.assetbinding.chart.ChartUtil;
import com.robifr.ledger.data.display.QueueDate;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.ui.dashboard.DashboardSummary;
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
import java.util.stream.Collectors;

public class DashboardSummaryViewModel {
  @NonNull private final DashboardViewModel _viewModel;

  @NonNull
  private final SafeMutableLiveData<DashboardSummary.OverviewType> _displayedChart =
      new SafeMutableLiveData<>(DashboardSummary.OverviewType.TOTAL_QUEUES);

  @NonNull
  private final SafeMutableLiveData<TotalQueuesChartModel> _totalQueuesChartModel =
      new SafeMutableLiveData<>(new TotalQueuesChartModel(List.of(), List.of(), List.of()));

  @NonNull private final SafeMediatorLiveData<Integer> _totalQueues = new SafeMediatorLiveData<>(0);

  @NonNull
  private final SafeMediatorLiveData<Integer> _totalUncompletedQueues =
      new SafeMediatorLiveData<>(0);

  @NonNull
  private final SafeMediatorLiveData<Integer> _totalActiveCustomers = new SafeMediatorLiveData<>(0);

  @NonNull
  private final SafeMediatorLiveData<BigDecimal> _totalProductsSold =
      new SafeMediatorLiveData<>(BigDecimal.ZERO);

  public DashboardSummaryViewModel(@NonNull DashboardViewModel viewModel) {
    this._viewModel = Objects.requireNonNull(viewModel);

    this._totalQueues.addSource(
        this._viewModel._queues().toLiveData(),
        queues -> this._totalQueues.setValue(queues.size()));
    this._totalUncompletedQueues.addSource(
        this._viewModel._queues().toLiveData(),
        queues ->
            this._totalUncompletedQueues.setValue(
                (int)
                    queues.stream()
                        .filter(queue -> queue.status() != QueueModel.Status.COMPLETED)
                        .count()));
    this._totalActiveCustomers.addSource(
        this._viewModel._queues().toLiveData(),
        queues ->
            this._totalActiveCustomers.setValue(
                (int)
                    queues.stream().map(QueueModel::customerId).filter(Objects::nonNull).count()));
    this._totalProductsSold.addSource(
        this._viewModel._queues().toLiveData(),
        queues ->
            this._totalProductsSold.setValue(
                queues.stream()
                    .flatMap(queue -> queue.productOrders().stream())
                    .map(productOrder -> BigDecimal.valueOf(productOrder.quantity()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)));
  }

  @NonNull
  public SafeLiveData<DashboardSummary.OverviewType> displayedChart() {
    return this._displayedChart;
  }

  @NonNull
  public SafeLiveData<TotalQueuesChartModel> totalQueuesChartModel() {
    return this._totalQueuesChartModel;
  }

  @NonNull
  public SafeLiveData<Integer> totalQueues() {
    return this._totalQueues;
  }

  @NonNull
  public SafeLiveData<Integer> totalUncompletedQueues() {
    return this._totalUncompletedQueues;
  }

  @NonNull
  public SafeLiveData<Integer> totalActiveCustomers() {
    return this._totalActiveCustomers;
  }

  @NonNull
  public SafeLiveData<BigDecimal> totalProductsSold() {
    return this._totalProductsSold;
  }

  public void onDisplayedChartChanged(@NonNull DashboardSummary.OverviewType overviewType) {
    Objects.requireNonNull(overviewType);

    this._displayedChart.setValue(overviewType);
  }

  public void onDisplayTotalQueuesChart() {
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

    final Map<String, Integer> rawDataSummed = new LinkedHashMap<>();
    final int yAxisTicks = 6; // Defined in `createLinearScale()`. It includes zero.
    int maxValue = yAxisTicks - 1;

    // Sum the values if the date is equal. The queues also have to be sorted by date
    // because D3.js draws everything in order.
    for (QueueModel queue :
        this._viewModel._queues().getValue().stream()
            .sorted(Comparator.comparing(QueueModel::date))
            .collect(Collectors.toList())) {
      final int data =
          rawDataSummed.merge(
              ChartUtil.toDateTime(
                  queue.date().atZone(ZoneId.systemDefault()), new Pair<>(startDate, endDate)),
              1,
              Integer::sum);
      maxValue = Math.max(maxValue, data);
    }

    final List<ChartData.Single<String, Integer>> formattedData = new ArrayList<>();

    for (var rawData : rawDataSummed.entrySet()) {
      formattedData.add(new ChartData.Single<>(rawData.getKey(), rawData.getValue()));
    }

    this._totalQueuesChartModel.setValue(
        new TotalQueuesChartModel(
            ChartUtil.toDateTimeDomain(new Pair<>(startDate, endDate)),
            List.of(0.0, ChartUtil.calculateNiceScale(0.0, maxValue, yAxisTicks)[1]),
            formattedData));
  }

  /**
   * @see ChartBinding#renderBarChart
   */
  public static record TotalQueuesChartModel(
      @NonNull List<String> xAxisDomain,
      @NonNull List<Double> yAxisDomain,
      @NonNull List<ChartData.Single<String, Integer>> data) {
    public TotalQueuesChartModel {
      Objects.requireNonNull(xAxisDomain);
      Objects.requireNonNull(yAxisDomain);
      Objects.requireNonNull(data);
    }
  }
}
