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
import com.robifr.ledger.data.model.Info;
import com.robifr.ledger.data.model.Model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InfoUpdater {
  private InfoUpdater() {}

  @NonNull
  public static <M extends Model, I extends Info> List<I> addInfo(
      @NonNull List<M> models,
      @NonNull List<I> oldInfo,
      @NonNull Function<M, I> modelToInfoConverter) {
    Objects.requireNonNull(models);
    Objects.requireNonNull(modelToInfoConverter);
    Objects.requireNonNull(oldInfo);

    final ArrayList<I> info = new ArrayList<>(oldInfo);
    final List<I> newInfo = models.stream().map(modelToInfoConverter).collect(Collectors.toList());

    info.addAll(newInfo);
    return info;
  }

  @NonNull
  public static <M extends Model, I extends Info> List<I> updateInfo(
      @NonNull List<M> models,
      @NonNull List<I> oldInfo,
      @NonNull Function<M, I> modelToInfoConverter) {
    Objects.requireNonNull(models);
    Objects.requireNonNull(modelToInfoConverter);
    Objects.requireNonNull(oldInfo);

    final ArrayList<I> info = new ArrayList<>(oldInfo);
    final List<I> newInfo = models.stream().map(modelToInfoConverter).collect(Collectors.toList());
    final HashMap<Long, I> filteredInfo = new HashMap<>();

    info.forEach(i -> filteredInfo.put(i.modelId(), i));
    newInfo.forEach(i -> filteredInfo.put(i.modelId(), i)); // Override duplicate ID.
    return new ArrayList<>(filteredInfo.values());
  }

  @NonNull
  public static <M extends Model, I extends Info> List<I> deleteInfo(
      @NonNull List<M> models,
      @NonNull List<I> oldInfo,
      @NonNull Function<M, I> modelToInfoConverter) {
    Objects.requireNonNull(models);
    Objects.requireNonNull(modelToInfoConverter);
    Objects.requireNonNull(oldInfo);

    final ArrayList<I> info = new ArrayList<>(oldInfo);

    for (M m : models) {
      for (int i = info.size(); i-- > 0; ) {
        if (info.get(i).modelId() != null && info.get(i).modelId().equals(m.modelId())) {
          info.remove(i);
          break;
        }
      }
    }

    return info;
  }
}
