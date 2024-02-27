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
