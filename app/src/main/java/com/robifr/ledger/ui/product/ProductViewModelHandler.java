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

package com.robifr.ledger.ui.product;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.product.recycler.ProductListHolder;
import com.robifr.ledger.ui.product.viewmodel.ProductViewModel;
import java.util.List;
import java.util.Objects;

public class ProductViewModelHandler {
  @NonNull private final ProductFragment _fragment;
  @NonNull private final ProductViewModel _viewModel;

  public ProductViewModelHandler(
      @NonNull ProductFragment fragment, @NonNull ProductViewModel viewModel) {
    this._fragment = Objects.requireNonNull(fragment);
    this._viewModel = Objects.requireNonNull(viewModel);

    this._viewModel
        .snackbarMessage()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onSnackbarMessage));
    this._viewModel.products().observe(this._fragment.getViewLifecycleOwner(), this::_onProducts);
    this._viewModel
        .expandedProductIndex()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onExpandedProductIndex);

    this._viewModel
        .filterView()
        .inputtedMinPriceText()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onFilteredMinPriceText);
    this._viewModel
        .filterView()
        .inputtedMaxPriceText()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onFilteredMaxPriceText);
  }

  private void _onSnackbarMessage(@NonNull StringResources stringRes) {
    Objects.requireNonNull(stringRes);

    Snackbar.make(
            (View) this._fragment.fragmentBinding().getRoot().getParent(),
            StringResources.stringOf(this._fragment.requireContext(), stringRes),
            Snackbar.LENGTH_LONG)
        .show();
  }

  private void _onProducts(@NonNull List<ProductModel> products) {
    Objects.requireNonNull(products);

    this._fragment.adapter().notifyDataSetChanged();
  }

  private void _onExpandedProductIndex(int index) {
    // Shrink all cards.
    for (int i = 0; i < this._fragment.fragmentBinding().recyclerView.getChildCount(); i++) {
      final RecyclerView.ViewHolder viewHolder =
          this._fragment
              .fragmentBinding()
              .recyclerView
              .getChildViewHolder(this._fragment.fragmentBinding().recyclerView.getChildAt(i));

      if (viewHolder instanceof ProductListHolder<?> holder) holder.setCardExpanded(false);
    }

    // Expand the selected card.
    if (index != -1) {
      final RecyclerView.ViewHolder viewHolder =
          // +1 offset because header holder.
          this._fragment.fragmentBinding().recyclerView.findViewHolderForLayoutPosition(index + 1);

      if (viewHolder instanceof ProductListHolder<?> holder) holder.setCardExpanded(true);
    }
  }

  private void _onFilteredMinPriceText(@NonNull String minPrice) {
    Objects.requireNonNull(minPrice);

    this._fragment.filter().filterPrice().setFilteredMinPriceText(minPrice);
  }

  private void _onFilteredMaxPriceText(@NonNull String maxPrice) {
    Objects.requireNonNull(maxPrice);

    this._fragment.filter().filterPrice().setFilteredMaxPriceText(maxPrice);
  }
}
