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

package com.robifr.ledger.ui.createqueue;

import android.os.Bundle;
import androidx.annotation.NonNull;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.ui.createqueue.viewmodel.CreateQueueViewModel;
import com.robifr.ledger.ui.selectcustomer.SelectCustomerFragment;
import com.robifr.ledger.ui.selectproduct.SelectProductFragment;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class CreateQueueResultHandler {
  @NonNull private final CreateQueueFragment _fragment;

  public CreateQueueResultHandler(@NonNull CreateQueueFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);

    this._fragment
        .getParentFragmentManager()
        .setFragmentResultListener(
            SelectCustomerFragment.Request.SELECT_CUSTOMER.key(),
            this._fragment.getViewLifecycleOwner(),
            this::_onSelectCustomerResult);
    this._fragment
        .getParentFragmentManager()
        .setFragmentResultListener(
            SelectProductFragment.Request.SELECT_PRODUCT.key(),
            this._fragment.getViewLifecycleOwner(),
            this::_onSelectProductResult);
  }

  private void _onSelectCustomerResult(@NonNull String requestKey, @NonNull Bundle result) {
    Objects.requireNonNull(requestKey);
    Objects.requireNonNull(result);

    final SelectCustomerFragment.Request request =
        Arrays.stream(SelectCustomerFragment.Request.values())
            .filter(e -> e.key().equals(requestKey))
            .findFirst()
            .orElse(null);
    if (request == null) return;

    switch (request) {
      case SELECT_CUSTOMER -> {
        final CreateQueueViewModel viewModel =
            CreateQueueResultHandler.this._fragment.createQueueViewModel();
        final Long customerId =
            result.getLong(SelectCustomerFragment.Result.SELECTED_CUSTOMER_ID_LONG.key());

        if (!customerId.equals(0L)) {
          viewModel
              .selectCustomerById(customerId)
              .observe(
                  CreateQueueResultHandler.this._fragment.getViewLifecycleOwner(),
                  viewModel::onCustomerChanged);
        } else {
          viewModel.onCustomerChanged(viewModel.inputtedCustomer().getValue().orElse(null));
        }
      }
    }
  }

  private void _onSelectProductResult(@NonNull String requestKey, @NonNull Bundle result) {
    Objects.requireNonNull(requestKey);
    Objects.requireNonNull(result);

    final SelectProductFragment.Request request =
        Arrays.stream(SelectProductFragment.Request.values())
            .filter(e -> e.key().equals(requestKey))
            .findFirst()
            .orElse(null);
    if (request == null) return;

    switch (request) {
      case SELECT_PRODUCT -> {
        final CreateQueueFragment fragment = CreateQueueResultHandler.this._fragment;
        final Long productId =
            result.getLong(SelectProductFragment.Result.SELECTED_PRODUCT_ID_LONG.key());

        //noinspection ExtractMethodRecommender
        final CompletableFuture<ProductModel> selectedProduct = new CompletableFuture<>();
        selectedProduct.thenAccept(
            product -> {
              fragment.createQueueViewModel().makeProductOrderView().onProductChanged(product);

              final boolean isEditingOrder =
                  fragment.createQueueViewModel().makeProductOrderView().productOrderToEdit()
                      != null;
              // Reopen make product order dialog if already opened before.
              if (isEditingOrder) fragment.inputProductOrder().makeProductOrder().openEditDialog();
              else fragment.inputProductOrder().makeProductOrder().openCreateDialog();
            });

        if (!productId.equals(0L)) {
          fragment
              .createQueueViewModel()
              .selectProductById(productId)
              .observe(fragment.getViewLifecycleOwner(), selectedProduct::complete);
        } else {
          selectedProduct.complete(
              fragment
                  .createQueueViewModel()
                  .makeProductOrderView()
                  .inputtedProduct()
                  .getValue()
                  .orElse(null));
        }
      }
    }
  }
}
