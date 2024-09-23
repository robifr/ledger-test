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

package com.robifr.ledger.ui.createproduct;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.createproduct.viewmodel.CreateProductViewModel;
import java.util.Objects;
import java.util.Optional;

public class CreateProductViewModelHandler {
  @NonNull protected final CreateProductFragment _fragment;
  @NonNull protected final CreateProductViewModel _viewModel;

  public CreateProductViewModelHandler(
      @NonNull CreateProductFragment fragment, @NonNull CreateProductViewModel viewModel) {
    this._fragment = Objects.requireNonNull(fragment);
    this._viewModel = Objects.requireNonNull(viewModel);

    this._viewModel
        .resultCreatedProductId()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onResultCreatedProductId));
    this._viewModel
        .snackbarMessage()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onSnackbarMessage));
    this._viewModel
        .inputtedNameError()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onInputtedNameError);
    this._viewModel
        .inputtedNameText()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onInputtedNameText);
    this._viewModel
        .inputtedPriceText()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onInputtedPriceText);
  }

  /**
   * @noinspection OptionalUsedAsFieldOrParameterType
   */
  private void _onResultCreatedProductId(@NonNull Optional<Long> productId) {
    Objects.requireNonNull(productId);

    productId.ifPresent(
        id -> {
          final Bundle bundle = new Bundle();
          bundle.putLong(CreateProductFragment.Result.CREATED_PRODUCT_ID_LONG.key(), id);

          this._fragment
              .getParentFragmentManager()
              .setFragmentResult(CreateProductFragment.Request.CREATE_PRODUCT.key(), bundle);
        });
    this._fragment.finish();
  }

  private void _onSnackbarMessage(@NonNull StringResources stringRes) {
    Objects.requireNonNull(stringRes);

    Snackbar.make(
            (View) this._fragment.fragmentBinding().getRoot().getParent(),
            StringResources.stringOf(this._fragment.requireContext(), stringRes),
            Snackbar.LENGTH_LONG)
        .show();
  }

  /**
   * @noinspection OptionalUsedAsFieldOrParameterType
   */
  private void _onInputtedNameError(@NonNull Optional<StringResources> stringRes) {
    Objects.requireNonNull(stringRes);

    this._fragment
        .inputName()
        .setError(
            stringRes
                .map(string -> StringResources.stringOf(this._fragment.requireContext(), string))
                .orElse(null));
  }

  private void _onInputtedNameText(@NonNull String name) {
    Objects.requireNonNull(name);

    this._fragment.inputName().setInputtedNameText(name);
  }

  private void _onInputtedPriceText(@NonNull String price) {
    Objects.requireNonNull(price);

    this._fragment.inputPrice().setInputtedPriceText(price);
  }
}
