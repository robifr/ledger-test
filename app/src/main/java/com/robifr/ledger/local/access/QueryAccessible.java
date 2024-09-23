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

package com.robifr.ledger.local.access;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.data.model.Model;
import com.robifr.ledger.data.model.QueueModel;
import java.util.List;

/**
 * @implNote Every method with ID as a parameter are using an object instead of its primitive type.
 *     So that we can easily query a foreign-key (nullable). Like {@link QueueModel#customerId()}.
 * @noinspection EmptyMethod
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
