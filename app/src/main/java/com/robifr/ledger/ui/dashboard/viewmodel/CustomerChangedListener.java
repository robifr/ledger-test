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
import java.util.List;
import java.util.Objects;

class CustomerChangedListener implements ModelChangedListener<CustomerModel> {
  @NonNull private final DashboardViewModel _viewModel;

  public CustomerChangedListener(@NonNull DashboardViewModel viewModel) {
    this._viewModel = Objects.requireNonNull(viewModel);
  }

  @Override
  @WorkerThread
  public void onModelAdded(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              this._updateBalanceInfo(customers, InfoUpdater::addInfo);
              this._updateDebtInfo(customers, InfoUpdater::addInfo);
            });
  }

  @Override
  @WorkerThread
  public void onModelUpdated(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              this._updateBalanceInfo(customers, InfoUpdater::updateInfo);
              this._updateDebtInfo(customers, InfoUpdater::updateInfo);
            });
  }

  @Override
  @WorkerThread
  public void onModelDeleted(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              this._updateBalanceInfo(customers, InfoUpdater::deleteInfo);
              this._updateDebtInfo(customers, InfoUpdater::deleteInfo);
            });
  }

  @Override
  @WorkerThread
  public void onModelUpserted(@NonNull List<CustomerModel> customers) {}

  private void _updateBalanceInfo(
      @NonNull List<CustomerModel> customers,
      @NonNull InfoUpdaterFunction<CustomerModel, CustomerBalanceInfo> updater) {
    Objects.requireNonNull(customers);
    Objects.requireNonNull(updater);

    final List<CustomerBalanceInfo> balanceInfo =
        updater.apply(
            customers,
            this._viewModel._customersWithBalance().getValue(),
            CustomerBalanceInfo::withModel);

    balanceInfo.removeIf(info -> info.balance() == 0L);
    this._viewModel._onCustomersWithBalanceChanged(balanceInfo);
  }

  private void _updateDebtInfo(
      @NonNull List<CustomerModel> customers,
      @NonNull InfoUpdaterFunction<CustomerModel, CustomerDebtInfo> updater) {
    Objects.requireNonNull(customers);
    Objects.requireNonNull(updater);

    final List<CustomerDebtInfo> debtInfo =
        updater.apply(
            customers,
            this._viewModel._customersWithDebt().getValue(),
            CustomerDebtInfo::withModel);

    debtInfo.removeIf(info -> info.debt().compareTo(BigDecimal.ZERO) == 0);
    this._viewModel._onCustomersWithDebtChanged(debtInfo);
  }
}
