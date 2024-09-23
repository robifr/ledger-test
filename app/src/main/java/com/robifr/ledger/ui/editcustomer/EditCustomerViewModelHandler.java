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
          bundle.putLong(EditCustomerFragment.Result.EDITED_CUSTOMER_ID_LONG.key(), id);

          this._fragment
              .getParentFragmentManager()
              .setFragmentResult(EditCustomerFragment.Request.EDIT_CUSTOMER.key(), bundle);
        });
    this._fragment.finish();
  }
}
