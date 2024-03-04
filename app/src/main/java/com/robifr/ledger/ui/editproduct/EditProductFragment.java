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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.lifecycle.ViewModelProvider;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.ui.FragmentResultKey;
import com.robifr.ledger.ui.createproduct.CreateProductFragment;
import com.robifr.ledger.ui.editproduct.viewmodel.EditProductViewModel;
import com.robifr.ledger.util.CurrencyFormat;
import java.math.BigDecimal;
import java.util.Objects;

public class EditProductFragment extends CreateProductFragment {
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

  @NonNull private final Long _initialProductIdToEdit;

  /** Default constructor when configuration changes. */
  public EditProductFragment() {
    this(0L);
  }

  protected EditProductFragment(@NonNull Long initialProductIdToEdit) {
    this._initialProductIdToEdit = Objects.requireNonNull(initialProductIdToEdit);
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

    if (this._createProductViewModel instanceof EditProductViewModel editProductViewModel) {
      final ProductModel initialProduct =
          editProductViewModel.selectProductById(this._initialProductIdToEdit);
      Objects.requireNonNull(initialProduct); // Logically shouldn't be null when editing data.

      editProductViewModel.setInitialProductToEdit(initialProduct);
      editProductViewModel.onNameTextChanged(initialProduct.name());
      editProductViewModel.onPriceTextChanged(
          CurrencyFormat.format(BigDecimal.valueOf(initialProduct.price()), "id", "ID"));
    }
  }

  public static class Factory extends FragmentFactory {
    @NonNull private final Long _initialProductIdToEdit;

    public Factory(@NonNull Long initialProductIdToEdit) {
      this._initialProductIdToEdit = Objects.requireNonNull(initialProductIdToEdit);
    }

    @Override
    @NonNull
    public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
      Objects.requireNonNull(classLoader);
      Objects.requireNonNull(className);

      return (className.equals(EditProductFragment.class.getName()))
          ? new EditProductFragment(this._initialProductIdToEdit)
          : super.instantiate(classLoader, className);
    }
  }
}
