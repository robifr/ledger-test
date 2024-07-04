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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import com.robifr.ledger.data.display.QueueDate;
import com.robifr.ledger.data.model.QueueModel;
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

  @NonNull private final MediatorLiveData<Integer> _totalQueue = new MediatorLiveData<>();
  @NonNull private final MutableLiveData<BigDecimal> _totalQueueAverage = new MutableLiveData<>();
  @NonNull private final MediatorLiveData<Integer> _totalActiveCustomers = new MediatorLiveData<>();

  @NonNull
  private final MutableLiveData<BigDecimal> _totalActiveCustomersAverage = new MutableLiveData<>();

  @NonNull private final MediatorLiveData<BigDecimal> _totalProductsSold = new MediatorLiveData<>();

  @NonNull
  private final MutableLiveData<BigDecimal> _totalProductsSoldAverage = new MutableLiveData<>();

  public DashboardPerformanceViewModel(@NonNull DashboardViewModel viewModel) {
    this._viewModel = Objects.requireNonNull(viewModel);

    this._totalQueue.addSource(
        this._viewModel._queues(),
        queues -> {
          if (queues != null) this._onTotalQueueChanged(queues);
        });
    this._totalActiveCustomers.addSource(
        this._viewModel._queues(),
        queues -> {
          if (queues != null) this._onTotalActiveCustomersChanged(queues);
        });
    this._totalProductsSold.addSource(
        this._viewModel._queues(),
        queues -> {
          if (queues != null) this._onTotalProductsSoldChanged(queues);
        });
  }

  @NonNull
  public LiveData<Integer> totalQueue() {
    return this._totalQueue;
  }

  @NonNull
  public LiveData<BigDecimal> totalQueueAverage() {
    return this._totalQueueAverage;
  }

  @NonNull
  public LiveData<Integer> totalActiveCustomers() {
    return this._totalActiveCustomers;
  }

  @NonNull
  public LiveData<BigDecimal> totalActiveCustomersAverage() {
    return this._totalActiveCustomersAverage;
  }

  @NonNull
  public LiveData<BigDecimal> totalProductsSold() {
    return this._totalProductsSold;
  }

  @NonNull
  public LiveData<BigDecimal> totalProductsSoldAverage() {
    return this._totalProductsSoldAverage;
  }

  private void _onTotalQueueChanged(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    final QueueDate date = this._viewModel.date().getValue();
    if (date == null) return;

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
    if (date == null) return;

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
    if (date == null) return;

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
