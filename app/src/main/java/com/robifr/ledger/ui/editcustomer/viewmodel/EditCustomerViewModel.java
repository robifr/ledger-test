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

package com.robifr.ledger.ui.editcustomer.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.createcustomer.viewmodel.CreateCustomerViewModel;
import com.robifr.ledger.ui.editcustomer.EditCustomerFragment;
import com.robifr.ledger.util.livedata.SafeEvent;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;

@HiltViewModel
public class EditCustomerViewModel extends CreateCustomerViewModel {
  @Nullable private CustomerModel _initialCustomerToEdit = null;

  @NonNull
  private final MutableLiveData<SafeEvent<Optional<Long>>> _resultEditedCustomerId =
      new MutableLiveData<>();

  @Inject
  public EditCustomerViewModel(
      @NonNull CustomerRepository customerRepository, @NonNull SavedStateHandle savedStateHandle) {
    super(customerRepository);
    Objects.requireNonNull(savedStateHandle);

    SafeEvent.observeOnce(
        // Shouldn't be null when editing data.
        this.selectCustomerById(
            Objects.requireNonNull(
                savedStateHandle.get(
                    EditCustomerFragment.Arguments.INITIAL_CUSTOMER_ID_TO_EDIT.key()))),
        customer -> {
          this._initialCustomerToEdit = customer;
          this.onNameTextChanged(customer.name());
          this.onBalanceChanged(customer.balance());
          this.onDebtChanged(customer.debt());
        },
        Objects::nonNull);
  }

  @Override
  @NonNull
  public CustomerModel inputtedCustomer() {
    final Long id =
        this._initialCustomerToEdit != null && this._initialCustomerToEdit.id() != null
            ? this._initialCustomerToEdit.id()
            : null;
    return CustomerModel.toBuilder(super.inputtedCustomer()).setId(id).build();
  }

  @Override
  public void onSave() {
    if (this.inputtedCustomer().name().isBlank()) {
      this._inputtedNameError.setValue(
          Optional.of(new StringResources.Strings(R.string.text_customer_name_is_required)));
      return;
    }

    this._updateCustomer(this.inputtedCustomer());
  }

  @NonNull
  public LiveData<SafeEvent<Optional<Long>>> resultEditedCustomerId() {
    return this._resultEditedCustomerId;
  }

  @NonNull
  public LiveData<CustomerModel> selectCustomerById(@Nullable Long customerId) {
    final MutableLiveData<CustomerModel> result = new MutableLiveData<>();

    this._customerRepository
        .selectById(customerId)
        .thenAcceptAsync(
            customer -> {
              if (customer == null) {
                this._snackbarMessage.postValue(
                    new SafeEvent<>(
                        new StringResources.Strings(
                            R.string.text_error_failed_to_find_related_customer)));
              }

              result.postValue(customer);
            });
    return result;
  }

  private void _updateCustomer(@NonNull CustomerModel customer) {
    Objects.requireNonNull(customer);

    this._customerRepository
        .update(customer)
        .thenAcceptAsync(
            effected -> {
              if (effected > 0) {
                this._resultEditedCustomerId.postValue(
                    new SafeEvent<>(Optional.ofNullable(customer.id())));
              }

              final StringResources stringRes =
                  effected > 0
                      ? new StringResources.Plurals(
                          R.plurals.args_updated_x_customer, effected, effected)
                      : new StringResources.Strings(R.string.text_error_failed_to_update_customer);
              this._snackbarMessage.postValue(new SafeEvent<>(stringRes));
            });
  }
}
