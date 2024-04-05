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
import androidx.annotation.Nullable;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.ui.LiveDataEvent.Observer;
import com.robifr.ledger.ui.searchproduct.viewmodel.SearchProductViewModel;
import com.robifr.ledger.util.Compats;
import java.util.List;
import java.util.Objects;

public class SearchProductViewModelHandler {
  @NonNull private final SearchProductFragment _fragment;
  @NonNull private final SearchProductViewModel _viewModel;

  public SearchProductViewModelHandler(
      @NonNull SearchProductFragment fragment, @NonNull SearchProductViewModel viewModel) {
    this._fragment = Objects.requireNonNull(fragment);
    this._viewModel = Objects.requireNonNull(viewModel);

    this._viewModel
        .initializedInitialQuery()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            new Observer<>(this::_onInitializedInitialQuery));
    this._viewModel
        .selectedProductId()
        .observe(
            this._fragment.getViewLifecycleOwner(), new Observer<>(this::_onSelectedProductId));
    this._viewModel.products().observe(this._fragment.getViewLifecycleOwner(), this::_onProducts);
  }

  private void _onInitializedInitialQuery(@Nullable String query) {
    if (query != null) {
      this._fragment.fragmentBinding().searchView.setQuery(query, true);
    } else {
      Compats.showKeyboard(
          this._fragment.requireContext(), this._fragment.fragmentBinding().searchView);
    }
  }

  private void _onSelectedProductId(@Nullable Long productId) {
    final Bundle bundle = new Bundle();

    if (productId != null) {
      bundle.putLong(SearchProductFragment.Result.SELECTED_PRODUCT_ID.key(), productId);
    }

    this._fragment
        .getParentFragmentManager()
        .setFragmentResult(SearchProductFragment.Request.SELECT_PRODUCT.key(), bundle);
    this._fragment.finish();
  }

  private void _onProducts(@Nullable List<ProductModel> products) {
    this._fragment.adapter().notifyDataSetChanged();

    final int noResultsVisibility =
        products != null && products.size() == 0 ? View.VISIBLE : View.GONE;
    final int recyclerVisibility =
        products != null && products.size() > 0 ? View.VISIBLE : View.GONE;

    this._fragment.fragmentBinding().horizontalListContainer.setVisibility(noResultsVisibility);
    this._fragment.fragmentBinding().noResultsImage.getRoot().setVisibility(noResultsVisibility);
    this._fragment.fragmentBinding().recyclerView.setVisibility(recyclerVisibility);
  }
}
