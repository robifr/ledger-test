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
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.robifr.ledger.R;
import com.robifr.ledger.data.QueueFilters;
import com.robifr.ledger.data.model.CustomerBalanceInfo;
import com.robifr.ledger.data.model.CustomerDebtInfo;
import com.robifr.ledger.data.model.QueueWithProductOrdersInfo;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.repository.QueueRepository;
import com.robifr.ledger.ui.LiveDataEvent;
import com.robifr.ledger.ui.StringResources;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;

@HiltViewModel
public class DashboardViewModel extends ViewModel {
  @NonNull private final QueueRepository _queueRepository;
  @NonNull private final CustomerRepository _customerRepository;
  @NonNull private final BalanceViewModel _balanceView;

  @NonNull
  private final CustomerChangedListener _customerChangedListener =
      new CustomerChangedListener(this);

  @NonNull
  private final MutableLiveData<LiveDataEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull private final MutableLiveData<QueueFilters.DateRange> _date = new MutableLiveData<>();

  @NonNull
  private Pair<ZonedDateTime, ZonedDateTime> _dateStartEnd =
      QueueFilters.DateRange.ALL_TIME.dateStartEnd();

  @NonNull
  private final MutableLiveData<List<QueueWithProductOrdersInfo>> _queueWithProductOrders =
      new MutableLiveData<>();

  @Inject
  public DashboardViewModel(
      @NonNull QueueRepository queueRepository, @NonNull CustomerRepository customerRepository) {
    this._queueRepository = Objects.requireNonNull(queueRepository);
    this._customerRepository = Objects.requireNonNull(customerRepository);
    this._balanceView = new BalanceViewModel(this);

    this._date.setValue(QueueFilters.DateRange.ALL_TIME);
    this._customerRepository.addModelChangedListener(this._customerChangedListener);
  }

  @Override
  public void onCleared() {
    this._customerRepository.removeModelChangedListener(this._customerChangedListener);
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
  public LiveData<QueueFilters.DateRange> date() {
    return this._date;
  }

  @NonNull
  public Pair<ZonedDateTime, ZonedDateTime> dateStartEnd() {
    return this._dateStartEnd;
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

  @NonNull
  public LiveData<List<QueueWithProductOrdersInfo>> selectAllWithProductOrdersInRange(
      @NonNull ZonedDateTime startDate, @NonNull ZonedDateTime endDate) {
    Objects.requireNonNull(startDate);
    Objects.requireNonNull(endDate);

    final MutableLiveData<List<QueueWithProductOrdersInfo>> result = new MutableLiveData<>();

    this._queueRepository
        .selectAllWithProductOrdersInRange(startDate, endDate)
        .thenAcceptAsync(
            queues -> {
              if (queues == null) {
                this._snackbarMessage.postValue(
                    new LiveDataEvent<>(
                        new StringResources.Strings(
                            R.string.text_error_unable_to_retrieve_all_queues)));
              }

              result.postValue(queues);
            });
    return result;
  }

  public void onDateChanged(
      @NonNull QueueFilters.DateRange date,
      @NonNull Pair<ZonedDateTime, ZonedDateTime> dateStartEnd) {
    Objects.requireNonNull(date);
    Objects.requireNonNull(dateStartEnd);
    Objects.requireNonNull(dateStartEnd.first);
    Objects.requireNonNull(dateStartEnd.second);

    // Set date start-end firstly so that when date get selected, the range already available.
    // Especially when selecting `QueueFilters.DateRange#CUSTOM`.
    this._dateStartEnd = dateStartEnd;
    this._date.setValue(date);
  }
}
