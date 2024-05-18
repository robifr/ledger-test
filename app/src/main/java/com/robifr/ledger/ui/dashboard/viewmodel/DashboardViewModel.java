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

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerBalanceInfo;
import com.robifr.ledger.data.model.CustomerDebtInfo;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.ui.LiveDataEvent;
import com.robifr.ledger.ui.StringResources;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;

@HiltViewModel
public class DashboardViewModel extends ViewModel {
  @NonNull private final CustomerRepository _customerRepository;
  @NonNull private final BalanceViewModel _balanceView;
  @NonNull private final CustomerUpdater _customerUpdater = new CustomerUpdater(this);

  @NonNull
  private final MutableLiveData<LiveDataEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @Inject
  public DashboardViewModel(@NonNull CustomerRepository customerRepository) {
    this._customerRepository = Objects.requireNonNull(customerRepository);
    this._balanceView = new BalanceViewModel(this);

    this._customerRepository.addModelChangedListener(this._customerUpdater);
  }

  @Override
  public void onCleared() {
    this._customerRepository.removeModelChangedListener(this._customerUpdater);
  }

  @NonNull
  public BalanceViewModel balanceView() {
    return this._balanceView;
  }

  @NonNull
  public LiveData<LiveDataEvent<StringResources>> snackbarMessage() {
    return this._snackbarMessage;
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
}
