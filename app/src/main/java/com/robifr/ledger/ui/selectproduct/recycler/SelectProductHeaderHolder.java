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

package com.robifr.ledger.ui.selectproduct.recycler;

import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.databinding.ListableListSelectedItemBinding;
import com.robifr.ledger.databinding.ProductCardWideBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.product.ProductCardWideComponent;
import com.robifr.ledger.ui.selectproduct.SelectedProductAction;
import java.util.Objects;
import java.util.Optional;

public class SelectProductHeaderHolder<T extends SelectedProductAction>
    extends RecyclerViewHolder<Optional<ProductModel>, T> implements View.OnClickListener {
  @NonNull private final ListableListSelectedItemBinding _headerBinding;
  @NonNull private final ProductCardWideBinding _selectedCardBinding;
  @NonNull private final ProductCardWideComponent _selectedCard;

  public SelectProductHeaderHolder(
      @NonNull ListableListSelectedItemBinding binding, @NonNull T action) {
    super(binding.getRoot(), action);
    this._headerBinding = Objects.requireNonNull(binding);
    this._selectedCardBinding =
        ProductCardWideBinding.inflate(
            LayoutInflater.from(this.itemView.getContext()),
            this._headerBinding.selectedItemContainer,
            false);
    this._selectedCard =
        new ProductCardWideComponent(this.itemView.getContext(), this._selectedCardBinding);

    this._headerBinding.selectedItemTitle.setText(R.string.selectProduct_selectedProduct);
    this._headerBinding.selectedItemContainer.addView(this._selectedCardBinding.getRoot());
    this._headerBinding.allListTitle.setText(R.string.selectProduct_allProducts);
    this._headerBinding.newButton.setOnClickListener(this);
    // Don't set menu button to `View.GONE` as the position will be occupied by expand button.
    this._selectedCardBinding.normalCard.menuButton.setVisibility(View.INVISIBLE);
    this._selectedCardBinding.normalCard.expandButton.setVisibility(View.VISIBLE);
    this._selectedCardBinding.normalCard.expandButton.setOnClickListener(this);
    this._selectedCardBinding.expandedCard.menuButton.setVisibility(View.INVISIBLE);
    this._selectedCardBinding.expandedCard.expandButton.setVisibility(View.VISIBLE);
    this._selectedCardBinding.expandedCard.expandButton.setOnClickListener(this);
  }

  @Override
  public void bind(@NonNull Optional<ProductModel> selectedProduct) {
    Objects.requireNonNull(selectedProduct);

    if (!selectedProduct.isPresent()) {
      this._selectedCard.reset();
      this._headerBinding.selectedItemDescription.setVisibility(View.GONE);
      this._headerBinding.selectedItemTitle.setVisibility(View.GONE);
      this._headerBinding.selectedItemContainer.setVisibility(View.GONE);
      return;
    }

    this._selectedCard.reset();
    this._selectedCard.setNormalCardProduct(selectedProduct.get());
    this._selectedCard.setExpandedCardProduct(selectedProduct.get());
    this._selectedCard.setCardChecked(true);
    this._headerBinding.selectedItemTitle.setVisibility(View.VISIBLE);
    this._headerBinding.selectedItemContainer.setVisibility(View.VISIBLE);
    this.setCardExpanded(this._action.isSelectedProductExpanded());
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.newButton ->
          Navigation.findNavController(this.itemView).navigate(R.id.createProductFragment);

      case R.id.expandButton -> {
        final boolean isExpanded =
            this._selectedCardBinding.expandedCard.getRoot().getVisibility() == View.VISIBLE;

        this._action.onSelectedProductExpanded(!isExpanded);
        // Display ripple effect. The effect is gone due to the clicked view
        // set to `View.GONE` when the card expand/collapse.
        if (isExpanded) {
          this._selectedCardBinding.normalCard.expandButton.setPressed(true);
          this._selectedCardBinding.normalCard.expandButton.setPressed(false);
        } else {
          this._selectedCardBinding.expandedCard.expandButton.setPressed(true);
          this._selectedCardBinding.expandedCard.expandButton.setPressed(false);
        }
      }
    }
  }

  public void setCardExpanded(boolean isExpanded) {
    this._selectedCard.setCardExpanded(isExpanded);
  }

  public void setSelectedItemDescriptionText(@Nullable String text) {
    this._headerBinding.selectedItemDescription.setText(text);
  }

  public void setSelectedItemDescriptionVisible(boolean isVisible) {
    final int visibility = isVisible ? View.VISIBLE : View.GONE;
    this._headerBinding.selectedItemDescription.setVisibility(visibility);
  }
}
