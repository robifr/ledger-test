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

package com.robifr.ledger.ui.product.recycler;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.databinding.ProductCardWideBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.product.ProductCardAction;
import com.robifr.ledger.ui.product.ProductCardExpandedComponent;
import com.robifr.ledger.ui.product.ProductCardNormalComponent;
import com.robifr.ledger.ui.product.ProductListAction;
import java.util.Objects;

public class ProductListHolder<T extends ProductListAction & ProductCardAction>
    extends RecyclerViewHolder<ProductModel, T> implements View.OnClickListener {
  @NonNull protected final ProductCardWideBinding _cardBinding;
  @NonNull protected final ProductCardNormalComponent _normalCard;
  @NonNull protected final ProductCardExpandedComponent _expandedCard;
  @NonNull protected final ProductListMenu _menu;
  @Nullable protected ProductModel _boundProduct;

  public ProductListHolder(@NonNull ProductCardWideBinding binding, @NonNull T action) {
    super(binding.getRoot(), action);
    this._cardBinding = Objects.requireNonNull(binding);
    this._normalCard =
        new ProductCardNormalComponent(this.itemView.getContext(), this._cardBinding.normalCard);
    this._expandedCard =
        new ProductCardExpandedComponent(
            this.itemView.getContext(), this._cardBinding.expandedCard);
    this._menu = new ProductListMenu(this);

    this._cardBinding.cardView.setOnClickListener(this);
    this._cardBinding.normalCard.menuButton.setOnClickListener(v -> this._menu.openDialog());
    this._cardBinding.expandedCard.menuButton.setOnClickListener(v -> this._menu.openDialog());
  }

  @Override
  public void bind(@NonNull ProductModel product) {
    this._boundProduct = Objects.requireNonNull(product);

    this._normalCard.setProduct(this._boundProduct);
    this._expandedCard.reset();

    // Prevent reused view holder card from being expanded.
    final boolean shouldCardExpanded =
        this._action.expandedProductIndex() != -1
            && this._boundProduct.equals(
                this._action.products().get(this._action.expandedProductIndex()));
    this.setCardExpanded(shouldCardExpanded);
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.cardView -> {
        final int expandedQueueIndex =
            this._cardBinding.expandedCard.getRoot().getVisibility() != View.VISIBLE
                ? this._action.products().indexOf(this._boundProduct)
                : -1;
        this._action.onExpandedProductIndexChanged(expandedQueueIndex);
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
