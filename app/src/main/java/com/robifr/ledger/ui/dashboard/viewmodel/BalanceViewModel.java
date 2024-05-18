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
import com.robifr.ledger.data.model.CustomerBalanceInfo;
import com.robifr.ledger.data.model.CustomerDebtInfo;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BalanceViewModel {
  @NonNull private final DashboardViewModel _viewModel;

  @NonNull private final MutableLiveData<List<CustomerBalanceInfo>> _customersWithBalance;
  @NonNull private final MutableLiveData<List<CustomerDebtInfo>> _customersWithDebt;

  public BalanceViewModel(@NonNull DashboardViewModel viewModel) {
    this._viewModel = Objects.requireNonNull(viewModel);

    // It's unusual indeed to call its own method in its constructor. Setting up initial values
    // inside a fragment is painful. You have to consider whether the fragment recreated due to
    // configuration changes, or if it's popped from the backstack, or when the view model itself
    // is recreated due to the fragment being navigated by bottom navigation.
    this._customersWithBalance =
        (MutableLiveData<List<CustomerBalanceInfo>>) this._viewModel.selectAllIdsWithBalance();
    this._customersWithDebt =
        (MutableLiveData<List<CustomerDebtInfo>>) this._viewModel.selectAllIdsWithDebt();
  }

  @NonNull
  public LiveData<List<CustomerBalanceInfo>> customersWithBalance() {
    return this._customersWithBalance;
  }

  @NonNull
  public LiveData<List<CustomerDebtInfo>> customersWithDebt() {
    return this._customersWithDebt;
  }

  public void onCustomersWithBalanceChanged(@NonNull List<CustomerBalanceInfo> balanceInfo) {
    Objects.requireNonNull(balanceInfo);

    this._customersWithBalance.setValue(Collections.unmodifiableList(balanceInfo));
  }

  public void onCustomersWithDebtChanged(@NonNull List<CustomerDebtInfo> debtInfo) {
    Objects.requireNonNull(debtInfo);

    this._customersWithDebt.setValue(Collections.unmodifiableList(debtInfo));
  }
}
