/**
 * Copyright (c) 2022-present Robi
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

package com.robifr.ledger.ui.searchable.product.recycler;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.databinding.ProductCardNormalBinding;
import com.robifr.ledger.ui.RecyclerViewAdapterOld;
import com.robifr.ledger.ui.RecyclerViewHolderOld;
import com.robifr.ledger.ui.main.product.ProductCardNormalComponent;

public class SearchProductVerticalHolder extends RecyclerViewHolderOld {
  @NonNull private final ProductCardNormalBinding _normalCardBinding;
  @NonNull private final ProductCardNormalComponent _normalCard;
  @Nullable private ProductModel _productModel;

  // TODO: View binding.
  public SearchProductVerticalHolder(
      @NonNull AppCompatActivity activity,
      @NonNull RecyclerViewAdapterOld adapter,
      @NonNull View view) {
    super(activity, adapter, view);
    this._normalCardBinding = ProductCardNormalBinding.bind(view);
    this._normalCard = new ProductCardNormalComponent(this._activity, this._normalCardBinding);

    this._normalCardBinding.menuButton.setVisibility(View.GONE);
  }

  @Override
  public void bind(int index) {
    this._productModel = (ProductModel) this._adapter.list().get(index);
    this._normalCard.setProduct(this._productModel);
  }
}
