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

package com.robifr.ledger.ui.select_customer.view_model;

import android.content.Context;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.robifr.ledger.data.CustomerSortMethod;
import com.robifr.ledger.data.CustomerSorter;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.ui.LiveDataEvent;
import com.robifr.ledger.ui.LiveDataModelUpdater;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class SelectCustomerViewModel extends ViewModel {
  @NonNull private final CustomerRepository _customerRepository;
  @Nullable private final CustomerModel _initialSelectedCustomer;
  @NonNull private final CustomersUpdater _customersUpdater;
  @NonNull private final CustomerSorter _sorter = new CustomerSorter();

  @NonNull
  private final MutableLiveData<LiveDataEvent<String>> _snackbarMessage = new MutableLiveData<>();

  @NonNull
  private final MutableLiveData<LiveDataEvent<Long>> _selectedCustomerId = new MutableLiveData<>();

  @NonNull private final MutableLiveData<List<CustomerModel>> _customers = new MutableLiveData<>();

  public SelectCustomerViewModel(
      @NonNull CustomerRepository customerRepository,
      @Nullable CustomerModel initialSelectedCustomer) {
    this._customerRepository = Objects.requireNonNull(customerRepository);
    this._initialSelectedCustomer = initialSelectedCustomer;
    this._customersUpdater = new CustomersUpdater(this._customers);

    this._sorter.setSortMethod(new CustomerSortMethod(CustomerSortMethod.SortBy.NAME, true));
    this._customerRepository.addModelChangedListener(this._customersUpdater);
  }

  @Override
  public void onCleared() {
    this._customerRepository.removeModelChangedListener(this._customersUpdater);
  }

  @Nullable
  public CustomerModel initialSelectedCustomer() {
    return this._initialSelectedCustomer;
  }

  @NonNull
  public LiveData<LiveDataEvent<String>> snackbarMessage() {
    return this._snackbarMessage;
  }

  @NonNull
  public LiveData<LiveDataEvent<Long>> selectedCustomerId() {
    return this._selectedCustomerId;
  }

  @NonNull
  public LiveData<List<CustomerModel>> customers() {
    return this._customers;
  }

  public void onCustomersChanged(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    this._customers.setValue(Collections.unmodifiableList(this._sorter.sort(customers)));
  }

  public void onCustomerSelected(@Nullable CustomerModel customer) {
    final Long customerId = customer != null && customer.id() != null ? customer.id() : null;
    this._selectedCustomerId.setValue(new LiveDataEvent<>(customerId));
  }

  @NonNull
  public List<CustomerModel> fetchAllCustomers() {
    try {
      return this._customerRepository.selectAll().get();

    } catch (ExecutionException | InterruptedException e) {
      this._snackbarMessage.setValue(new LiveDataEvent<>("Error! Unable to obtain all customers"));
    }

    return new ArrayList<>();
  }

  public static class Factory implements ViewModelProvider.Factory {
    @NonNull private final Context _context;
    @Nullable private final CustomerModel _initialSelectedCustomer;

    public Factory(@NonNull Context context, @Nullable CustomerModel initialSelectedCustomer) {
      Objects.requireNonNull(context);

      this._context = context.getApplicationContext();
      this._initialSelectedCustomer = initialSelectedCustomer;
    }

    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> cls) {
      Objects.requireNonNull(cls);

      final SelectCustomerViewModel viewModel =
          new SelectCustomerViewModel(
              CustomerRepository.instance(this._context), this._initialSelectedCustomer);
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
      SelectCustomerViewModel.this.onCustomersChanged(customers);
    }
  }
}
