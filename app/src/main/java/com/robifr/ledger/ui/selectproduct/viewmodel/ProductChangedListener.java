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

package com.robifr.ledger.ui.selectproduct.viewmodel;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.robifr.ledger.data.ModelUpdater;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.repository.ModelChangedListener;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

class ProductChangedListener implements ModelChangedListener<ProductModel> {
  private final SelectProductViewModel _viewModel;

  public ProductChangedListener(@NonNull SelectProductViewModel viewModel) {
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

    this._viewModel._onProductsChanged(
        updater.apply(this._viewModel.products().getValue(), products));
  }
}
