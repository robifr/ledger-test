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

package com.robifr.ledger.ui.dashboard.viewmodel;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerBalanceInfo;
import com.robifr.ledger.data.model.CustomerDebtInfo;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.repository.ModelChangedListener;
import com.robifr.ledger.repository.ProductRepository;
import com.robifr.ledger.ui.LiveDataEvent;
import com.robifr.ledger.ui.StringResources;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;

@HiltViewModel
public class DashboardViewModel extends ViewModel {
  @NonNull private final CustomerRepository _customerRepository;
  @NonNull private final ProductRepository _productRepository;
  @NonNull private final CustomerInfoUpdater _balanceInfoUpdater = new CustomerInfoUpdater();

  @NonNull
  private final MutableLiveData<LiveDataEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull private final MutableLiveData<List<CustomerBalanceInfo>> _customersWithBalance;
  @NonNull private final MutableLiveData<List<CustomerDebtInfo>> _customersWithDebt;

  @Inject
  public DashboardViewModel(
      @NonNull CustomerRepository customerRepository,
      @NonNull ProductRepository productRepository) {
    this._customerRepository = Objects.requireNonNull(customerRepository);
    this._productRepository = Objects.requireNonNull(productRepository);

    this._customerRepository.addModelChangedListener(this._balanceInfoUpdater);

    // It's unusual indeed to call its own method in its constructor. Setting up initial values
    // inside a fragment is painful. You have to consider whether the fragment recreated due to
    // configuration changes, or if it's popped from the backstack, or when the view model itself
    // is recreated due to the fragment being navigated by bottom navigation.
    this._customersWithBalance =
        (MutableLiveData<List<CustomerBalanceInfo>>) this.selectAllIdsWithBalance();
    this._customersWithDebt = (MutableLiveData<List<CustomerDebtInfo>>) this.selectAllIdsWithDebt();
  }

  @Override
  public void onCleared() {
    this._customerRepository.removeModelChangedListener(this._balanceInfoUpdater);
  }

  @NonNull
  public LiveData<LiveDataEvent<StringResources>> snackbarMessage() {
    return this._snackbarMessage;
  }

  @NonNull
  public LiveData<List<CustomerBalanceInfo>> customersWithBalance() {
    return this._customersWithBalance;
  }

  @NonNull
  public LiveData<List<CustomerDebtInfo>> customersWithDebt() {
    return this._customersWithDebt;
  }

