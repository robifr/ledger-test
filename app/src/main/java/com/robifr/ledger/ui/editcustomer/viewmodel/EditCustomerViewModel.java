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

    // Setting up initial values inside a fragment is painful. See commit d5604599.
    SafeEvent.observeOnce(
        // Shouldn't be null when editing data.
        this.selectCustomerById(
            Objects.requireNonNull(
                savedStateHandle.get(
                    EditCustomerFragment.Arguments.INITIAL_CUSTOMER_ID_TO_EDIT_LONG.key()))),
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
          Optional.of(new StringResources.Strings(R.string.createCustomer_name_emptyError)));
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
                        new StringResources.Strings(R.string.createCustomer_fetchCustomerError)));
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
                          R.plurals.createCustomer_updated_n_customer, effected, effected)
                      : new StringResources.Strings(R.string.createCustomer_updateCustomerError);
              this._snackbarMessage.postValue(new SafeEvent<>(stringRes));
            });
  }
}
