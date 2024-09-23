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

package com.robifr.ledger.ui.editproduct;

import android.os.Bundle;
import androidx.annotation.NonNull;
import com.robifr.ledger.ui.createproduct.CreateProductViewModelHandler;
import com.robifr.ledger.ui.editproduct.viewmodel.EditProductViewModel;
import java.util.Objects;
import java.util.Optional;

public class EditProductViewModelHandler extends CreateProductViewModelHandler {
  public EditProductViewModelHandler(
      @NonNull EditProductFragment fragment, @NonNull EditProductViewModel viewModel) {
    super(fragment, viewModel);
    viewModel
        .resultEditedProductId()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onResultEditedProductId));
  }

  /**
   * @noinspection OptionalUsedAsFieldOrParameterType
   */
  private void _onResultEditedProductId(@NonNull Optional<Long> productId) {
    Objects.requireNonNull(productId);

    productId.ifPresent(
        id -> {
          final Bundle bundle = new Bundle();
          bundle.putLong(EditProductFragment.Result.EDITED_PRODUCT_ID_LONG.key(), id);

          this._fragment
              .getParentFragmentManager()
              .setFragmentResult(EditProductFragment.Request.EDIT_PRODUCT.key(), bundle);
        });
    this._fragment.finish();
  }
}
