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

package com.robifr.ledger.ui.createcustomer.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.ui.LiveDataEvent;
import com.robifr.ledger.ui.StringResources;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.math.BigDecimal;
import java.util.Objects;
import javax.inject.Inject;

@HiltViewModel
public class CreateCustomerViewModel extends ViewModel {
  @NonNull protected final CustomerRepository _customerRepository;

  @NonNull
  protected final CustomerBalanceViewModel _balanceView = new CustomerBalanceViewModel(this);

  @NonNull
  protected final MutableLiveData<LiveDataEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull
  protected final MutableLiveData<LiveDataEvent<StringResources>> _inputtedNameError =
      new MutableLiveData<>();

  @NonNull protected final MutableLiveData<String> _inputtedNameText = new MutableLiveData<>();
  @NonNull protected final MutableLiveData<Long> _inputtedBalance = new MutableLiveData<>();
  @NonNull protected final MutableLiveData<BigDecimal> _inputtedDebt = new MutableLiveData<>();

  @NonNull
  private final MutableLiveData<LiveDataEvent<Long>> _createdCustomerId = new MutableLiveData<>();

  @Inject
  public CreateCustomerViewModel(@NonNull CustomerRepository customerRepository) {
    this._customerRepository = Objects.requireNonNull(customerRepository);
  }

  public CustomerBalanceViewModel balanceView() {
    return this._balanceView;
  }

  @NonNull
  public LiveData<LiveDataEvent<StringResources>> snackbarMessage() {
    return this._snackbarMessage;
  }

  @NonNull
  public LiveData<LiveDataEvent<StringResources>> inputtedNameError() {
    return this._inputtedNameError;
  }

  @NonNull
  public LiveData<LiveDataEvent<Long>> createdCustomerId() {
    return this._createdCustomerId;
  }

  @NonNull
  public LiveData<String> inputtedNameText() {
    return this._inputtedNameText;
  }

  @NonNull
  public LiveData<Long> inputtedBalance() {
    return this._inputtedBalance;
  }

  @NonNull
  public LiveData<BigDecimal> inputtedDebt() {
    return this._inputtedDebt;
  }

  /**
   * Get current inputted customer from any corresponding inputted live data. If any live data is
   * set using {@link MutableLiveData#postValue(Object)}, calling this method may not immediately
   * reflect the latest changes. For accurate results in asynchronous operations, consider calling
   * this method inside {@link Observer}.
   */
  @NonNull
  public CustomerModel inputtedCustomer() {
    final CustomerModel defaultCustomer = CustomerModel.toBuilder().setName("").build();

    final String name =
        Objects.requireNonNullElse(this._inputtedNameText.getValue(), defaultCustomer.name());
    final long balance =
        Objects.requireNonNullElse(this._inputtedBalance.getValue(), defaultCustomer.balance());
    final BigDecimal debt =
        Objects.requireNonNullElse(this._inputtedDebt.getValue(), defaultCustomer.debt());

    return CustomerModel.toBuilder().setName(name).setBalance(balance).setDebt(debt).build();
  }

  public void onNameTextChanged(@NonNull String name) {
    Objects.requireNonNull(name);

    this._inputtedNameText.setValue(name);

    // Disable error when name field filled.
    if (!name.isBlank()) this._inputtedNameError.setValue(new LiveDataEvent<>(null));
  }

  public void onBalanceChanged(long balance) {
    this._inputtedBalance.setValue(balance);
  }

  public void onDebtChanged(@NonNull BigDecimal debt) {
    Objects.requireNonNull(debt);

    this._inputtedDebt.setValue(debt);
  }

  public void onSave() {
    if (this.inputtedCustomer().name().isBlank()) {
      this._inputtedNameError.setValue(
          new LiveDataEvent<>(
              new StringResources.Strings(R.string.text_customer_name_is_required)));
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
              if (id != 0L) this._createdCustomerId.postValue(new LiveDataEvent<>(id));

              final StringResources stringRes =
                  id != 0L
                      ? new StringResources.Plurals(R.plurals.args_added_x_customer, 1, 1)
                      : new StringResources.Strings(R.string.text_error_failed_to_add_customer);
              this._snackbarMessage.postValue(new LiveDataEvent<>(stringRes));
            });
  }
}
