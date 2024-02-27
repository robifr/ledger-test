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

package com.robifr.ledger.local.access;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.data.model.Model;
import com.robifr.ledger.data.model.QueueModel;
import java.util.List;

/**
 * @implNote Every method with ID as a parameter are using an object instead of its primitive type.
 *     So that we can easily query a foreign-key (nullable). Like {@link QueueModel#customerId()}.
 */
interface QueryAccessible<M extends Model> {
  /**
   * @return Inserted row ID. -1 for a failed operation.
   * @noinspection NullableProblems
   */
  public long insert(@NonNull M model);

  /**
   * @return Number of row effected. 0 for a failed operation.
   * @noinspection NullableProblems
   */
  public int update(@NonNull M model);

  /**
   * @return Number of row effected. 0 for a failed operation.
   * @noinspection NullableProblems
   */
  public int delete(@NonNull M model);

  /**
   * @noinspection NullableProblems
   */
  @NonNull
  public List<M> selectAll();

  @Nullable
  public M selectById(@Nullable Long id);

  /**
   * @noinspection NullableProblems
   */
  @NonNull
  public List<M> selectById(@NonNull List<Long> ids);

  @Nullable
  public M selectByRowId(long rowId);

  public long selectIdByRowId(long rowId);

  public long selectRowIdById(@Nullable Long id);

  public boolean isExistsById(@Nullable Long id);
}
