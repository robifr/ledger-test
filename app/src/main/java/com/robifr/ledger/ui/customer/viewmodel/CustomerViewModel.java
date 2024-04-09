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

package com.robifr.ledger.ui.customer.viewmodel;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.CustomerFilters;
import com.robifr.ledger.data.CustomerSortMethod;
import com.robifr.ledger.data.CustomerSorter;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.ui.LiveDataEvent;
import com.robifr.ledger.ui.LiveDataModelUpdater;
import com.robifr.ledger.ui.StringResources;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;

@HiltViewModel
public class CustomerViewModel extends ViewModel {
  @NonNull private final CustomerRepository _customerRepository;
  @NonNull private final CustomersUpdater _customersUpdater;
  @NonNull private final CustomerFilterViewModel _filterView = new CustomerFilterViewModel(this);
  @NonNull private final CustomerSorter _sorter = new CustomerSorter();

  @NonNull
  private final MutableLiveData<LiveDataEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull private final MutableLiveData<List<CustomerModel>> _customers = new MutableLiveData<>();
  @NonNull private final MutableLiveData<CustomerSortMethod> _sortMethod = new MutableLiveData<>();

  /**
   * Currently expanded customer index from {@link CustomerViewModel#_customers customers}. -1 or
   * null to represent none being expanded.
   */
  @NonNull private final MutableLiveData<Integer> _expandedCustomerIndex = new MutableLiveData<>();

  @Inject
  public CustomerViewModel(@NonNull CustomerRepository customerRepository) {
    this._customerRepository = Objects.requireNonNull(customerRepository);
    this._customersUpdater = new CustomersUpdater(this._customers);

    this._customerRepository.addModelChangedListener(this._customersUpdater);

    // It's unusual indeed to call its own method in its constructor. Setting up initial values
    // inside a fragment is painful. You have to consider whether the fragment recreated due to
    // configuration changes, or if it's popped from the backstack, or when the view model itself
    // is recreated due to the fragment being navigated by bottom navigation.
    this.onSortMethodChanged(new CustomerSortMethod(CustomerSortMethod.SortBy.NAME, true));

    final LiveData<List<CustomerModel>> selectAllCustomers = this.selectAllCustomers();
    selectAllCustomers.observeForever(
        new Observer<>() {
          @Override
          public void onChanged(List<CustomerModel> customers) {
            if (customers != null) {
              CustomerViewModel.this._filterView.onFiltersChanged(
                  CustomerFilters.toBuilder().build(), customers);
            }

            selectAllCustomers.removeObserver(this);
          }
        });
  }

  @Override
  public void onCleared() {
    this._customerRepository.removeModelChangedListener(this._customersUpdater);
  }

  @NonNull
  public CustomerFilterViewModel filterView() {
    return this._filterView;
  }

  @NonNull
  public LiveData<LiveDataEvent<StringResources>> snackbarMessage() {
    return this._snackbarMessage;
  }

  @NonNull
  public LiveData<List<CustomerModel>> customers() {
    return this._customers;
  }

  @NonNull
  public LiveData<CustomerSortMethod> sortMethod() {
    return this._sortMethod;
  }

  /**
   * @see CustomerViewModel#_expandedCustomerIndex
   */
  public LiveData<Integer> expandedCustomerIndex() {
    return this._expandedCustomerIndex;
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
                    new LiveDataEvent<>(
                        new StringResources.Strings(
                            R.string.text_error_unable_to_retrieve_all_customers)));
              }

              result.postValue(customers);
            });
    return result;
  }

  public void deleteCustomer(@NonNull CustomerModel customer) {
    Objects.requireNonNull(customer);

    this._customerRepository
        .delete(customer)
        .thenAcceptAsync(
            effected -> {
              final StringResources stringRes =
                  effected > 0
                      ? new StringResources.Plurals(
                          R.plurals.args_deleted_x_customer, effected, effected)
                      : new StringResources.Strings(R.string.text_error_failed_to_delete_customer);
              this._snackbarMessage.postValue(new LiveDataEvent<>(stringRes));
            });
  }

  public void onCustomersChanged(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    this._customers.setValue(Collections.unmodifiableList(customers));
  }

  public void onSortMethodChanged(@NonNull CustomerSortMethod sortMethod) {
    final List<CustomerModel> customers =
        Objects.requireNonNullElse(this._customers.getValue(), new ArrayList<>());
    this.onSortMethodChanged(sortMethod, customers);
  }

  public void onSortMethodChanged(
      @NonNull CustomerSortMethod sortMethod, @NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(sortMethod);
    Objects.requireNonNull(customers);

    this._sortMethod.setValue(sortMethod);
    this._sorter.setSortMethod(sortMethod);
    this.onCustomersChanged(this._sorter.sort(customers));
  }

  /**
   * @see CustomerViewModel#onSortMethodChanged(CustomerSortMethod.SortBy, List)
   */
  public void onSortMethodChanged(@NonNull CustomerSortMethod.SortBy sortBy) {
    final List<CustomerModel> customers =
        Objects.requireNonNullElse(this._customers.getValue(), new ArrayList<>());
    this.onSortMethodChanged(sortBy, customers);
  }

  /**
   * Sort {@link CustomerViewModel#customers() customers} based on specified {@link
   * CustomerSortMethod.SortBy} type. Doing so will reverse the order — Ascending becomes descending
   * and vice versa. Use {@link CustomerViewModel#onSortMethodChanged(CustomerSortMethod)} if you
   * want to apply the order by yourself.
   */
  public void onSortMethodChanged(
      @NonNull CustomerSortMethod.SortBy sortBy, @NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(sortBy);
    Objects.requireNonNull(customers);

    final CustomerSortMethod sortMethod = this._sortMethod.getValue();
    if (sortMethod == null) return;

    // Reverse sort order when selecting same sort option.
    final boolean isAscending =
        sortMethod.sortBy() == sortBy ? !sortMethod.isAscending() : sortMethod.isAscending();

    this.onSortMethodChanged(new CustomerSortMethod(sortBy, isAscending), customers);
  }

  public void onExpandedCustomerIndexChanged(int index) {
    this._expandedCustomerIndex.setValue(index);
  }

  private class CustomersUpdater extends LiveDataModelUpdater<CustomerModel> {
    public CustomersUpdater(@NonNull MutableLiveData<List<CustomerModel>> customers) {
      super(customers);
    }

    @Override
    @MainThread
    public void onUpdateLiveData(@NonNull List<CustomerModel> customers) {
      CustomerViewModel.this._filterView.onFiltersChanged(
          CustomerViewModel.this._filterView.inputtedFilters(), customers);
    }
  }
}
