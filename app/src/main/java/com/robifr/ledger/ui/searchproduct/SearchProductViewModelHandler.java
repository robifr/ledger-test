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

package com.robifr.ledger.ui.searchproduct;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.ui.searchproduct.viewmodel.SearchProductViewModel;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SearchProductViewModelHandler {
  @NonNull private final SearchProductFragment _fragment;
  @NonNull private final SearchProductViewModel _viewModel;

  public SearchProductViewModelHandler(
      @NonNull SearchProductFragment fragment, @NonNull SearchProductViewModel viewModel) {
    this._fragment = Objects.requireNonNull(fragment);
    this._viewModel = Objects.requireNonNull(viewModel);

    this._viewModel
        .resultSelectedProductId()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onResultSelectedProductId));
    this._viewModel.products().observe(this._fragment.getViewLifecycleOwner(), this::_onProducts);
  }

  /**
   * @noinspection OptionalUsedAsFieldOrParameterType
   */
  private void _onResultSelectedProductId(@NonNull Optional<Long> productId) {
    Objects.requireNonNull(productId);

    final Bundle bundle = new Bundle();

    productId.ifPresent(
        id -> bundle.putLong(SearchProductFragment.Result.SELECTED_PRODUCT_ID_LONG.key(), id));

    this._fragment
        .getParentFragmentManager()
        .setFragmentResult(SearchProductFragment.Request.SELECT_PRODUCT.key(), bundle);
    this._fragment.finish();
  }

  /**
   * @noinspection OptionalUsedAsFieldOrParameterType
   */
  private void _onProducts(@NonNull Optional<List<ProductModel>> products) {
    Objects.requireNonNull(products);

    this._fragment.adapter().notifyDataSetChanged();

    final int noResultsVisibility =
        // Only show illustration when products are empty list.
        products.isPresent() && products.get().isEmpty() ? View.VISIBLE : View.GONE;
    final int recyclerVisibility =
        products.isPresent() && !products.get().isEmpty() ? View.VISIBLE : View.GONE;

    this._fragment.fragmentBinding().horizontalListContainer.setVisibility(noResultsVisibility);
    this._fragment.fragmentBinding().noResultsImage.getRoot().setVisibility(noResultsVisibility);
    this._fragment.fragmentBinding().recyclerView.setVisibility(recyclerVisibility);
  }
}
