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
import androidx.lifecycle.MediatorLiveData;
import com.robifr.ledger.data.model.CustomerBalanceInfo;
import com.robifr.ledger.data.model.CustomerDebtInfo;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class DashboardBalanceViewModel {
  @NonNull private final MediatorLiveData<BigDecimal> _totalBalance = new MediatorLiveData<>();

  @NonNull
  private final MediatorLiveData<Integer> _totalCustomersWithBalance = new MediatorLiveData<>();

  @NonNull private final MediatorLiveData<BigDecimal> _totalDebt = new MediatorLiveData<>();

  @NonNull
  private final MediatorLiveData<Integer> _totalCustomersWithDebt = new MediatorLiveData<>();

  public DashboardBalanceViewModel(
      @NonNull LiveData<List<CustomerBalanceInfo>> balanceInfoLiveData,
      @NonNull LiveData<List<CustomerDebtInfo>> debtInfoLiveData) {
    Objects.requireNonNull(balanceInfoLiveData);
    Objects.requireNonNull(debtInfoLiveData);

    this._totalBalance.addSource(
        balanceInfoLiveData,
        balanceInfo -> {
          if (balanceInfo != null) {
            this._totalBalance.setValue(
                balanceInfo.stream()
                    .map(customer -> BigDecimal.valueOf(customer.balance()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
          }
        });
    this._totalCustomersWithBalance.addSource(
        balanceInfoLiveData,
        balanceInfo -> {
          if (balanceInfo != null) this._totalCustomersWithBalance.setValue(balanceInfo.size());
        });
    this._totalDebt.addSource(
        debtInfoLiveData,
        debtInfo -> {
          if (debtInfo != null) {
            this._totalDebt.setValue(
                debtInfo.stream()
                    .map(CustomerDebtInfo::debt)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
          }
        });
    this._totalCustomersWithDebt.addSource(
        debtInfoLiveData,
        debtInfo -> {
          if (debtInfo != null) this._totalCustomersWithDebt.setValue(debtInfo.size());
        });
  }

  @NonNull
  public LiveData<BigDecimal> totalBalance() {
    return this._totalBalance;
  }

  @NonNull
  public LiveData<Integer> totalCustomersWithBalance() {
    return this._totalCustomersWithBalance;
  }

  @NonNull
  public LiveData<BigDecimal> totalDebt() {
    return this._totalDebt;
  }

  @NonNull
  public LiveData<Integer> totalCustomersWithDebt() {
    return this._totalCustomersWithDebt;
  }
}
