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

package com.robifr.ledger.ui.filtercustomer;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.filtercustomer.recycler.FilterCustomerListHolder;
import com.robifr.ledger.ui.filtercustomer.viewmodel.FilterCustomerViewModel;
import java.util.List;
import java.util.Objects;

public class FilterCustomerViewModelHandler {
  @NonNull private final FilterCustomerFragment _fragment;
  @NonNull private final FilterCustomerViewModel _viewModel;

  public FilterCustomerViewModelHandler(
      @NonNull FilterCustomerFragment fragment, @NonNull FilterCustomerViewModel viewModel) {
    this._fragment = Objects.requireNonNull(fragment);
    this._viewModel = Objects.requireNonNull(viewModel);

    this._viewModel
        .resultFilteredCustomerIds()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onResultFilteredCustomerIds));
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
        .filteredCustomers()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onFilteredCustomers);
  }

  private void _onResultFilteredCustomerIds(@NonNull List<Long> customerIds) {
    Objects.requireNonNull(customerIds);

    final Bundle bundle = new Bundle();
    bundle.putLongArray(
        FilterCustomerFragment.Result.FILTERED_CUSTOMER_IDS_LONG_ARRAY.key(),
        customerIds.stream().mapToLong(Long::longValue).toArray());

    this._fragment
        .getParentFragmentManager()
        .setFragmentResult(FilterCustomerFragment.Request.FILTER_CUSTOMER.key(), bundle);
    this._fragment.finish();
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

      if (viewHolder instanceof FilterCustomerListHolder<?> holder) holder.setCardExpanded(false);
    }

    // Expand the selected card.
    if (index != -1) {
      final RecyclerView.ViewHolder viewHolder =
          // +1 offset because header holder.
          this._fragment.fragmentBinding().recyclerView.findViewHolderForLayoutPosition(index + 1);

      if (viewHolder instanceof FilterCustomerListHolder<?> holder) holder.setCardExpanded(true);
    }
  }

  private void _onFilteredCustomers(@NonNull List<CustomerModel> customers) {
    Objects.requireNonNull(customers);

    // Uncheck all cards.
    for (int i = 0; i < this._fragment.fragmentBinding().recyclerView.getChildCount(); i++) {
      final RecyclerView.ViewHolder holder =
          this._fragment
              .fragmentBinding()
              .recyclerView
              .getChildViewHolder(this._fragment.fragmentBinding().recyclerView.getChildAt(i));

      if (holder instanceof FilterCustomerListHolder<?> listHolder) {
        listHolder.setCardChecked(false);
      }
    }

    // Check the selected card.
    for (CustomerModel customer : customers) {
      final int customerIndex = this._viewModel.customers().getValue().indexOf(customer);
      final RecyclerView.ViewHolder holder =
          // +1 offset because header holder.
          this._fragment
              .fragmentBinding()
              .recyclerView
              .findViewHolderForLayoutPosition(customerIndex + 1);

      if (holder instanceof FilterCustomerListHolder<?> listHolder) listHolder.setCardChecked(true);
    }

    this._fragment.adapter().notifyItemChanged(0); // Notify header holder.
  }
}