  @NonNull
  public LiveData<List<CustomerBalanceInfo>> selectAllIdsWithBalance() {
    final MutableLiveData<List<CustomerBalanceInfo>> result = new MutableLiveData<>();

    this._customerRepository
        .selectAllIdsWithBalance()
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

  @NonNull
  public LiveData<List<CustomerDebtInfo>> selectAllIdsWithDebt() {
    final MutableLiveData<List<CustomerDebtInfo>> result = new MutableLiveData<>();

    this._customerRepository
        .selectAllIdsWithDebt()
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

  private void _onCustomersWithBalanceChanged(@NonNull List<CustomerBalanceInfo> balanceInfo) {
    Objects.requireNonNull(balanceInfo);

    this._customersWithBalance.setValue(Collections.unmodifiableList(balanceInfo));
  }

  private void _onCustomersWithDebtChanged(@NonNull List<CustomerDebtInfo> debtInfo) {
    Objects.requireNonNull(debtInfo);

    this._customersWithDebt.setValue(Collections.unmodifiableList(debtInfo));
  }

  private class CustomerInfoUpdater implements ModelChangedListener<CustomerModel> {
    @Override
    @WorkerThread
    public void onModelUpdated(@NonNull List<CustomerModel> customers) {
      Objects.requireNonNull(customers);

      new Handler(Looper.getMainLooper())
          .post(
              () -> {
                DashboardViewModel.this._onCustomersWithBalanceChanged(
                    this._onUpdateBalanceInfo(customers));
                DashboardViewModel.this._onCustomersWithDebtChanged(
                    this._onUpdateDebtInfo(customers));
              });
    }

    @Override
    @WorkerThread
    public void onModelAdded(@NonNull List<CustomerModel> customers) {
      Objects.requireNonNull(customers);

      new Handler(Looper.getMainLooper())
          .post(
              () -> {
                DashboardViewModel.this._onCustomersWithBalanceChanged(
                    this._onAddBalanceInfo(customers));
                DashboardViewModel.this._onCustomersWithDebtChanged(this._onAddDebtInfo(customers));
              });
    }

    @Override
    @WorkerThread
    public void onModelDeleted(@NonNull List<CustomerModel> customers) {
      Objects.requireNonNull(customers);

      new Handler(Looper.getMainLooper())
          .post(
              () -> {
                DashboardViewModel.this._onCustomersWithBalanceChanged(
                    this._onRemoveBalanceInfo(customers));
                DashboardViewModel.this._onCustomersWithDebtChanged(
                    this._onRemoveDebtInfo(customers));
              });
    }

    @Override
    @WorkerThread
    public void onModelUpserted(@NonNull List<CustomerModel> customers) {}

    @NonNull
    private List<CustomerBalanceInfo> _onUpdateBalanceInfo(@NonNull List<CustomerModel> customers) {
      Objects.requireNonNull(customers);

      final ArrayList<CustomerBalanceInfo> currentInfo =
          DashboardViewModel.this._customersWithBalance.getValue() != null
              ? new ArrayList<>(DashboardViewModel.this._customersWithBalance.getValue())
              : new ArrayList<>();
      final List<CustomerBalanceInfo> customerInfo =
          customers.stream()
              .map(customer -> new CustomerBalanceInfo(customer.id(), customer.balance()))
              .collect(Collectors.toList());
      final HashMap<Long, CustomerBalanceInfo> filteredInfo = new HashMap<>();

      currentInfo.forEach(info -> filteredInfo.put(info.id(), info));
      customerInfo.forEach(info -> filteredInfo.put(info.id(), info)); // Override duplicate ID.
      filteredInfo.values().removeIf(info -> info.balance() == 0L);
      return new ArrayList<>(filteredInfo.values());
    }

    @NonNull
    private List<CustomerBalanceInfo> _onAddBalanceInfo(@NonNull List<CustomerModel> customers) {
      Objects.requireNonNull(customers);

      final ArrayList<CustomerBalanceInfo> currentInfo =
          DashboardViewModel.this._customersWithBalance.getValue() != null
              ? new ArrayList<>(DashboardViewModel.this._customersWithBalance.getValue())
              : new ArrayList<>();
      final List<CustomerBalanceInfo> customerInfo =
          customers.stream()
              .map(customer -> new CustomerBalanceInfo(customer.id(), customer.balance()))
              .collect(Collectors.toList());

      currentInfo.addAll(customerInfo);
      return currentInfo;
    }

    @NonNull
    private List<CustomerBalanceInfo> _onRemoveBalanceInfo(@NonNull List<CustomerModel> customers) {
      Objects.requireNonNull(customers);

      final ArrayList<CustomerBalanceInfo> currentInfo =
          DashboardViewModel.this._customersWithBalance.getValue() != null
              ? new ArrayList<>(DashboardViewModel.this._customersWithBalance.getValue())
              : new ArrayList<>();

      for (CustomerModel customer : customers) {
        for (int i = currentInfo.size(); i-- > 0; ) {
          if (currentInfo.get(i).id() != null && currentInfo.get(i).id().equals(customer.id())) {
            currentInfo.remove(i);
            break;
          }
        }
      }

      return currentInfo;
    }

    @NonNull
    private List<CustomerDebtInfo> _onUpdateDebtInfo(@NonNull List<CustomerModel> customers) {
      Objects.requireNonNull(customers);

      final ArrayList<CustomerDebtInfo> currentInfo =
          DashboardViewModel.this._customersWithDebt.getValue() != null
              ? new ArrayList<>(DashboardViewModel.this._customersWithDebt.getValue())
              : new ArrayList<>();
      final List<CustomerDebtInfo> customerInfo =
          customers.stream()
              .map(customer -> new CustomerDebtInfo(customer.id(), customer.debt()))
              .collect(Collectors.toList());
      final HashMap<Long, CustomerDebtInfo> filteredInfo = new HashMap<>();

      currentInfo.forEach(info -> filteredInfo.put(info.id(), info));
      customerInfo.forEach(info -> filteredInfo.put(info.id(), info)); // Override duplicate ID.
      filteredInfo.values().removeIf(info -> info.debt().compareTo(BigDecimal.ZERO) == 0);
      return new ArrayList<>(filteredInfo.values());
    }

    @NonNull
    private List<CustomerDebtInfo> _onAddDebtInfo(@NonNull List<CustomerModel> customers) {
      Objects.requireNonNull(customers);

      final ArrayList<CustomerDebtInfo> currentInfo =
          DashboardViewModel.this._customersWithDebt.getValue() != null
              ? new ArrayList<>(DashboardViewModel.this._customersWithDebt.getValue())
              : new ArrayList<>();
      final List<CustomerDebtInfo> customerInfo =
          customers.stream()
              .map(customer -> new CustomerDebtInfo(customer.id(), customer.debt()))
              .collect(Collectors.toList());

      currentInfo.addAll(customerInfo);
      return currentInfo;
    }

    @NonNull
    private List<CustomerDebtInfo> _onRemoveDebtInfo(@NonNull List<CustomerModel> customers) {
      Objects.requireNonNull(customers);

      final ArrayList<CustomerDebtInfo> currentInfo =
          DashboardViewModel.this._customersWithDebt.getValue() != null
              ? new ArrayList<>(DashboardViewModel.this._customersWithDebt.getValue())
              : new ArrayList<>();

      for (CustomerModel customer : customers) {
        for (int i = currentInfo.size(); i-- > 0; ) {
          if (currentInfo.get(i).id() != null && currentInfo.get(i).id().equals(customer.id())) {
            currentInfo.remove(i);
            break;
          }
        }
      }

      return currentInfo;
    }
  }
}
