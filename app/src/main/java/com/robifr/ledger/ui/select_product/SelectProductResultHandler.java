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

package com.robifr.ledger.ui.select_product;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentResultListener;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.ui.search_product.SearchProductFragment;
import com.robifr.ledger.ui.select_product.view_model.SelectProductViewModel;
import com.robifr.ledger.util.Enums;
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
            new SearchProductResultListener());
  }

  private class SearchProductResultListener implements FragmentResultListener {
    @Override
    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
      Objects.requireNonNull(requestKey);
      Objects.requireNonNull(result);

      final SearchProductFragment.Request request =
          Enums.valueOf(
              requestKey, SearchProductFragment.Request.class, SearchProductFragment.Request::key);
      if (request == null) return;

      switch (request) {
        case SELECT_PRODUCT -> {
          final SelectProductViewModel viewModel =
              SelectProductResultHandler.this._fragment.selectProductViewModel();
          final Long productId =
              result.getLong(SearchProductFragment.Result.SELECTED_PRODUCT_ID.key());
          final ProductModel selectedProduct =
              viewModel.products().getValue() != null && !productId.equals(0L)
                  ? viewModel.products().getValue().stream()
                      .filter(product -> product.id() != null && product.id().equals(productId))
                      .findFirst()
                      .orElse(null)
                  : null;

          if (selectedProduct != null
              && selectedProduct.id() != null
              && !selectedProduct.id().equals(viewModel.selectedProductId())) {
            viewModel.onProductSelected(selectedProduct);
          }
        }
      }
    }
  }
}
