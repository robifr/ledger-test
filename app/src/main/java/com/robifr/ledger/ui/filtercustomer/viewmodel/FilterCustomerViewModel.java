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

import android.content.Context;
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
import com.robifr.ledger.ui.StringResources;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class FilterCustomerViewModel extends ViewModel {
  @NonNull private final CustomerRepository _customerRepository;
  @NonNull private final CustomerSorter _sorter = new CustomerSorter();

  @NonNull
  private final MutableLiveData<LiveDataEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull
  private final MutableLiveData<LiveDataEvent<List<Long>>> _filteredCustomerIds =
      new MutableLiveData<>();

  @NonNull private final MutableLiveData<List<CustomerModel>> _customers = new MutableLiveData<>();

  @NonNull
  private final MutableLiveData<LiveDataEvent<List<Integer>>> _addedFilteredCustomerIndexes =
      new MutableLiveData<>();

  @NonNull
  private final MutableLiveData<LiveDataEvent<List<Integer>>> _removedFilteredCustomerIndexes =
      new MutableLiveData<>();

  @NonNull private final ArrayList<CustomerModel> _filteredCustomers = new ArrayList<>();

  public FilterCustomerViewModel(@NonNull CustomerRepository customerRepository) {
    this._customerRepository = Objects.requireNonNull(customerRepository);

    this._sorter.setSortMethod(new CustomerSortMethod(CustomerSortMethod.SortBy.NAME, true));
  }

  @NonNull
  public LiveData<LiveDataEvent<StringResources>> snackbarMessage() {
    return this._snackbarMessage;
  }

  @NonNull
  public LiveData<LiveDataEvent<List<Long>>> filteredCustomerIds() {
    return this._filteredCustomerIds;
  }

  @NonNull
  public LiveData<List<CustomerModel>> customers() {
    return this._customers;
  }

  @NonNull
  public LiveData<LiveDataEvent<List<Integer>>> addedFilteredCustomerIndexes() {
    return this._addedFilteredCustomerIndexes;
  }

  @NonNull
  public LiveData<LiveDataEvent<List<Integer>>> removedFilteredCustomerIndexes() {
    return this._removedFilteredCustomerIndexes;
  }

  @NonNull
  public List<CustomerModel> filteredCustomers() {
    return Collections.unmodifiableList(this._filteredCustomers);
  }

  public void onCustomersChanged(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    this._customers.setValue(Collections.unmodifiableList(this._sorter.sort(customers)));
  }

  public void onAddFilteredCustomer(@NonNull CustomerModel... customers) {
    Objects.requireNonNull(customers);

    final ArrayList<Integer> addedIndexes = new ArrayList<>();

    for (CustomerModel customer : customers) {
      if (this._customers.getValue() == null) return;

      addedIndexes.add(this._customers.getValue().indexOf(customer));
      this._filteredCustomers.add(customer);
    }

    this._addedFilteredCustomerIndexes.setValue(new LiveDataEvent<>(addedIndexes));
  }

  public void onRemoveFilteredCustomer(@NonNull CustomerModel... customers) {
    Objects.requireNonNull(customers);

    final ArrayList<Integer> removedIndexes = new ArrayList<>();

    for (int i = customers.length; i-- > 0; ) {
      if (this._customers.getValue() == null) return;

      removedIndexes.add(this._customers.getValue().indexOf(customers[i]));
      this._filteredCustomers.remove(customers[i]);
    }

    this._removedFilteredCustomerIndexes.setValue(new LiveDataEvent<>(removedIndexes));
  }

  public void onSave() {
    final List<Long> customerIds =
        this._customers.getValue() != null
            ? this._filteredCustomers.stream().map(CustomerModel::id).collect(Collectors.toList())
            : List.of();

    this._filteredCustomerIds.setValue(new LiveDataEvent<>(customerIds));
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

      final FilterCustomerViewModel viewModel =
          new FilterCustomerViewModel(CustomerRepository.instance(this._context));
      return Objects.requireNonNull(cls.cast(viewModel));
    }
  }
}
