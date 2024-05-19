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
import com.robifr.ledger.data.model.CustomerBalanceInfo;
import com.robifr.ledger.data.model.CustomerDebtInfo;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.repository.ModelChangedListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class CustomerUpdater implements ModelChangedListener<CustomerModel> {
  @NonNull private final DashboardViewModel _viewModel;

  public CustomerUpdater(@NonNull DashboardViewModel viewModel) {
    this._viewModel = Objects.requireNonNull(viewModel);
  }

  @Override
  @WorkerThread
  public void onModelUpdated(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              this._viewModel
                  .balanceView()
                  .onCustomersWithBalanceChanged(this._onUpdateBalanceInfo(customers));
              this._viewModel
                  .balanceView()
                  .onCustomersWithDebtChanged(this._onUpdateDebtInfo(customers));
            });
  }

  @Override
  @WorkerThread
  public void onModelAdded(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              this._viewModel
                  .balanceView()
                  .onCustomersWithBalanceChanged(this._onAddBalanceInfo(customers));
              this._viewModel
                  .balanceView()
                  .onCustomersWithDebtChanged(this._onAddDebtInfo(customers));
            });
  }

  @Override
  @WorkerThread
  public void onModelDeleted(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              this._viewModel
                  .balanceView()
                  .onCustomersWithBalanceChanged(this._onRemoveBalanceInfo(customers));
              this._viewModel
                  .balanceView()
                  .onCustomersWithDebtChanged(this._onRemoveDebtInfo(customers));
            });
  }

  @Override
  @WorkerThread
  public void onModelUpserted(@NonNull List<CustomerModel> customers) {}

  @NonNull
  private List<CustomerBalanceInfo> _onUpdateBalanceInfo(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    final ArrayList<CustomerBalanceInfo> currentInfo =
        this._viewModel.balanceView().customersWithBalance().getValue() != null
            ? new ArrayList<>(this._viewModel.balanceView().customersWithBalance().getValue())
            : new ArrayList<>();
    final List<CustomerBalanceInfo> customerInfo =
        customers.stream()
            .map(customer -> new CustomerBalanceInfo(customer.id(), customer.balance()))
            .collect(Collectors.toList());
    final HashMap<Long, CustomerBalanceInfo> filteredInfo = new HashMap<>();

    currentInfo.forEach(info -> filteredInfo.put(info.id(), info));
    customerInfo.forEach(info -> filteredInfo.put(info.id(), info)); // Override duplicate ID.
    filteredInfo.values().removeIf(info -> info.balance() == 0L);
    return new ArrayList<>(filteredInfo.values());
  }

  @NonNull
  private List<CustomerBalanceInfo> _onAddBalanceInfo(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    final ArrayList<CustomerBalanceInfo> currentInfo =
        this._viewModel.balanceView().customersWithBalance().getValue() != null
            ? new ArrayList<>(this._viewModel.balanceView().customersWithBalance().getValue())
            : new ArrayList<>();
    final List<CustomerBalanceInfo> customerInfo =
        customers.stream()
            .map(customer -> new CustomerBalanceInfo(customer.id(), customer.balance()))
            .collect(Collectors.toList());

    currentInfo.addAll(customerInfo);
    return currentInfo;
  }

  @NonNull
  private List<CustomerBalanceInfo> _onRemoveBalanceInfo(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    final ArrayList<CustomerBalanceInfo> currentInfo =
        this._viewModel.balanceView().customersWithBalance().getValue() != null
            ? new ArrayList<>(this._viewModel.balanceView().customersWithBalance().getValue())
            : new ArrayList<>();

    for (CustomerModel customer : customers) {
      for (int i = currentInfo.size(); i-- > 0; ) {
        if (currentInfo.get(i).id() != null && currentInfo.get(i).id().equals(customer.id())) {
          currentInfo.remove(i);
          break;
        }
      }
    }

    return currentInfo;
  }

  @NonNull
  private List<CustomerDebtInfo> _onUpdateDebtInfo(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    final ArrayList<CustomerDebtInfo> currentInfo =
        this._viewModel.balanceView().customersWithDebt().getValue() != null
            ? new ArrayList<>(this._viewModel.balanceView().customersWithDebt().getValue())
            : new ArrayList<>();
    final List<CustomerDebtInfo> customerInfo =
        customers.stream()
            .map(customer -> new CustomerDebtInfo(customer.id(), customer.debt()))
            .collect(Collectors.toList());
    final HashMap<Long, CustomerDebtInfo> filteredInfo = new HashMap<>();

    currentInfo.forEach(info -> filteredInfo.put(info.id(), info));
    customerInfo.forEach(info -> filteredInfo.put(info.id(), info)); // Override duplicate ID.
    filteredInfo.values().removeIf(info -> info.debt().compareTo(BigDecimal.ZERO) == 0);
    return new ArrayList<>(filteredInfo.values());
  }

  @NonNull
  private List<CustomerDebtInfo> _onAddDebtInfo(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    final ArrayList<CustomerDebtInfo> currentInfo =
        this._viewModel.balanceView().customersWithDebt().getValue() != null
            ? new ArrayList<>(this._viewModel.balanceView().customersWithDebt().getValue())
            : new ArrayList<>();
    final List<CustomerDebtInfo> customerInfo =
        customers.stream()
            .map(customer -> new CustomerDebtInfo(customer.id(), customer.debt()))
            .collect(Collectors.toList());

    currentInfo.addAll(customerInfo);
    return currentInfo;
  }

  @NonNull
  private List<CustomerDebtInfo> _onRemoveDebtInfo(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    final ArrayList<CustomerDebtInfo> currentInfo =
        this._viewModel.balanceView().customersWithDebt().getValue() != null
            ? new ArrayList<>(this._viewModel.balanceView().customersWithDebt().getValue())
            : new ArrayList<>();

    for (CustomerModel customer : customers) {
      for (int i = currentInfo.size(); i-- > 0; ) {
        if (currentInfo.get(i).id() != null && currentInfo.get(i).id().equals(customer.id())) {
          currentInfo.remove(i);
          break;
        }
      }
    }

    return currentInfo;
  }
}
