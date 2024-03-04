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

package com.robifr.ledger.ui.createproduct;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import com.robifr.ledger.ui.LiveDataEvent.Observer;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.createproduct.viewmodel.CreateProductViewModel;
import java.util.Objects;

public class CreateProductViewModelHandler {
  @NonNull protected final CreateProductFragment _fragment;
  @NonNull protected final CreateProductViewModel _viewModel;

  public CreateProductViewModelHandler(
      @NonNull CreateProductFragment fragment, @NonNull CreateProductViewModel viewModel) {
    this._fragment = Objects.requireNonNull(fragment);
    this._viewModel = Objects.requireNonNull(viewModel);

    this._viewModel
        .snackbarMessage()
        .observe(this._fragment.requireActivity(), new Observer<>(this::_onSnackbarMessage));
    this._viewModel
        .createdProductId()
        .observe(this._fragment.getViewLifecycleOwner(), new Observer<>(this::_onCreatedProductId));
    this._viewModel
        .inputtedNameError()
        .observe(
            this._fragment.getViewLifecycleOwner(), new Observer<>(this::_onInputtedNameError));
    this._viewModel
        .inputtedNameText()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onInputtedNameText);
    this._viewModel
        .inputtedPriceText()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onInputtedPriceText);
  }

  private void _onSnackbarMessage(@Nullable StringResources stringRes) {
    if (stringRes == null) return;

    Snackbar.make(
            this._fragment.requireView(),
            StringResources.stringOf(this._fragment.requireContext(), stringRes),
            Snackbar.LENGTH_LONG)
        .show();
  }

  private void _onCreatedProductId(@Nullable Long id) {
    if (id != null) {
      final Bundle bundle = new Bundle();
      bundle.putLong(CreateProductFragment.Result.CREATED_PRODUCT_ID.key(), id);

      this._fragment
          .getParentFragmentManager()
          .setFragmentResult(CreateProductFragment.Request.CREATE_PRODUCT.key(), bundle);
    }

    this._fragment.finish();
  }

  private void _onInputtedNameError(@Nullable StringResources stringRes) {
    final String text =
        stringRes != null
            ? StringResources.stringOf(this._fragment.requireContext(), stringRes)
            : null;
    this._fragment.inputName().setError(text);
  }

  private void _onInputtedNameText(@Nullable String name) {
    this._fragment.inputName().setInputtedNameText(name);
  }

  private void _onInputtedPriceText(@Nullable String price) {
    this._fragment.inputPrice().setInputtedPriceText(price);
  }
}
