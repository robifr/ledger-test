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

package com.robifr.ledger.ui.selectproduct;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.selectproduct.viewmodel.SelectProductViewModel;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SelectProductViewModelHandler {
  @NonNull private final SelectProductFragment _fragment;
  @NonNull private final SelectProductViewModel _viewModel;

  public SelectProductViewModelHandler(
      @NonNull SelectProductFragment fragment, @NonNull SelectProductViewModel viewModel) {
    this._fragment = Objects.requireNonNull(fragment);
    this._viewModel = Objects.requireNonNull(viewModel);

    this._viewModel
        .resultSelectedProductId()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onResultSelectedProductId));
    this._viewModel
        .snackbarMessage()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onSnackbarMessage));
    this._viewModel.products().observe(this._fragment.getViewLifecycleOwner(), this::_onProducts);
  }

  /**
   * @noinspection OptionalUsedAsFieldOrParameterType
   */
  private void _onResultSelectedProductId(@NonNull Optional<Long> productId) {
    Objects.requireNonNull(productId);

    final Bundle bundle = new Bundle();

    productId.ifPresent(
        id -> bundle.putLong(SelectProductFragment.Result.SELECTED_PRODUCT_ID_LONG.key(), id));

    this._fragment
        .getParentFragmentManager()
        .setFragmentResult(SelectProductFragment.Request.SELECT_PRODUCT.key(), bundle);
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

  private void _onProducts(@NonNull List<ProductModel> products) {
    Objects.requireNonNull(products);

    this._fragment.adapter().notifyDataSetChanged();
  }
}
