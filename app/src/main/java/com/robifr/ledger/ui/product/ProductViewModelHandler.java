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

package com.robifr.ledger.ui.product;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.ui.LiveDataEvent.Observer;
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
        .observe(this._fragment.getViewLifecycleOwner(), new Observer<>(this::_onSnackbarMessage));
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

  private void _onSnackbarMessage(@Nullable StringResources stringRes) {
    if (stringRes == null) return;

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

      if (viewHolder instanceof ProductListHolder holder) holder.setCardExpanded(false);
    }

    // Expand the selected card.
    if (index != -1) {
      final RecyclerView.ViewHolder viewHolder =
          // +1 offset because header holder.
          this._fragment.fragmentBinding().recyclerView.findViewHolderForLayoutPosition(index + 1);

      if (viewHolder instanceof ProductListHolder holder) holder.setCardExpanded(true);
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
