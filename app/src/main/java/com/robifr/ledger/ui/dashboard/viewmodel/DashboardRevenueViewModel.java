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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.robifr.ledger.assetbinding.chart.ChartUtil;
import com.robifr.ledger.data.display.QueueDate;
import com.robifr.ledger.data.model.ProductOrderModel;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.data.model.QueueWithProductOrdersInfo;
import com.robifr.ledger.ui.dashboard.DashboardRevenue;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DashboardRevenueViewModel {
  @NonNull private final DashboardViewModel _viewModel;

  @NonNull
  private final MutableLiveData<DashboardRevenue.OverviewType> _displayedChart =
      new MutableLiveData<>();

  @NonNull
  private final MutableLiveData<DashboardRevenue.ChartModel> _chartModel = new MutableLiveData<>();

  public DashboardRevenueViewModel(
      @NonNull DashboardViewModel viewModel,
      @NonNull LiveData<List<QueueWithProductOrdersInfo>> queuesWithProductOrders) {
    Objects.requireNonNull(queuesWithProductOrders);

    this._viewModel = Objects.requireNonNull(viewModel);
  }

  @NonNull
  public LiveData<DashboardRevenue.OverviewType> displayedChart() {
    return this._displayedChart;
  }

  @NonNull
  public LiveData<DashboardRevenue.ChartModel> chartModel() {
    return this._chartModel;
  }

  public void onDisplayedChartChanged(@NonNull DashboardRevenue.OverviewType overviewType) {
    Objects.requireNonNull(overviewType);

    this._displayedChart.setValue(overviewType);
  }

  public void onDisplayReceivedIncomeChart() {
    final QueueDate date = this._viewModel.date().getValue();
    final List<QueueWithProductOrdersInfo> queueInfo =
        this._viewModel.queuesWithProductOrders().getValue();
    if (date == null || queueInfo == null) return;

    final Map<ZonedDateTime, BigDecimal> unformattedQueueDateWithTotalPrice = new LinkedHashMap<>();

    for (QueueWithProductOrdersInfo queue : queueInfo) {
      // Received income are from the completed queues.
      if (queue.status() != QueueModel.Status.COMPLETED) continue;

      for (ProductOrderModel productOrder : queue.productOrders()) {
        unformattedQueueDateWithTotalPrice.merge(
            queue.date().atZone(ZoneId.systemDefault()),
            productOrder.totalPrice(),
            BigDecimal::add);
      }
    }

    final ZonedDateTime startDate =
        date.range() == QueueDate.Range.ALL_TIME
            // Remove unnecessary dates.
            ? queueInfo.stream()
                .map(QueueWithProductOrdersInfo::date)
                .min(Instant::compareTo)
                .orElse(date.dateStart().toInstant())
                .atZone(ZoneId.systemDefault())
            : date.dateStart();
    final Map<String, BigDecimal> queueDateWithTotalPrice =
        ChartUtil.toDateTimeData(
            unformattedQueueDateWithTotalPrice, new Pair<>(startDate, date.dateEnd()));

    final List<String> xAxisDomain = new ArrayList<>(queueDateWithTotalPrice.keySet());
    // Convert to percent because D3.js can't handle big decimal.
    final List<String> yAxisDomain = ChartUtil.toPercentageLinearDomain(queueDateWithTotalPrice);

    this._chartModel.setValue(
        new DashboardRevenue.ChartModel(
            xAxisDomain,
            yAxisDomain,
            ChartUtil.toPercentageData(queueDateWithTotalPrice, LinkedHashMap::new)));
  }

  public void onDisplayProjectedIncomeChart() {
    final QueueDate date = this._viewModel.date().getValue();
    final List<QueueWithProductOrdersInfo> queueInfo =
        this._viewModel.queuesWithProductOrders().getValue();
    if (date == null || queueInfo == null) return;

    final Map<ZonedDateTime, BigDecimal> unformattedQueueDateWithTotalPrice = new LinkedHashMap<>();

    for (QueueWithProductOrdersInfo queue : queueInfo) {
      for (ProductOrderModel productOrder : queue.productOrders()) {
        unformattedQueueDateWithTotalPrice.merge(
            queue.date().atZone(ZoneId.systemDefault()),
            productOrder.totalPrice(),
            BigDecimal::add);
      }
    }

    final ZonedDateTime startDate =
        date.range() == QueueDate.Range.ALL_TIME
            // Remove unnecessary dates.
            ? queueInfo.stream()
                .map(QueueWithProductOrdersInfo::date)
                .min(Instant::compareTo)
                .orElse(date.dateStart().toInstant())
                .atZone(ZoneId.systemDefault())
            : date.dateStart();
    final Map<String, BigDecimal> queueDateWithTotalPrice =
        ChartUtil.toDateTimeData(
            unformattedQueueDateWithTotalPrice, new Pair<>(startDate, date.dateEnd()));

    final List<String> xAxisDomain = new ArrayList<>(queueDateWithTotalPrice.keySet());
    // Convert to percent because D3.js can't handle big decimal.
    final List<String> yAxisDomain = ChartUtil.toPercentageLinearDomain(queueDateWithTotalPrice);

    this._chartModel.setValue(
        new DashboardRevenue.ChartModel(
            xAxisDomain,
            yAxisDomain,
            ChartUtil.toPercentageData(queueDateWithTotalPrice, LinkedHashMap::new)));
  }

  public void onDisplayTotalQueueChart() {
    final QueueDate date = this._viewModel.date().getValue();
    final List<QueueWithProductOrdersInfo> queueInfo =
        this._viewModel.queuesWithProductOrders().getValue();
    if (date == null || queueInfo == null) return;

    final Map<ZonedDateTime, BigDecimal> unformattedQueueDateWithTotalQueue = new LinkedHashMap<>();

    for (QueueWithProductOrdersInfo queue : queueInfo) {
      unformattedQueueDateWithTotalQueue.merge(
          queue.date().atZone(ZoneId.systemDefault()), BigDecimal.ONE, BigDecimal::add);
    }

    final ZonedDateTime startDate =
        date.range() == QueueDate.Range.ALL_TIME
            // Remove unnecessary dates.
            ? queueInfo.stream()
                .map(QueueWithProductOrdersInfo::date)
                .min(Instant::compareTo)
                .orElse(date.dateStart().toInstant())
                .atZone(ZoneId.systemDefault())
            : date.dateStart();
    final Map<String, BigDecimal> queueDateWithTotalQueue =
        ChartUtil.toDateTimeData(
            unformattedQueueDateWithTotalQueue, new Pair<>(startDate, date.dateEnd()));

    final List<String> xAxisDomain = new ArrayList<>(queueDateWithTotalQueue.keySet());
    // Convert to percent because D3.js can't handle big decimal.
    final List<String> yAxisDomain = ChartUtil.toPercentageLinearDomain(queueDateWithTotalQueue);

    this._chartModel.setValue(
        new DashboardRevenue.ChartModel(
            xAxisDomain,
            yAxisDomain,
            ChartUtil.toPercentageData(queueDateWithTotalQueue, LinkedHashMap::new)));
  }

  public void onDisplayProductsSoldChart() {
    final QueueDate date = this._viewModel.date().getValue();
    final List<QueueWithProductOrdersInfo> queueInfo =
        this._viewModel.queuesWithProductOrders().getValue();
    if (date == null || queueInfo == null) return;

    final Map<ZonedDateTime, BigDecimal> unformattedQueueDateWithTotalProducts =
        new LinkedHashMap<>();

    for (QueueWithProductOrdersInfo queue : queueInfo) {
      for (ProductOrderModel productOrder : queue.productOrders()) {
        unformattedQueueDateWithTotalProducts.merge(
            queue.date().atZone(ZoneId.systemDefault()),
            BigDecimal.valueOf(productOrder.quantity()),
            BigDecimal::add);
      }
    }

    final ZonedDateTime startDate =
        date.range() == QueueDate.Range.ALL_TIME
            // Remove unnecessary dates.
            ? queueInfo.stream()
                .map(QueueWithProductOrdersInfo::date)
                .min(Instant::compareTo)
                .orElse(date.dateStart().toInstant())
                .atZone(ZoneId.systemDefault())
            : date.dateStart();
    final Map<String, BigDecimal> queueDateWithTotalProducts =
        ChartUtil.toDateTimeData(
            unformattedQueueDateWithTotalProducts, new Pair<>(startDate, date.dateEnd()));

    final List<String> xAxisDomain = new ArrayList<>(queueDateWithTotalProducts.keySet());
    // Convert to percent because D3.js can't handle big decimal.
    final List<String> yAxisDomain = ChartUtil.toPercentageLinearDomain(queueDateWithTotalProducts);

    this._chartModel.setValue(
        new DashboardRevenue.ChartModel(
            xAxisDomain,
            yAxisDomain,
            ChartUtil.toPercentageData(queueDateWithTotalProducts, LinkedHashMap::new)));
  }
}
