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
