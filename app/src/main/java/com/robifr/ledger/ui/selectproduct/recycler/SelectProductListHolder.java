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

package com.robifr.ledger.ui.selectproduct.recycler;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.databinding.ProductCardWideBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.product.ProductCardNormalComponent;
import com.robifr.ledger.ui.selectproduct.SelectProductCardAction;
import java.util.Objects;

public class SelectProductListHolder<T extends SelectProductCardAction>
    extends RecyclerViewHolder<ProductModel, T> implements View.OnClickListener {
  @NonNull private final ProductCardWideBinding _cardBinding;
  @NonNull private final ProductCardNormalComponent _normalCard;
  @Nullable private ProductModel _boundProduct;

  public SelectProductListHolder(@NonNull ProductCardWideBinding binding, @NonNull T action) {
    super(binding.getRoot(), action);
    this._cardBinding = Objects.requireNonNull(binding);
    this._normalCard =
        new ProductCardNormalComponent(this.itemView.getContext(), this._cardBinding.normalCard);

    this._cardBinding.cardView.setOnClickListener(this);
    // Don't set menu button to `View.GONE` as the position will be occupied by expand button.
    this._cardBinding.normalCard.menuButton.setVisibility(View.INVISIBLE);
  }

  @Override
  public void bind(@NonNull ProductModel product) {
    this._boundProduct = Objects.requireNonNull(product);
    // Prevent reused view holder card from being expanded.
    final boolean shouldChecked =
        this._action.initialSelectedProduct() != null
            && this._action.initialSelectedProduct().id() != null
            && this._action.initialSelectedProduct().id().equals(this._boundProduct.id());

    this._normalCard.setProduct(this._boundProduct);
    this._cardBinding.cardView.setChecked(shouldChecked);
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);
    Objects.requireNonNull(this._boundProduct);

    switch (view.getId()) {
      case R.id.cardView -> this._action.onProductSelected(this._boundProduct);
    }
  }
}
