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

package com.robifr.ledger.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.data.model.Model;
import com.robifr.ledger.data.model.QueueModel;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @implNote Every method with ID as a parameter should be using an object instead of its primitive
 *     type. So that we can easily query a foreign-key (nullable). Like when querying {@link
 *     QueueModel#customerId()}.
 */
interface QueryReadable<M extends Model> {
  @NonNull
  public CompletableFuture<List<M>> selectAll();

  @NonNull
  public CompletableFuture<M> selectById(@Nullable Long id);

  @NonNull
  public CompletableFuture<List<M>> selectById(@NonNull List<Long> ids);

  @NonNull
  public CompletableFuture<Boolean> isExistsById(@Nullable Long id);
}
