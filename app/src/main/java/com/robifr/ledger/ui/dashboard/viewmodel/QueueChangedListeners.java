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

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import com.robifr.ledger.data.ModelUpdater;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.repository.ModelChangedListener;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

class QueueChangedListeners implements ModelChangedListener<QueueModel> {
  @NonNull private final DashboardViewModel _viewModel;

  public QueueChangedListeners(@NonNull DashboardViewModel viewModel) {
    this._viewModel = Objects.requireNonNull(viewModel);
  }

  @Override
  @WorkerThread
  public void onModelAdded(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    new Handler(Looper.getMainLooper())
        .post(() -> this._updateQueues(queues, ModelUpdater::addModel));
  }

  @Override
  @WorkerThread
  public void onModelUpdated(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    new Handler(Looper.getMainLooper())
        .post(() -> this._updateQueues(queues, ModelUpdater::updateModel));
  }

  @Override
  @WorkerThread
  public void onModelDeleted(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    new Handler(Looper.getMainLooper())
        .post(() -> this._updateQueues(queues, ModelUpdater::deleteModel));
  }

  @Override
  @WorkerThread
  public void onModelUpserted(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    new Handler(Looper.getMainLooper())
        .post(() -> this._updateQueues(queues, ModelUpdater::upsertModel));
  }

  private void _updateQueues(
      @NonNull List<QueueModel> queues,
      @NonNull BiFunction<List<QueueModel>, List<QueueModel>, List<QueueModel>> updater) {
    Objects.requireNonNull(queues);
    Objects.requireNonNull(updater);

    final List<QueueModel> filteredQueues =
        updater.apply(this._viewModel._queues().getValue(), queues);

    filteredQueues.removeIf(
        info ->
            info.date().isBefore(this._viewModel.date().getValue().dateStart().toInstant())
                || info.date().isAfter(this._viewModel.date().getValue().dateEnd().toInstant()));
    this._viewModel._onQueuesChanged(filteredQueues);
  }
}
