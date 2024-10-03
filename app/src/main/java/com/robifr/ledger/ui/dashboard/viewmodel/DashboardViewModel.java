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
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.dashboard.DashboardRevenue;
import com.robifr.ledger.util.livedata.SafeEvent;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMutableLiveData;
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
  @NonNull private final DashboardSummaryViewModel _summaryView;
  @NonNull private final DashboardBalanceViewModel _balanceView;
  @NonNull private final DashboardRevenueViewModel _revenueView;

  @NonNull
  private final QueueChangedListeners _queueChangedListener = new QueueChangedListeners(this);

  @NonNull
  private final CustomerChangedListener _customerChangedListener =
      new CustomerChangedListener(this);

  @NonNull
  private final MutableLiveData<SafeEvent<StringResources>> _snackbarMessage =
      new MutableLiveData<>();

  @NonNull
  private final SafeMutableLiveData<QueueDate> _date =
      new SafeMutableLiveData<>(QueueDate.withRange(QueueDate.Range.ALL_TIME));

  @NonNull
  private final SafeMutableLiveData<List<QueueModel>> _queues =
      new SafeMutableLiveData<>(List.of());

  @NonNull
  private final SafeMutableLiveData<List<CustomerBalanceInfo>> _customersWithBalance =
      new SafeMutableLiveData<>(List.of());

  @NonNull
  private final SafeMutableLiveData<List<CustomerDebtInfo>> _customersWithDebt =
      new SafeMutableLiveData<>(List.of());

  @Inject
  public DashboardViewModel(
      @NonNull QueueRepository queueRepository, @NonNull CustomerRepository customerRepository) {
    this._queueRepository = Objects.requireNonNull(queueRepository);
    this._customerRepository = Objects.requireNonNull(customerRepository);
    this._summaryView = new DashboardSummaryViewModel(this);
    this._balanceView = new DashboardBalanceViewModel(this);
    this._revenueView = new DashboardRevenueViewModel(this);

    this._queueRepository.addModelChangedListener(this._queueChangedListener);
    this._customerRepository.addModelChangedListener(this._customerChangedListener);
    this._revenueView.onDisplayedChartChanged(DashboardRevenue.OverviewType.RECEIVED_INCOME);

    // Setting up initial values inside a fragment is painful. See commit d5604599.
    SafeEvent.observeOnce(
        this._selectAllQueuesInRange(
            this._date.getValue().dateStart(), this._date.getValue().dateEnd()),
        this::_onQueuesChanged,
        Objects::nonNull);
    SafeEvent.observeOnce(
        this._selectAllCustomersWithBalance(),
        this::_onCustomersWithBalanceChanged,
        Objects::nonNull);
    SafeEvent.observeOnce(
        this._selectAllCustomersWithDebt(), this::_onCustomersWithDebtChanged, Objects::nonNull);
  }

  @Override
  public void onCleared() {
    this._queueRepository.removeModelChangedListener(this._queueChangedListener);
    this._customerRepository.removeModelChangedListener(this._customerChangedListener);
  }

  @NonNull
  public DashboardSummaryViewModel summaryView() {
    return this._summaryView;
  }

  @NonNull
  public DashboardBalanceViewModel balanceView() {
    return this._balanceView;
  }

  @NonNull
  public DashboardRevenueViewModel revenueView() {
    return this._revenueView;
  }

  @NonNull
  public LiveData<SafeEvent<StringResources>> snackbarMessage() {
    return this._snackbarMessage;
  }

  @NonNull
  public SafeLiveData<QueueDate> date() {
    return this._date;
  }

  public void onDateChanged(@NonNull QueueDate date) {
    Objects.requireNonNull(date);

    this._date.setValue(date);
    SafeEvent.observeOnce(
        this._selectAllQueuesInRange(date.dateStart(), date.dateEnd()),
        this::_onQueuesChanged,
        Objects::nonNull);
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
                    new SafeEvent<>(
                        new StringResources.Strings(R.string.dashboard_fetchAllCustomerError)));
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
                    new SafeEvent<>(
                        new StringResources.Strings(R.string.dashboard_fetchAllCustomerError)));
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
                    new SafeEvent<>(
                        new StringResources.Strings(R.string.dashboard_fetchAllQueueError)));
              }

              result.postValue(queues);
            });
    return result;
  }

  @NonNull
  SafeLiveData<List<CustomerBalanceInfo>> _customersWithBalance() {
    return this._customersWithBalance;
  }

  @NonNull
  SafeLiveData<List<CustomerDebtInfo>> _customersWithDebt() {
    return this._customersWithDebt;
  }

  @NonNull
  SafeLiveData<List<QueueModel>> _queues() {
    return this._queues;
  }

  void _onCustomersWithBalanceChanged(@NonNull List<CustomerBalanceInfo> balanceInfo) {
    Objects.requireNonNull(balanceInfo);

    this._customersWithBalance.setValue(Collections.unmodifiableList(balanceInfo));
  }

  void _onCustomersWithDebtChanged(@NonNull List<CustomerDebtInfo> debtInfo) {
    Objects.requireNonNull(debtInfo);

    this._customersWithDebt.setValue(Collections.unmodifiableList(debtInfo));
  }

  void _onQueuesChanged(@NonNull List<QueueModel> queues) {
    Objects.requireNonNull(queues);

    this._queues.setValue(Collections.unmodifiableList(queues));
  }
}
