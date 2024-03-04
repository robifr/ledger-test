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
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.local.ColumnConverter.FtsStringConverter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Dao
public abstract class ProductDao implements QueryAccessible<ProductModel> {
  /**
   * @noinspection NullableProblems
   */
  @Override
  @Transaction
  public long insert(@NonNull ProductModel product) {
    Objects.requireNonNull(product);

    final long rowId = this._insert(product);

    this._insertFts(rowId, FtsStringConverter.toFtsSpacedString(product.name()));
    return rowId;
  }

  /**
   * @noinspection NullableProblems
   */
  @Override
  @Transaction
  public int update(@NonNull ProductModel product) {
    Objects.requireNonNull(product);

    final long rowId = this.selectRowIdById(product.id());
    this._deleteFts(rowId);

    final int effectedRow = this._update(product);

    this._insertFts(rowId, FtsStringConverter.toFtsSpacedString(product.name()));
    return effectedRow;
  }

  /**
   * @noinspection NullableProblems
   */
  @Override
  @Transaction
  public int delete(@NonNull ProductModel product) {
    Objects.requireNonNull(product);

    this._deleteFts(this.selectRowIdById(product.id()));
    return this._delete(product);
  }

  /**
   * @noinspection NullableProblems
   */
  @Override
  @NonNull
  @Query("SELECT * FROM product")
  public abstract List<ProductModel> selectAll();

  @Override
  @Nullable
  @Query("SELECT * FROM product WHERE id = :productId")
  public abstract ProductModel selectById(@Nullable Long productId);

  /**
   * @noinspection NullableProblems
   */
  @Override
  @NonNull
  @Transaction
  public List<ProductModel> selectById(@NonNull List<Long> productIds) {
    Objects.requireNonNull(productIds);

    return productIds.stream().map(this::selectById).collect(Collectors.toList());
  }

  @Override
  @Nullable
  @Query("SELECT * FROM product WHERE rowid = :rowId")
  public abstract ProductModel selectByRowId(long rowId);

  @Override
  @Query("SELECT id FROM product WHERE rowid = :rowId")
  public abstract long selectIdByRowId(long rowId);

  @Override
  @Query("SELECT rowid FROM product WHERE id = :productId")
  public abstract long selectRowIdById(@Nullable Long productId);

  @Override
  @Query("SELECT EXISTS(SELECT id FROM product WHERE id = :productId)")
  public abstract boolean isExistsById(@Nullable Long productId);

  /**
   * @noinspection NullableProblems
   */
  @NonNull
  @Transaction
  public List<ProductModel> search(@NonNull String query) {
    Objects.requireNonNull(query);

    final String escapedQuery = query.replaceAll("\"", "\"\"");
    return this._search("*\"" + FtsStringConverter.toFtsSpacedString(escapedQuery) + "\"*");
  }

  /**
   * @noinspection NullableProblems
   */
  @Insert
  protected abstract long _insert(@NonNull ProductModel product);

  /**
   * @noinspection NullableProblems
   */
  @Update
  protected abstract int _update(@NonNull ProductModel product);

  /**
   * @noinspection NullableProblems
   */
  @Delete
  protected abstract int _delete(@NonNull ProductModel product);

  /**
   * @noinspection NullableProblems
   */
  @NonNull
  @Query(
      """
        SELECT * FROM product
        /**
        * Use where-in clause because we don't want the data get override from the FTS field,
        * since the string field is spaced.
        */
        WHERE product.rowid IN (
          SELECT product_fts.rowid FROM product_fts
          WHERE product_fts MATCH :query
        )
        ORDER BY product.name
      """)
  protected abstract List<ProductModel> _search(@NonNull String query);

  /**
   * Delete product virtual row from FTS table. It should be used before updating or deleting
   * product from the actual table.
   */
  @Query("DELETE FROM product_fts WHERE docid = :rowId")
  protected abstract void _deleteFts(long rowId);

  /**
   * Insert product virtual row into FTS table. It should be used after updating or inserting
   * product from the actual table.
   *
   * @return Inserted row ID.
   * @noinspection NullableProblems
   */
  @Query("INSERT INTO product_fts(docid, name) VALUES (:rowId, :productName)")
  protected abstract long _insertFts(long rowId, @NonNull String productName);
}
