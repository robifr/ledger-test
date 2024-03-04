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
import com.robifr.ledger.ui.LiveDataEvent.Observer;
import com.robifr.ledger.ui.createproduct.CreateProductViewModelHandler;
import com.robifr.ledger.ui.editproduct.viewmodel.EditProductViewModel;

public class EditProductViewModelHandler extends CreateProductViewModelHandler {
  public EditProductViewModelHandler(
      @NonNull EditProductFragment fragment, @NonNull EditProductViewModel viewModel) {
    super(fragment, viewModel);
    viewModel
        .editedProductId()
        .observe(this._fragment.getViewLifecycleOwner(), new Observer<>(this::_onEditedProductId));
  }

  private void _onEditedProductId(@Nullable Long productId) {
    if (productId != null) {
      final Bundle bundle = new Bundle();
      bundle.putLong(EditProductFragment.Result.EDITED_PRODUCT_ID.key(), productId);

      this._fragment
          .getParentFragmentManager()
          .setFragmentResult(EditProductFragment.Request.EDIT_PRODUCT.key(), bundle);
    }

    this._fragment.finish();
  }
}
