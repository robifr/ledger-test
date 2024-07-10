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
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.ui.dashboard.DashboardSummary;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMediatorLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import java.math.BigDecimal;
import java.util.Objects;

public class DashboardSummaryViewModel {
  @NonNull private final DashboardViewModel _viewModel;

  @NonNull
  private final SafeMutableLiveData<DashboardSummary.OverviewType> _displayedChart =
      new SafeMutableLiveData<>(DashboardSummary.OverviewType.TOTAL_QUEUES);

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
}
