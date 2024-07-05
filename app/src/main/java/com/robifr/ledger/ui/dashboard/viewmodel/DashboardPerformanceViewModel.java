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
import com.robifr.ledger.data.display.QueueDate;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMediatorLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

public class DashboardPerformanceViewModel {
  @NonNull private final DashboardViewModel _viewModel;

  @NonNull private final SafeMediatorLiveData<Integer> _totalQueue = new SafeMediatorLiveData<>(0);

  @NonNull
  private final SafeMutableLiveData<BigDecimal> _totalQueueAverage =
      new SafeMutableLiveData<>(BigDecimal.ZERO);

  @NonNull
  private final SafeMediatorLiveData<Integer> _totalActiveCustomers = new SafeMediatorLiveData<>(0);

  @NonNull
  private final SafeMutableLiveData<BigDecimal> _totalActiveCustomersAverage =
      new SafeMutableLiveData<>(BigDecimal.ZERO);

  @NonNull
  private final SafeMediatorLiveData<BigDecimal> _totalProductsSold =
      new SafeMediatorLiveData<>(BigDecimal.ZERO);

  @NonNull
  private final SafeMutableLiveData<BigDecimal> _totalProductsSoldAverage =
      new SafeMutableLiveData<>(BigDecimal.ZERO);

  public DashboardPerformanceViewModel(@NonNull DashboardViewModel viewModel) {
    this._viewModel = Objects.requireNonNull(viewModel);

    this._totalQueue.addSource(this._viewModel._queues().toLiveData(), this::_onTotalQueueChanged);
    this._totalActiveCustomers.addSource(
        this._viewModel._queues().toLiveData(), this::_onTotalActiveCustomersChanged);
    this._totalProductsSold.addSource(
        this._viewModel._queues().toLiveData(), this::_onTotalProductsSoldChanged);
  }

  @NonNull
  public SafeLiveData<Integer> totalQueue() {
    return this._totalQueue;
  }

  @NonNull
  public SafeLiveData<BigDecimal> totalQueueAverage() {
    return this._totalQueueAverage;
  }

  @NonNull
  public SafeLiveData<Integer> totalActiveCustomers() {
    return this._totalActiveCustomers;
  }

  @NonNull
  public SafeLiveData<BigDecimal> totalActiveCustomersAverage() {
    return this._totalActiveCustomersAverage;
  }

  @NonNull
  public SafeLiveData<BigDecimal> totalProductsSold() {
    return this._totalProductsSold;
  }

  @NonNull
  public SafeLiveData<BigDecimal> totalProductsSoldAverage() {
    return this._totalProductsSoldAverage;
  }

  private void _onTotalQueueChanged(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    final QueueDate date = this._viewModel.date().getValue();
    final ZonedDateTime startDate =
        date.range() == QueueDate.Range.ALL_TIME
            // Remove unnecessary dates.
            ? queues.stream()
                .map(QueueModel::date)
                .min(Instant::compareTo)
                .orElse(date.dateStart().toInstant())
                .atZone(ZoneId.systemDefault())
            : date.dateStart();
    final long totalDays = ChronoUnit.DAYS.between(startDate, date.dateEnd()) + 1L;

    final int totalQueue = queues.size();
    final BigDecimal average =
        BigDecimal.valueOf(totalQueue)
            .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP);

    this._totalQueue.setValue(totalQueue);
    this._totalQueueAverage.setValue(average);
  }

  private void _onTotalActiveCustomersChanged(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    final QueueDate date = this._viewModel.date().getValue();
    final ZonedDateTime startDate =
        date.range() == QueueDate.Range.ALL_TIME
            // Remove unnecessary dates.
            ? queues.stream()
                .map(QueueModel::date)
                .min(Instant::compareTo)
                .orElse(date.dateStart().toInstant())
                .atZone(ZoneId.systemDefault())
            : date.dateStart();
    final long totalDays = ChronoUnit.DAYS.between(startDate, date.dateEnd()) + 1L;

    final int totalActiveCustomers =
        (int) queues.stream().map(QueueModel::customerId).filter(Objects::nonNull).count();
    final BigDecimal average =
        BigDecimal.valueOf(totalActiveCustomers)
            .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP);

    this._totalActiveCustomers.setValue(totalActiveCustomers);
    this._totalActiveCustomersAverage.setValue(average);
  }

  private void _onTotalProductsSoldChanged(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    final QueueDate date = this._viewModel.date().getValue();
    final ZonedDateTime startDate =
        date.range() == QueueDate.Range.ALL_TIME
            // Remove unnecessary dates.
            ? queues.stream()
                .map(QueueModel::date)
                .min(Instant::compareTo)
                .orElse(date.dateStart().toInstant())
                .atZone(ZoneId.systemDefault())
            : date.dateStart();
    final long totalDays = ChronoUnit.DAYS.between(startDate, date.dateEnd()) + 1L;

    final BigDecimal totalProductsSold =
        queues.stream()
            .flatMap(queue -> queue.productOrders().stream())
            .map(productOrder -> BigDecimal.valueOf(productOrder.quantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    final BigDecimal average =
        totalProductsSold.divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP);

    this._totalProductsSold.setValue(totalProductsSold);
    this._totalProductsSoldAverage.setValue(average);
  }
}
