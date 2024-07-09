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

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.ProductCardDialogMenuBinding;
import com.robifr.ledger.ui.editproduct.EditProductFragment;
import com.robifr.ledger.ui.product.ProductFragment;
import java.util.Objects;

public class ProductListMenu implements View.OnClickListener {
  @NonNull private final ProductFragment _fragment;
  @NonNull private final ProductListHolder _holder;
  @NonNull private final ProductCardDialogMenuBinding _dialogBinding;
  @NonNull private final BottomSheetDialog _dialog;

  public ProductListMenu(@NonNull ProductFragment fragment, @NonNull ProductListHolder holder) {
    this._fragment = Objects.requireNonNull(fragment);
    this._holder = Objects.requireNonNull(holder);
    this._dialogBinding = ProductCardDialogMenuBinding.inflate(this._fragment.getLayoutInflater());
    this._dialog =
        new BottomSheetDialog(this._fragment.requireContext(), R.style.BottomSheetDialog);

    this._dialog.setContentView(this._dialogBinding.getRoot());
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.editButton -> {
        final Long productId = this._holder.boundProduct().id();
        if (productId == null) return;

        final Bundle bundle = new Bundle();
        bundle.putLong(EditProductFragment.Arguments.INITIAL_PRODUCT_ID_TO_EDIT.key(), productId);

        Navigation.findNavController(this._fragment.fragmentBinding().getRoot())
            .navigate(R.id.editProductFragment, bundle);
        this._dialog.dismiss();
      }

      case R.id.deleteButton -> {
        this._fragment.productViewModel().deleteProduct(this._holder.boundProduct());
        this._dialog.dismiss();
      }
    }
  }

  public void openDialog() {
    this._dialogBinding.editButton.setOnClickListener(this);
    this._dialogBinding.deleteButton.setOnClickListener(this);
    this._dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    this._dialog.show();
  }
}
