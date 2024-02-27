/**
 * Copyright (c) 2022-present Robi
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

package com.robifr.ledger.repository;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import com.robifr.ledger.data.model.Model;
import java.util.List;

public interface ModelChangedListener<M extends Model> {
  @WorkerThread
  public void onModelAdded(@NonNull List<M> models);

  @WorkerThread
  public void onModelUpdated(@NonNull List<M> models);

  @WorkerThread
  public void onModelDeleted(@NonNull List<M> models);

  @WorkerThread
  public void onModelUpserted(@NonNull List<M> models);

  public sealed interface Source<M extends Model>
      permits CustomerRepository, ProductOrderRepository, ProductRepository, QueueRepository {
    public void addModelChangedListener(@NonNull ModelChangedListener<M> listener);

    public void removeModelChangedListener(@NonNull ModelChangedListener<M> listener);

    public void notifyModelAdded(@NonNull List<M> models);

    public void notifyModelUpdated(@NonNull List<M> models);

    public void notifyModelDeleted(@NonNull List<M> models);

    public void notifyModelUpserted(@NonNull List<M> models);
  }
}
