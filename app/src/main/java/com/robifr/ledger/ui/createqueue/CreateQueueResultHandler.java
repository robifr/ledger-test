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

package com.robifr.ledger.ui.createqueue;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentResultListener;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.data.model.ProductModel;
import com.robifr.ledger.ui.createqueue.viewmodel.CreateQueueViewModel;
import com.robifr.ledger.ui.selectcustomer.SelectCustomerFragment;
import com.robifr.ledger.ui.selectproduct.SelectProductFragment;
import java.util.Arrays;
import java.util.Objects;

public class CreateQueueResultHandler {
  @NonNull private final CreateQueueFragment _fragment;

  public CreateQueueResultHandler(@NonNull CreateQueueFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);

    this._fragment
        .getParentFragmentManager()
        .setFragmentResultListener(
            SelectCustomerFragment.Request.SELECT_CUSTOMER.key(),
            this._fragment.getViewLifecycleOwner(),
            new SelectCustomerResultListener());
    this._fragment
        .getParentFragmentManager()
        .setFragmentResultListener(
            SelectProductFragment.Request.SELECT_PRODUCT.key(),
            this._fragment.getViewLifecycleOwner(),
            new SelectProductResultListener());
  }

  private class SelectCustomerResultListener implements FragmentResultListener {
    @Override
    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
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
              result.getLong(SelectCustomerFragment.Result.SELECTED_CUSTOMER_ID.key());
          final CustomerModel inputtedCustomer =
              !customerId.equals(0L)
                  ? viewModel.selectCustomerById(customerId)
                  : viewModel.inputtedCustomer().getValue();

          viewModel.onCustomerChanged(inputtedCustomer);
        }
      }
    }
  }

  private class SelectProductResultListener implements FragmentResultListener {
    @Override
    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
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
              result.getLong(SelectProductFragment.Result.SELECTED_PRODUCT_ID.key());
          final ProductModel selectedProduct =
              !productId.equals(0L)
                  ? fragment.createQueueViewModel().selectProductById(productId)
                  : fragment
                      .createQueueViewModel()
                      .makeProductOrderView()
                      .inputtedProduct()
                      .getValue();
          final boolean isEditingOrder =
              fragment.createQueueViewModel().makeProductOrderView().productOrderToEdit() != null;

          fragment.createQueueViewModel().makeProductOrderView().onProductChanged(selectedProduct);

          // Reopen make product order dialog if already opened before.
          if (isEditingOrder) fragment.inputProductOrder().makeProductOrder().openEditDialog();
          else fragment.inputProductOrder().makeProductOrder().openCreateDialog();
        }
      }
    }
  }
}
