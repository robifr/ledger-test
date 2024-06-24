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

package com.robifr.ledger.ui.product.viewmodel;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.robifr.ledger.data.ModelUpdater;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.repository.ModelChangedListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

class ProductChangedListener implements ModelChangedListener<ProductModel> {
  private final ProductViewModel _viewModel;

  public ProductChangedListener(@NonNull ProductViewModel viewModel) {
    this._viewModel = Objects.requireNonNull(viewModel);
  }

  @Override
  public void onModelAdded(@NonNull List<ProductModel> products) {
    Objects.requireNonNull(products);

    new Handler(Looper.getMainLooper())
        .post(() -> this._updateProducts(products, ModelUpdater::addModel));
  }

  @Override
  public void onModelUpdated(@NonNull List<ProductModel> products) {
    Objects.requireNonNull(products);

    new Handler(Looper.getMainLooper())
        .post(() -> this._updateProducts(products, ModelUpdater::updateModel));
  }

  @Override
  public void onModelDeleted(@NonNull List<ProductModel> products) {
    Objects.requireNonNull(products);

    new Handler(Looper.getMainLooper())
        .post(() -> this._updateProducts(products, ModelUpdater::deleteModel));
  }

  @Override
  public void onModelUpserted(@NonNull List<ProductModel> products) {
    Objects.requireNonNull(products);

    new Handler(Looper.getMainLooper())
        .post(() -> this._updateProducts(products, ModelUpdater::upsertModel));
  }

  private void _updateProducts(
      @NonNull List<ProductModel> products,
      @NonNull BiFunction<List<ProductModel>, List<ProductModel>, List<ProductModel>> updater) {
    Objects.requireNonNull(products);
    Objects.requireNonNull(updater);

    final ArrayList<ProductModel> currentProducts =
        this._viewModel.products().getValue() != null
            ? new ArrayList<>(this._viewModel.products().getValue())
            : new ArrayList<>();
    this._viewModel
        .filterView()
        .onFiltersChanged(
            this._viewModel.filterView().inputtedFilters(),
            updater.apply(currentProducts, products));
  }
}
