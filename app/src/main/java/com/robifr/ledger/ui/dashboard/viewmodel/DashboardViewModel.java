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
import com.robifr.ledger.data.display.QueueDate;
import com.robifr.ledger.data.model.CustomerBalanceInfo;
import com.robifr.ledger.data.model.CustomerDebtInfo;
import com.robifr.ledger.data.model.QueueModel;
import com.robifr.ledger.repository.CustomerRepository;
import com.robifr.ledger.repository.QueueRepository;
import com.robifr.ledger.ui.LiveDataEvent;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.dashboard.DashboardRevenue;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;

@HiltViewModel
public class DashboardViewModel extends ViewModel {
  @NonNull private final QueueRepository _queueRepository;
  @NonNull private final CustomerRepository _customerRepository;
  @NonNull private final DashboardBalanceViewModel _balanceView;
  @NonNull private final DashboardPerformanceViewModel _performanceView;
  @NonNull private final DashboardRevenueViewModel _revenueView;

  @NonNull
  private final QueueChangedListeners _queueChangedListener = new QueueChangedListeners(this);

  @NonNull
  private final CustomerChangedListener _customerChangedListener =
      new CustomerChangedListener(this);

  @NonNull
  private final MutableLiveData<LiveDataEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull private final MutableLiveData<QueueDate> _date = new MutableLiveData<>();
  @NonNull private final MutableLiveData<List<QueueModel>> _queues = new MutableLiveData<>();

  @NonNull
  private final MutableLiveData<List<CustomerBalanceInfo>> _customersWithBalance =
      new MutableLiveData<>();

  @NonNull
  private final MutableLiveData<List<CustomerDebtInfo>> _customersWithDebt =
      new MutableLiveData<>();

  @Inject
  public DashboardViewModel(
      @NonNull QueueRepository queueRepository, @NonNull CustomerRepository customerRepository) {
    this._queueRepository = Objects.requireNonNull(queueRepository);
    this._customerRepository = Objects.requireNonNull(customerRepository);
    this._balanceView =
        new DashboardBalanceViewModel(this._customersWithBalance, this._customersWithDebt);
    this._performanceView = new DashboardPerformanceViewModel(this._queues);
    this._revenueView = new DashboardRevenueViewModel(this, this._queues);

    this._queueRepository.addModelChangedListener(this._queueChangedListener);
    this._customerRepository.addModelChangedListener(this._customerChangedListener);

    final QueueDate date = QueueDate.withRange(QueueDate.Range.ALL_TIME);
    // It's unusual indeed to call its own method in its constructor. Setting up initial values
    // inside a fragment is painful. You have to consider whether the fragment recreated due to
    // configuration changes, or if it's popped from the backstack, or when the view model itself
    // is recreated due to the fragment being navigated by bottom navigation.
    this.onDateChanged(date);
    this._revenueView.onDisplayedChartChanged(DashboardRevenue.OverviewType.RECEIVED_INCOME);
    LiveDataEvent.observeOnce(
        this._selectAllCustomersWithBalance(), this::onCustomersWithBalanceChanged, Objects::nonNull);
    LiveDataEvent.observeOnce(
        this._selectAllCustomersWithDebt(), this::onCustomersWithDebtChanged, Objects::nonNull);
  }

  @Override
  public void onCleared() {
    this._queueRepository.removeModelChangedListener(this._queueChangedListener);
    this._customerRepository.removeModelChangedListener(this._customerChangedListener);
  }

  @NonNull
  public DashboardBalanceViewModel balanceView() {
    return this._balanceView;
  }

  @NonNull
  public DashboardPerformanceViewModel performanceView() {
    return this._performanceView;
  }

  @NonNull
  public DashboardRevenueViewModel revenueView() {
    return this._revenueView;
  }

  @NonNull
  public LiveData<LiveDataEvent<StringResources>> snackbarMessage() {
    return this._snackbarMessage;
  }

  @NonNull
  public LiveData<QueueDate> date() {
    return this._date;
  }

  @NonNull
  public LiveData<List<QueueModel>> queues() {
    return this._queues;
  }

  public void onDateChanged(@NonNull QueueDate date) {
    Objects.requireNonNull(date);

    this._date.setValue(date);
    LiveDataEvent.observeOnce(
        this._selectAllQueuesInRange(date.dateStart(), date.dateEnd()),
        this::onQueuesChanged,
        Objects::nonNull);
  }

  public void onQueuesChanged(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    this._queues.setValue(Collections.unmodifiableList(queues));
  }

  @NonNull
  private LiveData<List<CustomerBalanceInfo>> _selectAllCustomersWithBalance() {
    final MutableLiveData<List<CustomerBalanceInfo>> result = new MutableLiveData<>();

    this._customerRepository
        .selectAllInfoWithBalance()
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
  private LiveData<List<CustomerDebtInfo>> _selectAllCustomersWithDebt() {
    final MutableLiveData<List<CustomerDebtInfo>> result = new MutableLiveData<>();

    this._customerRepository
        .selectAllInfoWithDebt()
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
  private LiveData<List<QueueModel>> _selectAllQueuesInRange(
      @NonNull ZonedDateTime startDate, @NonNull ZonedDateTime endDate) {
    Objects.requireNonNull(startDate);
    Objects.requireNonNull(endDate);

    final MutableLiveData<List<QueueModel>> result = new MutableLiveData<>();

    this._queueRepository
        .selectAllInRange(startDate, endDate)
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

  void onCustomersWithBalanceChanged(@NonNull List<CustomerBalanceInfo> balanceInfo) {
    Objects.requireNonNull(balanceInfo);

    this._customersWithBalance.setValue(Collections.unmodifiableList(balanceInfo));
  }

  void onCustomersWithDebtChanged(@NonNull List<CustomerDebtInfo> debtInfo) {
    Objects.requireNonNull(debtInfo);

    this._customersWithDebt.setValue(Collections.unmodifiableList(debtInfo));
  }
}
