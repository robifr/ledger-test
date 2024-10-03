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

package com.robifr.ledger.ui.filtercustomer.viewmodel;

import androidx.annotation.NonNull;
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
import com.robifr.ledger.ui.filtercustomer.FilterCustomerFragment;
import com.robifr.ledger.util.livedata.SafeEvent;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;

@HiltViewModel
public class FilterCustomerViewModel extends ViewModel {
  @NonNull private final CustomerRepository _customerRepository;
  @NonNull private final CustomerSorter _sorter = new CustomerSorter();

  @NonNull
  private final SafeMutableLiveData<List<CustomerModel>> _filteredCustomers =
      new SafeMutableLiveData<>(List.of());

  @NonNull
  private final MutableLiveData<SafeEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull
  private final SafeMutableLiveData<List<CustomerModel>> _customers =
      new SafeMutableLiveData<>(List.of());

  /**
   * Currently expanded customer index from {@link #_customers customers}. -1 to represent none
   * being expanded.
   */
  @NonNull
  private final SafeMutableLiveData<Integer> _expandedCustomerIndex = new SafeMutableLiveData<>(-1);

  @NonNull
  private final MutableLiveData<SafeEvent<List<Long>>> _resultFilteredCustomerIds =
      new MutableLiveData<>();

  @Inject
  public FilterCustomerViewModel(
      @NonNull CustomerRepository customerRepository, @NonNull SavedStateHandle savedStateHandle) {
    Objects.requireNonNull(savedStateHandle);

    this._customerRepository = Objects.requireNonNull(customerRepository);

    this._sorter.setSortMethod(new CustomerSortMethod(CustomerSortMethod.SortBy.NAME, true));

    // Setting up initial values inside a fragment is painful. See commit d5604599.
    SafeEvent.observeOnce(
        this.selectAllCustomers(),
        customers -> {
          final long[] filteredCustomerIds =
              Objects.requireNonNullElse(
                  savedStateHandle.get(
                      FilterCustomerFragment.Arguments.INITIAL_FILTERED_CUSTOMER_IDS_LONG_ARRAY
                          .key()),
                  new long[] {});
          final List<CustomerModel> filteredCustomers =
              customers.stream()
                  .filter(
                      customer ->
                          Arrays.stream(filteredCustomerIds)
                              .boxed()
                              .anyMatch(id -> customer.id() != null && customer.id().equals(id)))
                  .collect(Collectors.toList());

          this.onCustomersChanged(customers);
          this.onFilteredCustomersChanged(filteredCustomers);
        },
        Objects::nonNull);
  }

  @NonNull
  public SafeLiveData<List<CustomerModel>> filteredCustomers() {
    return this._filteredCustomers;
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
   * @see #_expandedCustomerIndex
   */
  public SafeLiveData<Integer> expandedCustomerIndex() {
    return this._expandedCustomerIndex;
  }

  @NonNull
  public LiveData<SafeEvent<List<Long>>> resultFilteredCustomerIds() {
    return this._resultFilteredCustomerIds;
  }

  public void onCustomersChanged(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    this._customers.setValue(Collections.unmodifiableList(this._sorter.sort(customers)));
  }

  public void onExpandedCustomerIndexChanged(int index) {
    this._expandedCustomerIndex.setValue(index);
  }

  public void onFilteredCustomersChanged(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    this._filteredCustomers.setValue(Collections.unmodifiableList(customers));
  }

  public void onSave() {
    final List<Long> customerIds =
        this._filteredCustomers.getValue().stream()
            .map(CustomerModel::id)
            .collect(Collectors.toList());

    this._resultFilteredCustomerIds.setValue(new SafeEvent<>(customerIds));
  }

  @NonNull
  public LiveData<List<CustomerModel>> selectAllCustomers() {
    final MutableLiveData<List<CustomerModel>> result = new MutableLiveData<>();

    this._customerRepository
        .selectAll()
        .thenAcceptAsync(
            customers -> {
              if (customers == null) {
                this._snackbarMessage.postValue(
                    new SafeEvent<>(
                        new StringResources.Strings(
                            R.string.filterCustomer_fetchAllCustomerError)));
              }

              result.postValue(customers);
            });
    return result;
  }
}
