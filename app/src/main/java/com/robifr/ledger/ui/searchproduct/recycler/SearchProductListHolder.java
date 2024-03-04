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

package com.robifr.ledger.ui.searchproduct.recycler;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.databinding.ProductCardBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.main.product.ProductCardNormalComponent;
import com.robifr.ledger.ui.searchproduct.SearchProductFragment;
import java.util.Objects;

public class SearchProductListHolder extends RecyclerViewHolder<ProductModel>
    implements View.OnClickListener {
  @NonNull private final SearchProductFragment _fragment;
  @NonNull private final ProductCardBinding _cardBinding;
  @NonNull private final ProductCardNormalComponent _normalCard;
  @Nullable private ProductModel _boundProduct;

  public SearchProductListHolder(
      @NonNull SearchProductFragment fragment, @NonNull ProductCardBinding binding) {
    super(binding.getRoot());
    this._fragment = Objects.requireNonNull(fragment);
    this._cardBinding = Objects.requireNonNull(binding);
    this._normalCard =
        new ProductCardNormalComponent(
            this._fragment.requireContext(), this._cardBinding.normalCard);

    this._cardBinding.cardView.setOnClickListener(this);
    this._cardBinding.normalCard.menuButton.setVisibility(View.GONE);
  }

  @Override
  public void bind(@NonNull ProductModel product) {
    this._boundProduct = Objects.requireNonNull(product);
    this._normalCard.setProduct(this._boundProduct);
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.cardView ->
          this._fragment.searchProductViewModel().onProductSelected(this._boundProduct);
    }
  }
}
