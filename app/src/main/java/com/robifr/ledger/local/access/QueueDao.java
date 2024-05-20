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

package com.robifr.ledger.local.access;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import com.robifr.ledger.data.model.QueueModel;
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
}
