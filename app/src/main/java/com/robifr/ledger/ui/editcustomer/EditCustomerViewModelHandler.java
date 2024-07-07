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

package com.robifr.ledger.ui.editcustomer;

import android.os.Bundle;
import androidx.annotation.NonNull;
import com.robifr.ledger.ui.createcustomer.CreateCustomerViewModelHandler;
import com.robifr.ledger.ui.editcustomer.viewmodel.EditCustomerViewModel;
import java.util.Objects;
import java.util.Optional;

public class EditCustomerViewModelHandler extends CreateCustomerViewModelHandler {
  public EditCustomerViewModelHandler(
      @NonNull EditCustomerFragment fragment, @NonNull EditCustomerViewModel viewModel) {
    super(fragment, viewModel);
    viewModel
        .resultEditedCustomerId()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onResultEditedCustomerId));
  }

  /**
   * @noinspection OptionalUsedAsFieldOrParameterType
   */
  private void _onResultEditedCustomerId(@NonNull Optional<Long> customerId) {
    Objects.requireNonNull(customerId);

    customerId.ifPresent(
        id -> {
          final Bundle bundle = new Bundle();
          bundle.putLong(EditCustomerFragment.Result.EDITED_CUSTOMER_ID.key(), id);

          this._fragment
              .getParentFragmentManager()
              .setFragmentResult(EditCustomerFragment.Request.EDIT_CUSTOMER.key(), bundle);
        });
    this._fragment.finish();
  }
}
