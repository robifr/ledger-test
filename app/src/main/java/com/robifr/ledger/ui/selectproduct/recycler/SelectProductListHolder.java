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

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.databinding.ProductCardWideBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.product.ProductCardWideComponent;
import com.robifr.ledger.ui.selectproduct.SelectProductCardAction;
import java.util.Objects;

public class SelectProductListHolder<T extends SelectProductCardAction>
    extends RecyclerViewHolder<ProductModel, T> implements View.OnClickListener {
  @NonNull private final ProductCardWideBinding _cardBinding;
  @NonNull private final ProductCardWideComponent _card;
  @Nullable private ProductModel _boundProduct;

  public SelectProductListHolder(@NonNull ProductCardWideBinding binding, @NonNull T action) {
    super(binding.getRoot(), action);
    this._cardBinding = Objects.requireNonNull(binding);
    this._card = new ProductCardWideComponent(this.itemView.getContext(), this._cardBinding);

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

    this._card.setNormalCardProduct(this._boundProduct);
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
