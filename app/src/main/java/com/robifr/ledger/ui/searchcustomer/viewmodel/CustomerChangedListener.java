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
