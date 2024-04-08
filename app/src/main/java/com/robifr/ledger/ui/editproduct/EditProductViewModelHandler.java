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

package com.robifr.ledger.ui.editproduct;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.ui.LiveDataEvent.Observer;
import com.robifr.ledger.ui.createproduct.CreateProductViewModelHandler;
import com.robifr.ledger.ui.editproduct.viewmodel.EditProductViewModel;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;

public class EditProductViewModelHandler extends CreateProductViewModelHandler {
  public EditProductViewModelHandler(
      @NonNull EditProductFragment fragment, @NonNull EditProductViewModel viewModel) {
    super(fragment, viewModel);
    viewModel
        .resultEditedProductId()
        .observe(
            this._fragment.getViewLifecycleOwner(), new Observer<>(this::_onResultEditedProductId));
    viewModel
        .initializedInitialProductToEdit()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            new Observer<>(this::_onInitializedInitialProductToEdit));
  }

  private void _onResultEditedProductId(@Nullable Long productId) {
    if (productId != null) {
      final Bundle bundle = new Bundle();
      bundle.putLong(EditProductFragment.Result.EDITED_PRODUCT_ID.key(), productId);

      this._fragment
          .getParentFragmentManager()
          .setFragmentResult(EditProductFragment.Request.EDIT_PRODUCT.key(), bundle);
    }

    this._fragment.finish();
  }

  private void _onInitializedInitialProductToEdit(@Nullable ProductModel product) {
    if (product == null) return;

    this._viewModel.onNameTextChanged(product.name());
    this._viewModel.onPriceTextChanged(
        CurrencyFormat.format(BigDecimal.valueOf(product.price()), "id", "ID"));
  }
}
