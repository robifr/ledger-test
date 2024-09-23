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

package com.robifr.ledger.ui.queue.viewmodel;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.robifr.ledger.data.ModelUpdater;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.repository.ModelChangedListener;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

class QueueChangedListener implements ModelChangedListener<QueueModel> {
  private final QueueViewModel _viewModel;

  public QueueChangedListener(@NonNull QueueViewModel viewModel) {
    this._viewModel = Objects.requireNonNull(viewModel);
  }

  @Override
  public void onModelAdded(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    new Handler(Looper.getMainLooper())
        .post(() -> this._updateQueues(queues, ModelUpdater::addModel));
  }

  @Override
  public void onModelUpdated(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    new Handler(Looper.getMainLooper())
        .post(() -> this._updateQueues(queues, ModelUpdater::updateModel));
  }

  @Override
  public void onModelDeleted(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    new Handler(Looper.getMainLooper())
        .post(() -> this._updateQueues(queues, ModelUpdater::deleteModel));
  }

  @Override
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

    this._viewModel
        .filterView()
        .onFiltersChanged(
            this._viewModel.filterView().inputtedFilters(),
            updater.apply(this._viewModel.queues().getValue(), queues));
  }
}
