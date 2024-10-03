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

package com.robifr.ledger.ui.selectproduct;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.selectproduct.recycler.SelectProductHeaderHolder;
import com.robifr.ledger.ui.selectproduct.recycler.SelectProductListHolder;
import com.robifr.ledger.ui.selectproduct.viewmodel.SelectProductViewModel;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SelectProductViewModelHandler {
  @NonNull private final SelectProductFragment _fragment;
  @NonNull private final SelectProductViewModel _viewModel;

  public SelectProductViewModelHandler(
      @NonNull SelectProductFragment fragment, @NonNull SelectProductViewModel viewModel) {
    this._fragment = Objects.requireNonNull(fragment);
    this._viewModel = Objects.requireNonNull(viewModel);

    this._viewModel
        .resultSelectedProductId()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onResultSelectedProductId));
    this._viewModel
        .snackbarMessage()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onSnackbarMessage));
    this._viewModel.products().observe(this._fragment.getViewLifecycleOwner(), this::_onProducts);
    this._viewModel
        .isSelectedProductExpanded()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onSelectedProductExpanded);
    this._viewModel
        .expandedProductIndex()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onExpandedProductIndex);
  }

  /**
   * @noinspection OptionalUsedAsFieldOrParameterType
   */
  private void _onResultSelectedProductId(@NonNull Optional<Long> productId) {
    Objects.requireNonNull(productId);

    final Bundle bundle = new Bundle();

    productId.ifPresent(
        id -> bundle.putLong(SelectProductFragment.Result.SELECTED_PRODUCT_ID_LONG.key(), id));

    this._fragment
        .getParentFragmentManager()
        .setFragmentResult(SelectProductFragment.Request.SELECT_PRODUCT.key(), bundle);
    this._fragment.finish();
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

    final RecyclerView.ViewHolder viewHolder =
        this._fragment.fragmentBinding().recyclerView.findViewHolderForLayoutPosition(0);

    if (viewHolder instanceof SelectProductHeaderHolder<?> headerHolder) {
      final ProductModel selectedProduct = this._viewModel.initialSelectedProduct();
      final ProductModel selectedProductOnDb =
          selectedProduct != null
              ? products.stream()
                  .filter(
                      product -> product.id() != null && product.id().equals(selectedProduct.id()))
                  .findFirst()
                  .orElse(null)
              : null;

      // The original product on database was deleted.
      if (selectedProduct != null && selectedProductOnDb == null) {
        headerHolder.setSelectedItemDescriptionText(
            this._fragment
                .requireContext()
                .getString(R.string.selectProduct_originalProductDeleted));
        headerHolder.setSelectedItemDescriptionVisible(true);

        // The original product on database was edited.
      } else if (selectedProduct != null && !selectedProduct.equals(selectedProductOnDb)) {
        headerHolder.setSelectedItemDescriptionText(
            this._fragment
                .requireContext()
                .getString(R.string.selectProduct_originalProductChanged));
        headerHolder.setSelectedItemDescriptionVisible(true);

        // It's the same unchanged product.
      } else {
        headerHolder.setSelectedItemDescriptionVisible(false);
      }
    }

    this._fragment.adapter().notifyDataSetChanged();
  }

  private void _onSelectedProductExpanded(boolean isExpanded) {
    final RecyclerView.ViewHolder viewHolder =
        this._fragment.fragmentBinding().recyclerView.findViewHolderForLayoutPosition(0);

    if (viewHolder instanceof SelectProductHeaderHolder<?> holder) {
      holder.setCardExpanded(isExpanded);
    }
  }

  private void _onExpandedProductIndex(int index) {
    // Shrink all cards.
    for (int i = 0; i < this._fragment.fragmentBinding().recyclerView.getChildCount(); i++) {
      final RecyclerView.ViewHolder viewHolder =
          this._fragment
              .fragmentBinding()
              .recyclerView
              .getChildViewHolder(this._fragment.fragmentBinding().recyclerView.getChildAt(i));

      if (viewHolder instanceof SelectProductListHolder<?> holder) holder.setCardExpanded(false);
    }

    // Expand the selected card.
    if (index != -1) {
      final RecyclerView.ViewHolder viewHolder =
          // +1 offset because header holder.
          this._fragment.fragmentBinding().recyclerView.findViewHolderForLayoutPosition(index + 1);

      if (viewHolder instanceof SelectProductListHolder<?> holder) holder.setCardExpanded(true);
    }
  }
}
