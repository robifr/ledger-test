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

package com.robifr.ledger.ui.main.customer.view_model;

import android.content.Context;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.robifr.ledger.R;
import com.robifr.ledger.data.CustomerSortMethod;
import com.robifr.ledger.data.CustomerSorter;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.ui.LiveDataEvent;
import com.robifr.ledger.ui.LiveDataModelUpdater;
import com.robifr.ledger.ui.StringResources;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

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

  public CustomerViewModel(@NonNull CustomerRepository customerRepository) {
    this._customerRepository = Objects.requireNonNull(customerRepository);
    this._customersUpdater = new CustomersUpdater(this._customers);

    this._customerRepository.addModelChangedListener(this._customersUpdater);
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

  @NonNull
  public List<CustomerModel> fetchAllCustomers() {
    try {
      return this._customerRepository.selectAll().get();

    } catch (ExecutionException | InterruptedException e) {
      this._snackbarMessage.setValue(
          new LiveDataEvent<>(
              new StringResources.Strings(R.string.text_error_unable_to_retrieve_all_customers)));
    }

    return new ArrayList<>();
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
                          R.plurals.args_customer_deleted, effected, effected)
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

  public static class Factory implements ViewModelProvider.Factory {
    @NonNull private final Context _context;

    public Factory(@NonNull Context context) {
      Objects.requireNonNull(context);

      this._context = context.getApplicationContext();
    }

    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> cls) {
      Objects.requireNonNull(cls);

      final CustomerViewModel viewModel =
          new CustomerViewModel(CustomerRepository.instance(this._context));
      return Objects.requireNonNull(cls.cast(viewModel));
    }
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
