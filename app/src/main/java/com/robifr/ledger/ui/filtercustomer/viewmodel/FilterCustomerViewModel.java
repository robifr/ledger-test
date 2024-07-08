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
                      FilterCustomerFragment.Arguments.INITIAL_FILTERED_CUSTOMER_IDS.key()),
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

  @NonNull
  public LiveData<SafeEvent<List<Long>>> resultFilteredCustomerIds() {
    return this._resultFilteredCustomerIds;
  }

  public void onCustomersChanged(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    this._customers.setValue(Collections.unmodifiableList(this._sorter.sort(customers)));
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
                            R.string.text_error_unable_to_retrieve_all_customers)));
              }

              result.postValue(customers);
            });
    return result;
  }
}
