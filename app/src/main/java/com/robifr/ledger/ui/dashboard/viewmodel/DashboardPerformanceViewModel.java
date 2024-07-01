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
import com.robifr.ledger.data.model.QueueModel;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class DashboardPerformanceViewModel {
  @NonNull private final MediatorLiveData<Integer> _totalQueue = new MediatorLiveData<>();
  @NonNull private final MediatorLiveData<Integer> _totalActiveCustomers = new MediatorLiveData<>();
  @NonNull private final MediatorLiveData<BigDecimal> _totalProductsSold = new MediatorLiveData<>();

  public DashboardPerformanceViewModel(@NonNull LiveData<List<QueueModel>> queuesLiveData) {
    Objects.requireNonNull(queuesLiveData);

    this._totalQueue.addSource(
        queuesLiveData,
        queues -> {
          if (queues != null) this._totalQueue.setValue(queues.size());
        });
    this._totalActiveCustomers.addSource(
        queuesLiveData,
        queues -> {
          if (queues != null) {
            this._totalActiveCustomers.setValue(
                (int) queues.stream().map(QueueModel::customerId).filter(Objects::nonNull).count());
          }
        });
    this._totalProductsSold.addSource(
        queuesLiveData,
        queues -> {
          if (queues != null) {
            this._totalProductsSold.setValue(
                queues.stream()
                    .flatMap(queue -> queue.productOrders().stream())
                    .map(productOrder -> BigDecimal.valueOf(productOrder.quantity()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
          }
        });
  }

  @NonNull
  public LiveData<Integer> totalQueue() {
    return this._totalQueue;
  }

  @NonNull
  public LiveData<Integer> totalActiveCustomers() {
    return this._totalActiveCustomers;
  }

  @NonNull
  public LiveData<BigDecimal> totalProductsSold() {
    return this._totalProductsSold;
  }
}
