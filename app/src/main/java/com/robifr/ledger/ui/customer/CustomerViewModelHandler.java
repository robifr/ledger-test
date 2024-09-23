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

package com.robifr.ledger.ui.customer;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.customer.recycler.CustomerListHolder;
import com.robifr.ledger.ui.customer.viewmodel.CustomerViewModel;
import java.util.List;
import java.util.Objects;

public class CustomerViewModelHandler {
  @NonNull private final CustomerFragment _fragment;
  @NonNull private final CustomerViewModel _viewModel;

  public CustomerViewModelHandler(
      @NonNull CustomerFragment fragment, @NonNull CustomerViewModel viewModel) {
    this._fragment = Objects.requireNonNull(fragment);
    this._viewModel = Objects.requireNonNull(viewModel);

    this._viewModel
        .snackbarMessage()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onSnackbarMessage));
    this._viewModel.customers().observe(this._fragment.getViewLifecycleOwner(), this::_onCustomers);
    this._viewModel
        .expandedCustomerIndex()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onExpandedCustomerIndex);

    this._viewModel
        .filterView()
        .inputtedMinBalanceText()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onFilterMinBalanceText);
    this._viewModel
        .filterView()
        .inputtedMaxBalanceText()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onFilterMaxBalanceText);
    this._viewModel
        .filterView()
        .inputtedMinDebtText()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onFilterMinDebtText);
    this._viewModel
        .filterView()
        .inputtedMaxDebtText()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onFilterMaxDebtText);
  }

  private void _onSnackbarMessage(@NonNull StringResources stringRes) {
    Objects.requireNonNull(stringRes);

    Snackbar.make(
            (View) this._fragment.fragmentBinding().getRoot().getParent(),
            StringResources.stringOf(this._fragment.requireContext(), stringRes),
            Snackbar.LENGTH_LONG)
        .show();
  }

  private void _onCustomers(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    this._fragment.adapter().notifyDataSetChanged();
  }

  private void _onExpandedCustomerIndex(int index) {
    // Shrink all cards.
    for (int i = 0; i < this._fragment.fragmentBinding().recyclerView.getChildCount(); i++) {
      final RecyclerView.ViewHolder viewHolder =
          this._fragment
              .fragmentBinding()
              .recyclerView
              .getChildViewHolder(this._fragment.fragmentBinding().recyclerView.getChildAt(i));

      if (viewHolder instanceof CustomerListHolder<?> holder) holder.setCardExpanded(false);
    }

    // Expand the selected card.
    if (index != -1) {
      final RecyclerView.ViewHolder viewHolder =
          // +1 offset because header holder.
          this._fragment.fragmentBinding().recyclerView.findViewHolderForLayoutPosition(index + 1);

      if (viewHolder instanceof CustomerListHolder<?> holder) holder.setCardExpanded(true);
    }
  }

  private void _onFilterMinBalanceText(@NonNull String minBalance) {
    Objects.requireNonNull(minBalance);

    this._fragment.filter().filterBalance().setFilteredMinBalanceText(minBalance);
  }

  private void _onFilterMaxBalanceText(@NonNull String maxBalance) {
    Objects.requireNonNull(maxBalance);

    this._fragment.filter().filterBalance().setFilteredMaxBalanceText(maxBalance);
  }

  private void _onFilterMinDebtText(@NonNull String minDebt) {
    Objects.requireNonNull(minDebt);

    this._fragment.filter().filterDebt().setFilteredMinDebtText(minDebt);
  }

  private void _onFilterMaxDebtText(@NonNull String maxDebt) {
    Objects.requireNonNull(maxDebt);

    this._fragment.filter().filterDebt().setFilteredMaxDebtText(maxDebt);
  }
}
