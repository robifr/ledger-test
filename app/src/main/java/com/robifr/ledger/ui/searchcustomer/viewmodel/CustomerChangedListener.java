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

package com.robifr.ledger.ui.searchcustomer.viewmodel;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.robifr.ledger.data.ModelUpdater;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.repository.ModelChangedListener;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

class CustomerChangedListener implements ModelChangedListener<CustomerModel> {
  private final SearchCustomerViewModel _viewModel;

  public CustomerChangedListener(@NonNull SearchCustomerViewModel viewModel) {
    this._viewModel = Objects.requireNonNull(viewModel);
  }

  @Override
  public void onModelAdded(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    new Handler(Looper.getMainLooper())
        .post(() -> this._updateCustomers(customers, ModelUpdater::addModel));
  }

  @Override
  public void onModelUpdated(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    new Handler(Looper.getMainLooper())
        .post(() -> this._updateCustomers(customers, ModelUpdater::updateModel));
  }

  @Override
  public void onModelDeleted(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    new Handler(Looper.getMainLooper())
        .post(() -> this._updateCustomers(customers, ModelUpdater::deleteModel));
  }

  @Override
  public void onModelUpserted(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    new Handler(Looper.getMainLooper())
        .post(() -> this._updateCustomers(customers, ModelUpdater::upsertModel));
  }

  private void _updateCustomers(
      @NonNull List<CustomerModel> customers,
      @NonNull BiFunction<List<CustomerModel>, List<CustomerModel>, List<CustomerModel>> updater) {
    Objects.requireNonNull(customers);
    Objects.requireNonNull(updater);

    this._viewModel._onCustomersChanged(
        updater.apply(this._viewModel.customers().getValue().orElse(List.of()), customers));
  }
}
