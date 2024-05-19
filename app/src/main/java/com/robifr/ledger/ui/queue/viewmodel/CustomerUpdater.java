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

package com.robifr.ledger.ui.queue.viewmodel;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.repository.ModelChangedListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class CustomerUpdater implements ModelChangedListener<CustomerModel> {
  @NonNull private final QueueViewModel _viewModel;

  public CustomerUpdater(@NonNull QueueViewModel viewModel) {
    this._viewModel = Objects.requireNonNull(viewModel);
  }

  @Override
  @WorkerThread
  public void onModelAdded(@NonNull List<CustomerModel> customers) {}

  @Override
  @WorkerThread
  public void onModelUpdated(@NonNull List<CustomerModel> customers) {
    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              final ArrayList<QueueModel> queues =
                  this._viewModel.queues().getValue() != null
                      ? new ArrayList<>(this._viewModel.queues().getValue())
                      : new ArrayList<>();

              for (CustomerModel customer : customers) {
                for (int i = 0; i < queues.size(); i++) {
                  final QueueModel queue = queues.get(i);

                  // When customer updated, apply those changes into the queue model.
                  if (queue.customerId() != null && queue.customerId().equals(customer.id())) {
                    final QueueModel updatedQueue =
                        QueueModel.toBuilder(queue)
                            .setCustomerId(customer.id())
                            .setCustomer(customer)
                            .build();
                    queues.set(i, updatedQueue);
                  }
                }
              }

              this._viewModel
                  .filterView()
                  .onFiltersChanged(this._viewModel.filterView().inputtedFilters(), queues);
            });
  }

  @Override
  @WorkerThread
  public void onModelDeleted(@NonNull List<CustomerModel> customers) {
    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              final ArrayList<QueueModel> queues =
                  this._viewModel.queues().getValue() != null
                      ? new ArrayList<>(this._viewModel.queues().getValue())
                      : new ArrayList<>();

              for (CustomerModel customer : customers) {
                for (int i = 0; i < queues.size(); i++) {
                  final QueueModel queue = queues.get(i);

                  // When customer deleted, remove them from the queue model.
                  if (queue.customerId() != null && queue.customerId().equals(customer.id())) {
                    final QueueModel updatedQueue =
                        QueueModel.toBuilder(queue).setCustomerId(null).setCustomer(null).build();
                    queues.set(i, updatedQueue);
                  }
                }
              }

              this._viewModel
                  .filterView()
                  .onFiltersChanged(this._viewModel.filterView().inputtedFilters(), queues);
            });
  }

  @Override
  @WorkerThread
  public void onModelUpserted(@NonNull List<CustomerModel> customers) {
    // Only when customer updated, apply those changes into the queue model
    // and ignore for any inserted customer.
    this.onModelUpdated(customers);
  }
}
