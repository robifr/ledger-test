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

package com.robifr.ledger.ui.searchproduct;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.ui.searchproduct.viewmodel.SearchProductViewModel;
import com.robifr.ledger.util.Compats;
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
    this._viewModel.query().observe(this._fragment.getViewLifecycleOwner(), this::_onQuery);
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

  private void _onQuery(@NonNull String query) {
    Objects.requireNonNull(query);

    if (!query.isEmpty()) {
      this._fragment.fragmentBinding().searchView.setQuery(query, true);
    } else {
      Compats.showKeyboard(
          this._fragment.requireContext(), this._fragment.fragmentBinding().searchView);
    }
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
