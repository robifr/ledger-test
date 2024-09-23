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
import com.robifr.ledger.data.model.CustomerDebtInfo;
import com.robifr.ledger.util.livedata.SafeLiveData;
import com.robifr.ledger.util.livedata.SafeMediatorLiveData;
import java.math.BigDecimal;
import java.util.Objects;

public class DashboardBalanceViewModel {
  @NonNull private final DashboardViewModel _viewModel;

  @NonNull
  private final SafeMediatorLiveData<BigDecimal> _totalBalance =
      new SafeMediatorLiveData<>(BigDecimal.ZERO);

  @NonNull
  private final SafeMediatorLiveData<Integer> _totalCustomersWithBalance =
      new SafeMediatorLiveData<>(0);

  @NonNull
  private final SafeMediatorLiveData<BigDecimal> _totalDebt =
      new SafeMediatorLiveData<>(BigDecimal.ZERO);

  @NonNull
  private final SafeMediatorLiveData<Integer> _totalCustomersWithDebt =
      new SafeMediatorLiveData<>(0);

  public DashboardBalanceViewModel(@NonNull DashboardViewModel viewModel) {
    this._viewModel = Objects.requireNonNull(viewModel);

    this._totalBalance.addSource(
        this._viewModel._customersWithBalance().toLiveData(),
        balanceInfo ->
            this._totalBalance.setValue(
                balanceInfo.stream()
                    .map(customer -> BigDecimal.valueOf(customer.balance()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)));
    this._totalCustomersWithBalance.addSource(
        this._viewModel._customersWithBalance().toLiveData(),
        balanceInfo -> this._totalCustomersWithBalance.setValue(balanceInfo.size()));
    this._totalDebt.addSource(
        this._viewModel._customersWithDebt().toLiveData(),
        debtInfo ->
            this._totalDebt.setValue(
                debtInfo.stream()
                    .map(CustomerDebtInfo::debt)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)));
    this._totalCustomersWithDebt.addSource(
        this._viewModel._customersWithDebt().toLiveData(),
        debtInfo -> this._totalCustomersWithDebt.setValue(debtInfo.size()));
  }

  @NonNull
  public SafeLiveData<BigDecimal> totalBalance() {
    return this._totalBalance;
  }

  @NonNull
  public SafeLiveData<Integer> totalCustomersWithBalance() {
    return this._totalCustomersWithBalance;
  }

  @NonNull
  public SafeLiveData<BigDecimal> totalDebt() {
    return this._totalDebt;
  }

  @NonNull
  public SafeLiveData<Integer> totalCustomersWithDebt() {
    return this._totalCustomersWithDebt;
  }
}
