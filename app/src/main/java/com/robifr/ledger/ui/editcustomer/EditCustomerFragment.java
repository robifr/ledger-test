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
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.robifr.ledger.R;
import com.robifr.ledger.ui.FragmentResultKey;
import com.robifr.ledger.ui.createcustomer.CreateCustomerFragment;
import com.robifr.ledger.ui.editcustomer.viewmodel.EditCustomerViewModel;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Objects;

@AndroidEntryPoint
public class EditCustomerFragment extends CreateCustomerFragment {
  public enum Arguments implements FragmentResultKey {
    INITIAL_CUSTOMER_ID_TO_EDIT_LONG
  }

  public enum Request implements FragmentResultKey {
    EDIT_CUSTOMER
  }

  public enum Result implements FragmentResultKey {
    EDITED_CUSTOMER_ID_LONG
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
    super.onViewCreated(view, savedInstance);
    Objects.requireNonNull(this._fragmentBinding);

    this._createCustomerViewModel = new ViewModelProvider(this).get(EditCustomerViewModel.class);
    this._viewModelHandler =
        new EditCustomerViewModelHandler(
            this, (EditCustomerViewModel) this._createCustomerViewModel);

    this._fragmentBinding.toolbar.setTitle(R.string.editCustomer);
  }
}
