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
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.room.Upsert;
import com.robifr.ledger.data.model.ProductOrderModel;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Dao
public abstract class ProductOrderDao implements QueryAccessible<ProductOrderModel> {
  /**
   * @noinspection NullableProblems
   */
  @Override
  @Insert
  public abstract long insert(@NonNull ProductOrderModel productOrder);

  /**
   * @return List of inserted row ID. -1 for a failed operation.
   * @noinspection NullableProblems
   */
  @NonNull
  @Insert
  public abstract List<Long> insert(@NonNull List<ProductOrderModel> productOrders);

  /**
   * @noinspection NullableProblems
   */
  @Override
  @Update
  public abstract int update(@NonNull ProductOrderModel productOrder);

  /**
   * @return Number of row effected.
   * @noinspection NullableProblems
   */
  @Update
  public abstract int update(@NonNull List<ProductOrderModel> productOrders);

  /**
   * @noinspection NullableProblems
   */
  @Override
  @Delete
  public abstract int delete(@NonNull ProductOrderModel productOrder);

  /**
   * @return Number of row effected.
   * @noinspection NullableProblems
   */
  @Delete
  public abstract int delete(@NonNull List<ProductOrderModel> productOrders);

  /**
   * @noinspection NullableProblems
   */
  @Override
  @NonNull
  @Query("SELECT * FROM product_order")
  public abstract List<ProductOrderModel> selectAll();

  @Override
  @Nullable
  @Query("SELECT * FROM product_order WHERE id = :productOrderId")
  public abstract ProductOrderModel selectById(@Nullable Long productOrderId);

  /**
   * @noinspection NullableProblems
   */
  @Override
  @NonNull
  @Transaction
  public List<ProductOrderModel> selectById(@NonNull List<Long> productOrderIds) {
    Objects.requireNonNull(productOrderIds);

    return productOrderIds.stream().map(this::selectById).collect(Collectors.toList());
  }

  @Override
  @Nullable
  @Query("SELECT * FROM product_order WHERE rowid = :rowId")
  public abstract ProductOrderModel selectByRowId(long rowId);

  /**
   * @noinspection NullableProblems
   */
  @NonNull
  @Transaction
  public List<ProductOrderModel> selectByRowId(@NonNull List<Long> rowIds) {
    Objects.requireNonNull(rowIds);

    return rowIds.stream().map(this::selectByRowId).collect(Collectors.toList());
  }

  @Override
  @Query("SELECT id FROM product_order WHERE rowid = :rowId")
  public abstract long selectIdByRowId(long rowId);

  /**
   * @noinspection NullableProblems
   */
  @NonNull
  @Transaction
  public List<Long> selectIdByRowId(@NonNull List<Long> rowIds) {
    Objects.requireNonNull(rowIds);

    return rowIds.stream().map(this::selectIdByRowId).collect(Collectors.toList());
  }

  @Override
  @Query("SELECT rowid FROM product_order WHERE id = :productOrderId")
  public abstract long selectRowIdById(@Nullable Long productOrderId);

  @Override
  @Query("SELECT EXISTS(SELECT id FROM product_order WHERE id = :productOrderId)")
  public abstract boolean isExistsById(@Nullable Long productOrderId);

  /**
   * @return Upserted row ID. -1 for a failed operation.
   * @noinspection NullableProblems
   */
  @Upsert
  public abstract long upsert(@NonNull ProductOrderModel productOrder);

  /**
   * @return Upserted row IDs. -1 for a failed operation.
   * @noinspection NullableProblems
   */
  @NonNull
  @Upsert
  public abstract List<Long> upsert(@NonNull List<ProductOrderModel> productOrders);

  /**
   * @noinspection NullableProblems
   */
  @NonNull
  @Query("SELECT * FROM product_order WHERE queue_id = :queueId")
  public abstract List<ProductOrderModel> selectAllByQueueId(@Nullable Long queueId);
}
