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
import androidx.room.TypeConverters;
import androidx.room.Update;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.local.ColumnConverter.InstantConverter;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Dao
public abstract class QueueDao implements QueryAccessible<QueueModel> {
  /**
   * @noinspection NullableProblems
   */
  @Override
  @Insert
  public abstract long insert(@NonNull QueueModel queue);

  /**
   * @noinspection NullableProblems
   */
  @Override
  @Update
  public abstract int update(@NonNull QueueModel queue);

  /**
   * @noinspection NullableProblems
   */
  @Override
  @Delete
  public abstract int delete(@NonNull QueueModel queue);

  /**
   * @noinspection NullableProblems
   */
  @Override
  @NonNull
  @Query("SELECT * FROM queue")
  public abstract List<QueueModel> selectAll();

  @Override
  @Nullable
  @Query("SELECT * FROM queue WHERE id = :queueId")
  public abstract QueueModel selectById(@Nullable Long queueId);

  /**
   * @noinspection NullableProblems
   */
  @Override
  @NonNull
  @Transaction
  public List<QueueModel> selectById(@NonNull List<Long> queueIds) {
    Objects.requireNonNull(queueIds);

    return queueIds.stream().map(this::selectById).collect(Collectors.toList());
  }

  @Override
  @Nullable
  @Query("SELECT * FROM queue WHERE rowid = :rowId")
  public abstract QueueModel selectByRowId(long rowId);

  @Override
  @Query("SELECT id FROM queue WHERE rowid = :rowId")
  public abstract long selectIdByRowId(long rowId);

  @Override
  @Query("SELECT rowid FROM queue WHERE id = :queueId")
  public abstract long selectRowIdById(@Nullable Long queueId);

  @Override
  @Query("SELECT EXISTS(SELECT id FROM queue WHERE id = :queueId)")
  public abstract boolean isExistsById(@Nullable Long queueId);

  /**
   * @noinspection NullableProblems
   */
  @NonNull
  @Query("SELECT * FROM queue WHERE date >= :startDate AND date <= :endDate")
  @Transaction
  @TypeConverters(InstantConverter.class)
  public abstract List<QueueModel> selectAllInRange(
      @NonNull Instant startDate, @NonNull Instant endDate);
}
