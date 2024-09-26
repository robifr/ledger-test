/**
 * Copyright 2024 Robi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.robifr.ledger.ui.dashboard.viewmodel;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import com.robifr.ledger.assetbinding.chart.ChartBinding;
import com.robifr.ledger.assetbinding.chart.ChartData;
import com.robifr.ledger.assetbinding.chart.ChartUtil;
import com.robifr.ledger.data.display.QueueDate;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.data.model.ProductOrderModel;
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
  private final SafeMutableLiveData<UncompletedQueuesChartModel> _uncompletedQueuesChartModel =
      new SafeMutableLiveData<>(new UncompletedQueuesChartModel(List.of(), List.of(), null));

  @NonNull
  private final SafeMediatorLiveData<Integer> _totalUncompletedQueues =
      new SafeMediatorLiveData<>(0);

  /** Map of the most active customers with their appearance counts. */
  @NonNull
  private final SafeMutableLiveData<Map<CustomerModel, Integer>> _mostActiveCustomers =
      new SafeMutableLiveData<>(Map.of());

  @NonNull
  private final SafeMediatorLiveData<Integer> _totalActiveCustomers = new SafeMediatorLiveData<>(0);

  /** Map of the most products sold with their quantity counts. */
  @NonNull
  private final SafeMutableLiveData<Map<ProductModel, BigDecimal>> _mostProductsSold =
      new SafeMutableLiveData<>(Map.of());

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
                    queues.stream()
                        .map(QueueModel::customerId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .count()));
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
  public SafeLiveData<UncompletedQueuesChartModel> uncompletedQueuesChartModel() {
    return this._uncompletedQueuesChartModel;
  }

  @NonNull
  public SafeLiveData<Integer> totalUncompletedQueues() {
    return this._totalUncompletedQueues;
  }

  /**
   * @see #_mostActiveCustomers
   */
  @NonNull
  public SafeLiveData<Map<CustomerModel, Integer>> mostActiveCustomers() {
    return this._mostActiveCustomers;
  }

  @NonNull
  public SafeLiveData<Integer> totalActiveCustomers() {
    return this._totalActiveCustomers;
  }

  /**
   * @see #_mostProductsSold
   */
  @NonNull
  public SafeLiveData<Map<ProductModel, BigDecimal>> mostProductsSold() {
    return this._mostProductsSold;
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

  public void onDisplayUncompletedQueuesChart() {
    final Map<Integer, Integer> rawDataSummed = new LinkedHashMap<>();
    // The order is important to ensure that `UncompletedQueuesChartModel#colors` matches.
    rawDataSummed.put(QueueModel.Status.IN_QUEUE.resourceString(), 0);
    rawDataSummed.put(QueueModel.Status.IN_PROCESS.resourceString(), 0);
    rawDataSummed.put(QueueModel.Status.UNPAID.resourceString(), 0);

    ZonedDateTime oldestDate = null;

    for (QueueModel queue : this._viewModel._queues().getValue()) {
      // Merge the data if the status is uncompleted.
      if (rawDataSummed.containsKey(queue.status().resourceString())) {
        rawDataSummed.merge(queue.status().resourceString(), 1, Integer::sum);

        if (oldestDate == null || queue.date().compareTo(oldestDate.toInstant()) < 0) {
          oldestDate = queue.date().atZone(ZoneId.systemDefault());
        }
      }
    }

    final List<ChartData.Single<Integer, Integer>> formattedData = new ArrayList<>();

    for (Map.Entry<Integer, Integer> rawData : rawDataSummed.entrySet()) {
      formattedData.add(new ChartData.Single<>(rawData.getKey(), rawData.getValue()));
    }

    this._uncompletedQueuesChartModel.setValue(
        new UncompletedQueuesChartModel(
            formattedData,
            List.of(
                QueueModel.Status.IN_QUEUE.resourceBackgroundColor(),
                QueueModel.Status.IN_PROCESS.resourceBackgroundColor(),
                QueueModel.Status.UNPAID.resourceBackgroundColor()),
            oldestDate));
  }

  public void onDisplayMostActiveCustomers() {
    this._mostActiveCustomers.setValue(
        this._viewModel._queues().getValue().stream()
            .filter(queue -> queue.customer() != null)
            .collect(Collectors.groupingBy(QueueModel::customer, Collectors.counting()))
            .entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(4)
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().intValue(),
                    (a, b) -> a,
                    LinkedHashMap::new)));
  }

  public void onDisplayMostProductsSold() {
    this._mostProductsSold.setValue(
        this._viewModel._queues().getValue().stream()
            .flatMap(queue -> queue.productOrders().stream())
            .filter(order -> order.referencedProduct() != null)
            .collect(
                Collectors.toMap(
                    ProductOrderModel::referencedProduct,
                    order -> BigDecimal.valueOf(order.quantity()),
                    BigDecimal::add))
            .entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(4)
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new)));
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

  /**
   * @param data {@link ChartData.Single} contains:
   *     <ul>
   *       <li>{@link ChartData.Single#key() key} - The {@link QueueModel.Status#resourceString()
   *           resourceString} in {@link QueueModel.Status QueueModel.Status}.
   *       <li>{@link ChartData.Single#value() value} - The total count of queues with the
   *           corresponding status.
   *     </ul>
   *
   * @param colors The {@link QueueModel.Status#resourceBackgroundColor() resourceBackgroundColor}
   *     in {@link QueueModel.Status QueueModel.Status}.
   * @param oldestDate Oldest date for ranged queue to be shown in the center of donut chart.
   * @see ChartBinding#renderDonutChart
   */
  public static record UncompletedQueuesChartModel(
      @NonNull List<ChartData.Single<Integer, Integer>> data,
      @NonNull @ColorRes List<Integer> colors,
      @Nullable ZonedDateTime oldestDate) {
    public UncompletedQueuesChartModel {
      Objects.requireNonNull(data);
      Objects.requireNonNull(colors);
    }
  }
}
