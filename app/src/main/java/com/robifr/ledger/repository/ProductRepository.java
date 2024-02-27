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

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.local.LocalDatabase;
import com.robifr.ledger.local.access.ProductDao;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class ProductRepository
    implements ModelChangedListener.Source<ProductModel>,
        QueryReadable<ProductModel>,
        QueryModifiable<ProductModel> {
  @Nullable private static ProductRepository _instance;
  @NonNull private final ProductDao _localDao;

  @NonNull
  private final HashSet<ModelChangedListener<ProductModel>> _modelChangedListeners =
      new HashSet<>();

  private ProductRepository(@NonNull Context context) {
    Objects.requireNonNull(context);

    this._localDao = LocalDatabase.instance(context.getApplicationContext()).productDao();
  }

  @NonNull
  public static synchronized ProductRepository instance(@NonNull Context context) {
    Objects.requireNonNull(context);

    return ProductRepository._instance =
        ProductRepository._instance == null
            ? new ProductRepository(context)
            : ProductRepository._instance;
  }

  @Override
  public void addModelChangedListener(@NonNull ModelChangedListener<ProductModel> listener) {
    Objects.requireNonNull(listener);

    this._modelChangedListeners.add(listener);
  }

  @Override
  public void removeModelChangedListener(@NonNull ModelChangedListener<ProductModel> listener) {
    Objects.requireNonNull(listener);

    this._modelChangedListeners.remove(listener);
  }

  @Override
  public void notifyModelAdded(@NonNull List<ProductModel> products) {
    Objects.requireNonNull(products);

    this._modelChangedListeners.forEach(listener -> listener.onModelAdded(products));
  }

  @Override
  public void notifyModelUpdated(@NonNull List<ProductModel> products) {
    Objects.requireNonNull(products);

    this._modelChangedListeners.forEach(listener -> listener.onModelUpdated(products));
  }

  @Override
  public void notifyModelDeleted(@NonNull List<ProductModel> products) {
    Objects.requireNonNull(products);

    this._modelChangedListeners.forEach(listener -> listener.onModelDeleted(products));
  }

  @Override
  public void notifyModelUpserted(@NonNull List<ProductModel> products) {
    Objects.requireNonNull(products);

    this._modelChangedListeners.forEach(listener -> listener.onModelUpserted(products));
  }

  @Override
  @NonNull
  public CompletableFuture<List<ProductModel>> selectAll() {
    return CompletableFuture.supplyAsync(this._localDao::selectAll);
  }

  @Override
  @NonNull
  public CompletableFuture<ProductModel> selectById(@Nullable Long id) {
    return CompletableFuture.supplyAsync(() -> this._localDao.selectById(id));
  }

  @Override
  @NonNull
  public CompletableFuture<List<ProductModel>> selectById(@NonNull List<Long> ids) {
    Objects.requireNonNull(ids);

    return CompletableFuture.supplyAsync(() -> this._localDao.selectById(ids));
  }

  @Override
  @NonNull
  public CompletableFuture<Boolean> isExistsById(@Nullable Long id) {
    return CompletableFuture.supplyAsync(() -> this._localDao.isExistsById(id));
  }

  @Override
  @NonNull
  public CompletableFuture<Long> add(@NonNull ProductModel product) {
    Objects.requireNonNull(product);

    final CompletableFuture<Long> insert =
        CompletableFuture.supplyAsync(() -> this._localDao.insert(product));

    return insert.thenComposeAsync(
        rowId -> {
          final CompletableFuture<Long> selectId =
              CompletableFuture.supplyAsync(() -> this._localDao.selectIdByRowId(rowId));

          selectId
              .thenComposeAsync(this::selectById)
              .thenAcceptAsync(
                  insertedProduct -> {
                    if (insertedProduct != null) this.notifyModelAdded(List.of(insertedProduct));
                  });
          return selectId;
        });
  }

  @Override
  @NonNull
  public CompletableFuture<Integer> update(@NonNull ProductModel product) {
    Objects.requireNonNull(product);

    final CompletableFuture<Integer> update =
        CompletableFuture.supplyAsync(() -> this._localDao.update(product));

    update.thenAcceptAsync(
        effected -> {
          if (effected == 0) return;

          this.selectById(product.id())
              .thenAcceptAsync(
                  updatedProduct -> {
                    if (updatedProduct != null) this.notifyModelUpdated(List.of(updatedProduct));
                  });
        });
    return update;
  }

  @Override
  @NonNull
  public CompletableFuture<Integer> delete(@NonNull ProductModel product) {
    Objects.requireNonNull(product);

    // Note: product ID on product order table will automatically set
    //    to null upon product deletion.
    return this.selectById(product.id())
        .thenComposeAsync(
            productToDelete -> {
              if (productToDelete == null) return CompletableFuture.completedFuture(0);

              final CompletableFuture<Integer> delete =
                  CompletableFuture.supplyAsync(() -> this._localDao.delete(productToDelete));

              delete.thenAcceptAsync(
                  effected -> {
                    if (effected > 0) this.notifyModelDeleted(List.of(productToDelete));
                  });
              return delete;
            });
  }

  @NonNull
  public CompletableFuture<List<ProductModel>> search(@NonNull String query) {
    Objects.requireNonNull(query);

    return CompletableFuture.supplyAsync(() -> this._localDao.search(query));
  }
}
