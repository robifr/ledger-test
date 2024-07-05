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
