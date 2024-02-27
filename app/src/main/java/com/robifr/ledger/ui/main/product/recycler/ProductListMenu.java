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

package com.robifr.ledger.ui.main.product.recycler;

import android.view.View;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.robifr.ledger.R;
import com.robifr.ledger.databinding.ProductCardDialogMenuBinding;
import com.robifr.ledger.ui.BackStack;
import com.robifr.ledger.ui.edit_product.EditProductFragment;
import com.robifr.ledger.ui.main.product.ProductFragment;
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
        if (this._holder.boundProduct().id() == null) return;

        final EditProductFragment editProductFragment =
            (EditProductFragment)
                new EditProductFragment.Factory(this._holder.boundProduct().id())
                    .instantiate(
                        this._fragment.requireContext().getClassLoader(),
                        EditProductFragment.class.getName());

        if (this._fragment.requireActivity() instanceof BackStack navigation
            && navigation.currentTabStackTag() != null) {
          navigation.pushFragmentStack(
              navigation.currentTabStackTag(),
              editProductFragment,
              EditProductFragment.class.toString());
          this._dialog.dismiss();
        }
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
    this._dialog.show();
  }
}
