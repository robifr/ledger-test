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

package com.robifr.ledger.ui.selectcustomer;

import android.os.Bundle;
import androidx.annotation.NonNull;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.ui.searchcustomer.SearchCustomerFragment;
import com.robifr.ledger.ui.selectcustomer.viewmodel.SelectCustomerViewModel;
import java.util.Arrays;
import java.util.Objects;

public class SelectCustomerResultHandler {
  @NonNull private final SelectCustomerFragment _fragment;

  public SelectCustomerResultHandler(@NonNull SelectCustomerFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);

    this._fragment
        .getParentFragmentManager()
        .setFragmentResultListener(
            SearchCustomerFragment.Request.SELECT_CUSTOMER.key(),
            this._fragment.getViewLifecycleOwner(),
            this::onSearchCustomerResult);
  }

  public void onSearchCustomerResult(@NonNull String requestKey, @NonNull Bundle result) {
    Objects.requireNonNull(requestKey);
    Objects.requireNonNull(result);

    final SearchCustomerFragment.Request request =
        Arrays.stream(SearchCustomerFragment.Request.values())
            .filter(e -> e.key().equals(requestKey))
            .findFirst()
            .orElse(null);
    if (request == null) return;

    switch (request) {
      case SELECT_CUSTOMER -> {
        final SelectCustomerViewModel viewModel =
            SelectCustomerResultHandler.this._fragment.selectCustomerViewModel();
        final Long customerId =
            result.getLong(SearchCustomerFragment.Result.SELECTED_CUSTOMER_ID.key());
        final CustomerModel selectedCustomer =
            !customerId.equals(0L)
                ? viewModel.customers().getValue().stream()
                    .filter(customer -> customer.id() != null && customer.id().equals(customerId))
                    .findFirst()
                    .orElse(null)
                : null;

        if (selectedCustomer != null) viewModel.onCustomerSelected(selectedCustomer);
      }
    }
  }
}
