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

package com.robifr.ledger.ui;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.MutableLiveData;
import com.robifr.ledger.data.model.Model;
import com.robifr.ledger.repository.ModelChangedListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class LiveDataModelUpdater<M extends Model> implements ModelChangedListener<M> {
  @NonNull protected final MutableLiveData<List<M>> _liveData;

  public LiveDataModelUpdater(@NonNull MutableLiveData<List<M>> liveData) {
    this._liveData = Objects.requireNonNull(liveData);
  }

  /**
   * Invoked when the underlying data models change, be it addition, update, deletion, or upsert.
   * Override to update live data with the combined list of both changed and current models.
   *
   * @param combinedModels Combined list of models, including those affected by changes from the
   *     database and the existing ones from the live data itself.
   */
  @MainThread
  public abstract void onUpdateLiveData(@NonNull List<M> combinedModels);

  @Override
  @WorkerThread
  public void onModelAdded(@NonNull List<M> models) {
    Objects.requireNonNull(models);

    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              final ArrayList<M> currentModels =
                  this._liveData.getValue() != null
                      ? new ArrayList<>(this._liveData.getValue())
                      : new ArrayList<>();
              currentModels.addAll(models);

              this.onUpdateLiveData(currentModels);
            });
  }

  @Override
  @WorkerThread
  public void onModelUpdated(@NonNull List<M> models) {
    Objects.requireNonNull(models);

    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              final ArrayList<M> currentModels =
                  this._liveData.getValue() != null
                      ? new ArrayList<>(this._liveData.getValue())
                      : new ArrayList<>();

              for (M model : models) {
                for (int i = 0; i < currentModels.size(); i++) {
                  final M currentModel = currentModels.get(i);

                  if (currentModel.modelId() != null
                      && currentModel.modelId().equals(model.modelId())) {
                    currentModels.set(i, model);
                    break;
                  }
                }
              }

              this.onUpdateLiveData(currentModels);
            });
  }

  @Override
  @WorkerThread
  public void onModelDeleted(@NonNull List<M> models) {
    Objects.requireNonNull(models);

    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              final ArrayList<M> currentModels =
                  this._liveData.getValue() != null
                      ? new ArrayList<>(this._liveData.getValue())
                      : new ArrayList<>();

              for (M model : models) {
                for (int i = 0; i < currentModels.size(); i++) {
                  final M currentModel = currentModels.get(i);

                  if (currentModel.modelId() != null
                      && currentModel.modelId().equals(model.modelId())) {
                    currentModels.remove(i);
                    break;
                  }
                }
              }

              this.onUpdateLiveData(currentModels);
            });
  }

  @Override
  @WorkerThread
  public void onModelUpserted(@NonNull List<M> models) {
    Objects.requireNonNull(models);

    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              final ArrayList<M> currentModels =
                  this._liveData.getValue() != null
                      ? new ArrayList<>(this._liveData.getValue())
                      : new ArrayList<>();

              for (M model : models) {
                for (int i = 0; i < currentModels.size(); i++) {
                  final M currentModel = currentModels.get(i);

                  // Update when having the same ID.
                  if (currentModel.modelId() != null
                      && currentModel.modelId().equals(model.modelId())) {
                    currentModels.set(i, model);
                    break;
                  }

                  // Add as new when reached the end of array
                  // while can't find model with the same ID.
                  if (i == currentModels.size() - 1) currentModels.add(model);
                }
              }

              this.onUpdateLiveData(currentModels);
            });
  }
}
