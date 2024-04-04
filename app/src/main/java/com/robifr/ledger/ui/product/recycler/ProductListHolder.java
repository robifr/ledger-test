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

package com.robifr.ledger.ui.product.recycler;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.databinding.ProductCardWideBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.product.ProductCardExpandedComponent;
import com.robifr.ledger.ui.product.ProductCardNormalComponent;
import com.robifr.ledger.ui.product.ProductFragment;
import java.util.List;
import java.util.Objects;

public class ProductListHolder extends RecyclerViewHolder<ProductModel>
    implements View.OnClickListener {
  @NonNull protected final ProductFragment _fragment;
  @NonNull protected final ProductCardWideBinding _cardBinding;
  @NonNull protected final ProductCardNormalComponent _normalCard;
  @NonNull protected final ProductCardExpandedComponent _expandedCard;
  @NonNull protected final ProductListMenu _menu;
  @Nullable protected ProductModel _boundProduct;

  public ProductListHolder(
      @NonNull ProductFragment fragment, @NonNull ProductCardWideBinding binding) {
    super(binding.getRoot());
    this._fragment = Objects.requireNonNull(fragment);
    this._cardBinding = Objects.requireNonNull(binding);
    this._normalCard =
        new ProductCardNormalComponent(
            this._fragment.requireContext(), this._cardBinding.normalCard);
    this._expandedCard =
        new ProductCardExpandedComponent(
            this._fragment.requireContext(), this._cardBinding.expandedCard);
    this._menu = new ProductListMenu(this._fragment, this);

    this._cardBinding.cardView.setOnClickListener(this);
    this._cardBinding.normalCard.menuButton.setOnClickListener(v -> this._menu.openDialog());
    this._cardBinding.expandedCard.menuButton.setOnClickListener(v -> this._menu.openDialog());
  }

  @Override
  public void bind(@NonNull ProductModel product) {
    this._boundProduct = Objects.requireNonNull(product);

    this._normalCard.setProduct(this._boundProduct);
    this._expandedCard.reset();

    // Prevent reused view holder to expand the card
    // if current bound product is different with selected expanded card.
    final List<ProductModel> products = this._fragment.productViewModel().products().getValue();
    final int expandedProductIndex =
        Objects.requireNonNullElse(
            this._fragment.productViewModel().expandedProductIndex().getValue(), -1);
    final boolean shouldCardExpanded =
        expandedProductIndex != -1
            && products != null
            && this._boundProduct.equals(products.get(expandedProductIndex));

    this.setCardExpanded(shouldCardExpanded);
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.cardView -> {
        final List<ProductModel> products = this._fragment.productViewModel().products().getValue();
        if (products == null) return;

        // Only expand when it shrank.
        final int expandedQueueIndex =
            this._cardBinding.expandedCard.getRoot().getVisibility() != View.VISIBLE
                ? products.indexOf(this._boundProduct)
                : -1;
        this._fragment.productViewModel().onExpandedProductIndexChanged(expandedQueueIndex);
      }
    }
  }

  @NonNull
  public ProductModel boundProduct() {
    return Objects.requireNonNull(this._boundProduct);
  }

  public void setCardExpanded(boolean isExpanded) {
    Objects.requireNonNull(this._boundProduct);

    final int normalCardVisibility = isExpanded ? View.GONE : View.VISIBLE;
    final int expandedCardVisibility = isExpanded ? View.VISIBLE : View.GONE;

    this._cardBinding.normalCard.getRoot().setVisibility(normalCardVisibility);
    this._cardBinding.expandedCard.getRoot().setVisibility(expandedCardVisibility);

    // Only fill the view when it's shown on screen.
    if (isExpanded) this._expandedCard.setProduct(this._boundProduct);
  }
}
