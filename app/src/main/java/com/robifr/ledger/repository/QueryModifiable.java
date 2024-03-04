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
import com.robifr.ledger.data.model.Model;
import java.util.concurrent.CompletableFuture;

interface QueryModifiable<M extends Model> {
  /**
   * @return Inserted model ID. 0 for a failed operation.
   */
  @NonNull
  public CompletableFuture<Long> add(@NonNull M model);

  /**
   * @return Number of row effected. 0 for a failed operation.
   */
  @NonNull
  public CompletableFuture<Integer> update(@NonNull M model);

  /**
   * @return Number of row effected. 0 for a failed operation.
   */
  @NonNull
  public CompletableFuture<Integer> delete(@NonNull M model);
}
