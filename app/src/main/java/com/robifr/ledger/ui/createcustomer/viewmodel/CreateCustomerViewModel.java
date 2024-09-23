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

package com.robifr.ledger.ui.createcustomer.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.util.livedata.SafeEvent;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;

@HiltViewModel
public class CreateCustomerViewModel extends ViewModel {
  @NonNull protected final CustomerRepository _customerRepository;

  @NonNull
  protected final CustomerBalanceViewModel _balanceView = new CustomerBalanceViewModel(this);

  @NonNull
  protected final MutableLiveData<SafeEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull
  protected final SafeMutableLiveData<Optional<StringResources>> _inputtedNameError =
      new SafeMutableLiveData<>(Optional.empty());

  @NonNull
  protected final SafeMutableLiveData<String> _inputtedNameText = new SafeMutableLiveData<>("");

  @NonNull
  protected final SafeMutableLiveData<Long> _inputtedBalance = new SafeMutableLiveData<>(0L);

  @NonNull
  protected final SafeMutableLiveData<BigDecimal> _inputtedDebt =
      new SafeMutableLiveData<>(BigDecimal.ZERO);

  @NonNull
  private final MutableLiveData<SafeEvent<Optional<Long>>> _resultCreatedCustomerId =
      new MutableLiveData<>();

  @Inject
  public CreateCustomerViewModel(@NonNull CustomerRepository customerRepository) {
    this._customerRepository = Objects.requireNonNull(customerRepository);
  }

  public CustomerBalanceViewModel balanceView() {
    return this._balanceView;
  }

  @NonNull
  public LiveData<SafeEvent<StringResources>> snackbarMessage() {
    return this._snackbarMessage;
  }

  @NonNull
  public SafeLiveData<Optional<StringResources>> inputtedNameError() {
    return this._inputtedNameError;
  }

  @NonNull
  public SafeLiveData<String> inputtedNameText() {
    return this._inputtedNameText;
  }

  @NonNull
  public SafeLiveData<Long> inputtedBalance() {
    return this._inputtedBalance;
  }

  @NonNull
  public SafeLiveData<BigDecimal> inputtedDebt() {
    return this._inputtedDebt;
  }

  @NonNull
  public LiveData<SafeEvent<Optional<Long>>> resultCreatedCustomerId() {
    return this._resultCreatedCustomerId;
  }

  /**
   * Get current inputted customer from any corresponding inputted live data. If any live data is
   * set using {@link MutableLiveData#postValue(Object)}, calling this method may not immediately
   * reflect the latest changes. For accurate results in asynchronous operations, consider calling
   * this method inside {@link Observer}.
   */
  @NonNull
  public CustomerModel inputtedCustomer() {
    return CustomerModel.toBuilder()
        .setName(this._inputtedNameText.getValue())
        .setBalance(this._inputtedBalance.getValue())
        .setDebt(this._inputtedDebt.getValue())
        .build();
  }

  public void onNameTextChanged(@NonNull String name) {
    Objects.requireNonNull(name);

    this._inputtedNameText.setValue(name);

    // Disable error when name field filled.
    if (!name.isBlank()) this._inputtedNameError.setValue(Optional.empty());
  }

  public void onBalanceChanged(long balance) {
    this._inputtedBalance.setValue(balance);
  }

  public void onDebtChanged(@NonNull BigDecimal debt) {
    Objects.requireNonNull(debt);

    this._inputtedDebt.setValue(debt);
  }

  public void onSave() {
    if (this._inputtedNameText.getValue().isBlank()) {
      this._inputtedNameError.setValue(
          Optional.of(new StringResources.Strings(R.string.text_customer_name_is_required)));
      return;
    }

    this._addCustomer(this.inputtedCustomer());
  }

  private void _addCustomer(@NonNull CustomerModel customer) {
    Objects.requireNonNull(customer);

    this._customerRepository
        .add(customer)
        .thenAcceptAsync(
            id -> {
              if (id != 0L) {
                this._resultCreatedCustomerId.postValue(new SafeEvent<>(Optional.of(id)));
              }

              final StringResources stringRes =
                  id != 0L
                      ? new StringResources.Plurals(R.plurals.args_added_x_customer, 1, 1)
                      : new StringResources.Strings(R.string.text_error_failed_to_add_customer);
              this._snackbarMessage.postValue(new SafeEvent<>(stringRes));
            });
  }
}
