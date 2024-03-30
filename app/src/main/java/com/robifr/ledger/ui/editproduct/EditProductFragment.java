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
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.Navigation;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.ui.FragmentResultKey;
import com.robifr.ledger.ui.createproduct.CreateProductFragment;
import com.robifr.ledger.ui.editproduct.viewmodel.EditProductViewModel;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.util.Objects;

public class EditProductFragment extends CreateProductFragment {
  public enum Arguments implements FragmentResultKey {
    INITIAL_PRODUCT_ID_TO_EDIT;

    @Override
    @NonNull
    public String key() {
      return FragmentResultKey.generateKey(this);
    }
  }

  public enum Request implements FragmentResultKey {
    EDIT_PRODUCT;

    @Override
    @NonNull
    public String key() {
      return FragmentResultKey.generateKey(this);
    }
  }

  public enum Result implements FragmentResultKey {
    EDITED_PRODUCT_ID;

    @Override
    @NonNull
    public String key() {
      return FragmentResultKey.generateKey(this);
    }
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
    super.onViewCreated(view, savedInstance);
    Objects.requireNonNull(this._fragmentBinding);

    this._createProductViewModel =
        new ViewModelProvider(this, new EditProductViewModel.Factory(this.requireContext()))
            .get(EditProductViewModel.class);
    this._viewModelHandler =
        new EditProductViewModelHandler(this, (EditProductViewModel) this._createProductViewModel);

    this._fragmentBinding.toolbar.setTitle(this.getString(R.string.text_edit_product));

    final NavBackStackEntry backStackEntry =
        Navigation.findNavController(this._fragmentBinding.getRoot()).getCurrentBackStackEntry();

    if (this._createProductViewModel instanceof EditProductViewModel editProductViewModel
        && backStackEntry != null
        && backStackEntry.getArguments() != null) {
      final ProductModel initialProduct =
          editProductViewModel.selectProductById(
              backStackEntry
                  .getArguments()
                  .getLong(Arguments.INITIAL_PRODUCT_ID_TO_EDIT.key(), 0L));
      Objects.requireNonNull(initialProduct); // Logically shouldn't be null when editing data.

      editProductViewModel.setInitialProductToEdit(initialProduct);
      editProductViewModel.onNameTextChanged(initialProduct.name());
      editProductViewModel.onPriceTextChanged(
          CurrencyFormat.format(BigDecimal.valueOf(initialProduct.price()), "id", "ID"));
    }
  }
}
