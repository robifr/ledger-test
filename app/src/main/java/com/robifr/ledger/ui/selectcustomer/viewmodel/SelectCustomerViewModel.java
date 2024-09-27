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

package com.robifr.ledger.ui.selectcustomer.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.display.CustomerSortMethod;
import com.robifr.ledger.data.display.CustomerSorter;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.selectcustomer.SelectCustomerFragment;
import com.robifr.ledger.util.livedata.SafeEvent;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;

@HiltViewModel
public class SelectCustomerViewModel extends ViewModel {
  @NonNull private final CustomerRepository _customerRepository;

  @NonNull
  private final CustomerChangedListener _customerChangedListener =
      new CustomerChangedListener(this);

  @NonNull private final CustomerSorter _sorter = new CustomerSorter();
  @Nullable private final CustomerModel _initialSelectedCustomer;

  @NonNull
  private final MutableLiveData<SafeEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull
  private final SafeMutableLiveData<List<CustomerModel>> _customers =
      new SafeMutableLiveData<>(List.of());

  /** Whether the preview of selected customer is expanded or not. */
  @NonNull
  private final SafeMutableLiveData<Boolean> _isSelectedCustomerExpanded =
      new SafeMutableLiveData<>(false);

  /**
   * Currently expanded customer index from {@link #_customers}. -1 to represent none being
   * expanded.
   */
  @NonNull
  private final SafeMutableLiveData<Integer> _expandedCustomerIndex = new SafeMutableLiveData<>(-1);

  @NonNull
  private final MutableLiveData<SafeEvent<Optional<Long>>> _resultSelectedCustomerId =
      new MutableLiveData<>();

  @Inject
  public SelectCustomerViewModel(
      @NonNull CustomerRepository customerRepository, @NonNull SavedStateHandle savedStateHandle) {
    Objects.requireNonNull(savedStateHandle);

    this._customerRepository = Objects.requireNonNull(customerRepository);
    this._initialSelectedCustomer =
        savedStateHandle.get(
            SelectCustomerFragment.Arguments.INITIAL_SELECTED_CUSTOMER_PARCELABLE.key());

    this._sorter.setSortMethod(new CustomerSortMethod(CustomerSortMethod.SortBy.NAME, true));
    this._customerRepository.addModelChangedListener(this._customerChangedListener);

    // Setting up initial values inside a fragment is painful. See commit d5604599.
    SafeEvent.observeOnce(this._selectAllCustomers(), this::_onCustomersChanged, Objects::nonNull);
  }

  @Override
  public void onCleared() {
    this._customerRepository.removeModelChangedListener(this._customerChangedListener);
  }

  @Nullable
  public CustomerModel initialSelectedCustomer() {
    return this._initialSelectedCustomer;
  }

  @NonNull
  public LiveData<SafeEvent<StringResources>> snackbarMessage() {
    return this._snackbarMessage;
  }

  @NonNull
  public SafeLiveData<List<CustomerModel>> customers() {
    return this._customers;
  }

  /**
   * @see #_isSelectedCustomerExpanded
   */
  public SafeLiveData<Boolean> isSelectedCustomerExpanded() {
    return this._isSelectedCustomerExpanded;
  }

  /**
   * @see #_expandedCustomerIndex
   */
  public SafeLiveData<Integer> expandedCustomerIndex() {
    return this._expandedCustomerIndex;
  }

  @NonNull
  public LiveData<SafeEvent<Optional<Long>>> resultSelectedCustomerId() {
    return this._resultSelectedCustomerId;
  }

  public void onCustomerSelected(@Nullable CustomerModel customer) {
    this._resultSelectedCustomerId.setValue(
        new SafeEvent<>(Optional.ofNullable(customer).map(CustomerModel::id)));
  }

  public void onSelectedCustomerExpanded(boolean isExpanded) {
    this._isSelectedCustomerExpanded.setValue(isExpanded);
  }

  public void onExpandedCustomerIndexChanged(int index) {
    this._expandedCustomerIndex.setValue(index);
  }

  @NonNull
  private LiveData<List<CustomerModel>> _selectAllCustomers() {
    final MutableLiveData<List<CustomerModel>> result = new MutableLiveData<>();

    this._customerRepository
        .selectAll()
        .thenAcceptAsync(
            customers -> {
              if (customers == null) {
                this._snackbarMessage.postValue(
                    new SafeEvent<>(
                        new StringResources.Strings(
                            R.string.text_error_unable_to_retrieve_all_customers)));
              }

              result.postValue(customers);
            });
    return result;
  }

  void _onCustomersChanged(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    this._customers.setValue(Collections.unmodifiableList(this._sorter.sort(customers)));
  }
}
