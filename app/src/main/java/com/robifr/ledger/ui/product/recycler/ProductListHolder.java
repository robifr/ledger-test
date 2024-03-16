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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.databinding.ProductCardWideBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.product.ProductCardNormalComponent;
import com.robifr.ledger.ui.product.ProductFragment;
import java.util.Objects;

public class ProductListHolder extends RecyclerViewHolder<ProductModel> {
  @NonNull protected final ProductFragment _fragment;
  @NonNull protected final ProductCardWideBinding _cardBinding;
  @NonNull protected final ProductCardNormalComponent _normalCard;
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
    this._menu = new ProductListMenu(this._fragment, this);

    this._cardBinding.cardView.setClickable(false);
    this._cardBinding.normalCard.menuButton.setOnClickListener(v -> this._menu.openDialog());
  }

  @Override
  public void bind(@NonNull ProductModel product) {
    this._boundProduct = Objects.requireNonNull(product);
    this._normalCard.setProduct(this._boundProduct);
  }

  @NonNull
  public ProductModel boundProduct() {
    return Objects.requireNonNull(this._boundProduct);
  }
}
