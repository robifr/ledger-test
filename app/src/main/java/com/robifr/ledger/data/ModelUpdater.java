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

package com.robifr.ledger.data;

import androidx.annotation.NonNull;
import com.robifr.ledger.data.model.Model;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ModelUpdater {
  private ModelUpdater() {}

  @NonNull
  public static <M extends Model> List<M> addModel(
      @NonNull List<M> oldModels, @NonNull List<M> newModels) {
    Objects.requireNonNull(newModels);
    Objects.requireNonNull(oldModels);

    final ArrayList<M> models = new ArrayList<>(oldModels);
    models.addAll(newModels);
    return models;
  }

  @NonNull
  public static <M extends Model> List<M> updateModel(
      @NonNull List<M> oldModels, @NonNull List<M> newModels) {
    Objects.requireNonNull(newModels);
    Objects.requireNonNull(oldModels);

    final ArrayList<M> models = new ArrayList<>(oldModels);

    for (M updatedModel : newModels) {
      for (int i = 0; i < models.size(); i++) {
        final M currentModel = models.get(i);

        if (currentModel.modelId() != null
            && currentModel.modelId().equals(updatedModel.modelId())) {
          models.set(i, updatedModel);
          break;
        }
      }
    }

    return models;
  }

  @NonNull
  public static <M extends Model> List<M> deleteModel(
      @NonNull List<M> oldModels, @NonNull List<M> newModels) {
    Objects.requireNonNull(newModels);
    Objects.requireNonNull(oldModels);

    final ArrayList<M> models = new ArrayList<>(oldModels);

    for (M updatedModel : newModels) {
      for (int i = 0; i < models.size(); i++) {
        final M currentModel = models.get(i);

        if (currentModel.modelId() != null
            && currentModel.modelId().equals(updatedModel.modelId())) {
          models.remove(i);
          break;
        }
      }
    }

    return models;
  }

  @NonNull
  public static <M extends Model> List<M> upsertModel(
      @NonNull List<M> oldModels, @NonNull List<M> newModels) {
    Objects.requireNonNull(newModels);
    Objects.requireNonNull(oldModels);

    final ArrayList<M> models = new ArrayList<>(oldModels);

    for (M updatedModels : newModels) {
      for (int i = 0; i < models.size(); i++) {
        final M currentModel = models.get(i);

        // Update when having the same ID.
        if (currentModel.modelId() != null
            && currentModel.modelId().equals(updatedModels.modelId())) {
          models.set(i, updatedModels);
          break;
        }

        // Add as new when reached the end of array
        // while can't find model with the same ID.
        if (i == models.size() - 1) models.add(updatedModels);
      }
    }

    return models;
  }
}
