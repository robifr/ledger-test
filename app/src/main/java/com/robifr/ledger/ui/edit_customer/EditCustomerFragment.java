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

package com.robifr.ledger.ui.edit_customer;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.lifecycle.ViewModelProvider;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.ui.FragmentResultKey;
import com.robifr.ledger.ui.create_customer.CreateCustomerFragment;
import com.robifr.ledger.ui.edit_customer.view_model.EditCustomerViewModel;
import java.util.Objects;

public class EditCustomerFragment extends CreateCustomerFragment {
  public enum Request implements FragmentResultKey {
    EDIT_CUSTOMER;

    @Override
    @NonNull
    public String key() {
      return FragmentResultKey.generateKey(this);
    }
  }

  public enum Result implements FragmentResultKey {
    EDITED_CUSTOMER_ID;

    @Override
    @NonNull
    public String key() {
      return FragmentResultKey.generateKey(this);
    }
  }

  @NonNull private final Long _initialCustomerIdToEdit;

  /** Default constructor when configuration changes. */
  public EditCustomerFragment() {
    this(0L);
  }

  private EditCustomerFragment(@NonNull Long initialCustomerIdToEdit) {
    this._initialCustomerIdToEdit = Objects.requireNonNull(initialCustomerIdToEdit);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
    super.onViewCreated(view, savedInstance);
    Objects.requireNonNull(this._fragmentBinding);

    this._createCustomerViewModel =
        new ViewModelProvider(this, new EditCustomerViewModel.Factory(this.requireContext()))
            .get(EditCustomerViewModel.class);
    this._viewModelHandler =
        new EditCustomerViewModelHandler(
            this, (EditCustomerViewModel) this._createCustomerViewModel);

    this._fragmentBinding.toolbar.setTitle("Edit customer");

    if (this._createCustomerViewModel instanceof EditCustomerViewModel editCustomerViewModel) {
      final CustomerModel initialCustomer =
          editCustomerViewModel.selectCustomerById(this._initialCustomerIdToEdit);
      Objects.requireNonNull(initialCustomer); // Logically shouldn't be null when editing data.

      editCustomerViewModel.setInitialCustomerToEdit(initialCustomer);
      editCustomerViewModel.onNameTextChanged(initialCustomer.name());
      editCustomerViewModel.onBalanceChanged(initialCustomer.balance());
      editCustomerViewModel.onDebtChanged(initialCustomer.debt());
    }
  }

  public static class Factory extends FragmentFactory {
    @NonNull private final Long _initialCustomerIdToEdit;

    public Factory(@NonNull Long initialCustomerIdToEdit) {
      this._initialCustomerIdToEdit = Objects.requireNonNull(initialCustomerIdToEdit);
    }

    @Override
    @NonNull
    public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
      Objects.requireNonNull(classLoader);
      Objects.requireNonNull(className);

      return (className.equals(EditCustomerFragment.class.getName()))
          ? new EditCustomerFragment(this._initialCustomerIdToEdit)
          : super.instantiate(classLoader, className);
    }
  }
}
