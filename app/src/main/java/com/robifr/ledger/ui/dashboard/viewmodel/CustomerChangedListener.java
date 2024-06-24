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
import com.robifr.ledger.data.InfoUpdater;
import com.robifr.ledger.data.model.CustomerBalanceInfo;
import com.robifr.ledger.data.model.CustomerDebtInfo;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.repository.ModelChangedListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class CustomerChangedListener implements ModelChangedListener<CustomerModel> {
  @NonNull private final DashboardViewModel _viewModel;

  public CustomerChangedListener(@NonNull DashboardViewModel viewModel) {
    this._viewModel = Objects.requireNonNull(viewModel);
  }

  @Override
  @WorkerThread
  public void onModelUpdated(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              final ArrayList<CustomerBalanceInfo> currentBalanceInfo =
                  this._viewModel.customersWithBalance().getValue() != null
                      ? new ArrayList<>(this._viewModel.customersWithBalance().getValue())
                      : new ArrayList<>();
              final List<CustomerBalanceInfo> balanceInfo =
                  InfoUpdater.updateInfo(
                      customers,
                      currentBalanceInfo,
                      (customer) -> new CustomerBalanceInfo(customer.id(), customer.balance()));

              final ArrayList<CustomerDebtInfo> currentDebtInfo =
                  this._viewModel.customersWithBalance().getValue() != null
                      ? new ArrayList<>(this._viewModel.customersWithDebt().getValue())
                      : new ArrayList<>();
              final List<CustomerDebtInfo> debtInfo =
                  InfoUpdater.updateInfo(
                      customers,
                      currentDebtInfo,
                      (customer) -> new CustomerDebtInfo(customer.id(), customer.debt()));

              balanceInfo.removeIf(info -> info.balance() == 0L);
              debtInfo.removeIf(info -> info.debt().compareTo(BigDecimal.ZERO) == 0);
              this._viewModel.onCustomersWithBalanceChanged(balanceInfo);
              this._viewModel.onCustomersWithDebtChanged(debtInfo);
            });
  }

  @Override
  @WorkerThread
  public void onModelAdded(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              final ArrayList<CustomerBalanceInfo> currentBalanceInfo =
                  this._viewModel.customersWithBalance().getValue() != null
                      ? new ArrayList<>(this._viewModel.customersWithBalance().getValue())
                      : new ArrayList<>();
              final List<CustomerBalanceInfo> balanceInfo =
                  InfoUpdater.addInfo(
                      customers,
                      currentBalanceInfo,
                      (customer) -> new CustomerBalanceInfo(customer.id(), customer.balance()));

              final ArrayList<CustomerDebtInfo> currentDebtInfo =
                  this._viewModel.customersWithBalance().getValue() != null
                      ? new ArrayList<>(this._viewModel.customersWithDebt().getValue())
                      : new ArrayList<>();
              final List<CustomerDebtInfo> debtInfo =
                  InfoUpdater.addInfo(
                      customers,
                      currentDebtInfo,
                      (customer) -> new CustomerDebtInfo(customer.id(), customer.debt()));

              this._viewModel.onCustomersWithBalanceChanged(balanceInfo);
              this._viewModel.onCustomersWithDebtChanged(debtInfo);
            });
  }

  @Override
  @WorkerThread
  public void onModelDeleted(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              final ArrayList<CustomerBalanceInfo> currentBalanceInfo =
                  this._viewModel.customersWithBalance().getValue() != null
                      ? new ArrayList<>(this._viewModel.customersWithBalance().getValue())
                      : new ArrayList<>();
              final List<CustomerBalanceInfo> balanceInfo =
                  InfoUpdater.removeInfo(
                      customers,
                      currentBalanceInfo,
                      (customer) -> new CustomerBalanceInfo(customer.id(), customer.balance()));

              final ArrayList<CustomerDebtInfo> currentDebtInfo =
                  this._viewModel.customersWithBalance().getValue() != null
                      ? new ArrayList<>(this._viewModel.customersWithDebt().getValue())
                      : new ArrayList<>();
              final List<CustomerDebtInfo> debtInfo =
                  InfoUpdater.removeInfo(
                      customers,
                      currentDebtInfo,
                      (customer) -> new CustomerDebtInfo(customer.id(), customer.debt()));

              this._viewModel.onCustomersWithBalanceChanged(balanceInfo);
              this._viewModel.onCustomersWithDebtChanged(debtInfo);
            });
  }

  @Override
  @WorkerThread
  public void onModelUpserted(@NonNull List<CustomerModel> customers) {}
}
