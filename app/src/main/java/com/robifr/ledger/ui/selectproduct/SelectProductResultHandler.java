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
import androidx.annotation.NonNull;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.ui.searchproduct.SearchProductFragment;
import com.robifr.ledger.ui.selectproduct.viewmodel.SelectProductViewModel;
import java.util.Arrays;
import java.util.Objects;

public class SelectProductResultHandler {
  @NonNull private final SelectProductFragment _fragment;

  public SelectProductResultHandler(@NonNull SelectProductFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);

    this._fragment
        .getParentFragmentManager()
        .setFragmentResultListener(
            SearchProductFragment.Request.SELECT_PRODUCT.key(),
            this._fragment.getViewLifecycleOwner(),
            this::_onSearchProductResult);
  }

  private void _onSearchProductResult(@NonNull String requestKey, @NonNull Bundle result) {
    Objects.requireNonNull(requestKey);
    Objects.requireNonNull(result);

    final SearchProductFragment.Request request =
        Arrays.stream(SearchProductFragment.Request.values())
            .filter(e -> e.key().equals(requestKey))
            .findFirst()
            .orElse(null);
    if (request == null) return;

    switch (request) {
      case SELECT_PRODUCT -> {
        final SelectProductViewModel viewModel =
            SelectProductResultHandler.this._fragment.selectProductViewModel();
        final Long productId =
            result.getLong(SearchProductFragment.Result.SELECTED_PRODUCT_ID_LONG.key());
        final ProductModel selectedProduct =
            !productId.equals(0L)
                ? viewModel.products().getValue().stream()
                    .filter(product -> product.id() != null && product.id().equals(productId))
                    .findFirst()
                    .orElse(null)
                : null;

        if (selectedProduct != null) viewModel.onProductSelected(selectedProduct);
      }
    }
  }
}
