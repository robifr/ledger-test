/**
 * Copyright (c) 2022-present Robi
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

package com.robifr.ledger.ui.filter_customer;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.ui.LiveDataEvent.Observer;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.filter_customer.recycler.FilterCustomerListHolder;
import com.robifr.ledger.ui.filter_customer.view_model.FilterCustomerViewModel;
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
        .snackbarMessage()
        .observe(this._fragment.requireActivity(), new Observer<>(this::_onSnackbarMessage));
    this._viewModel
        .filteredCustomerIds()
        .observe(
            this._fragment.getViewLifecycleOwner(), new Observer<>(this::_onFilteredCustomerIds));
    this._viewModel.customers().observe(this._fragment.getViewLifecycleOwner(), this::_onCustomers);

    this._viewModel
        .addedFilteredCustomerIndexes()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            new Observer<>(this::_onAddedFilteredCustomerIndexes));
    this._viewModel
        .removedFilteredCustomerIndexes()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            new Observer<>(this::_onRemovedFilteredCustomerIndexes));
  }

  private void _onSnackbarMessage(@Nullable StringResources stringRes) {
    if (stringRes == null) return;

    Snackbar.make(
            this._fragment.requireView(),
            StringResources.stringOf(this._fragment.requireContext(), stringRes),
            Snackbar.LENGTH_LONG)
        .show();
  }

  private void _onFilteredCustomerIds(@Nullable List<Long> customerIds) {
    final long[] filteredCustomerIds =
        customerIds != null
            ? customerIds.stream().mapToLong(Long::longValue).toArray()
            : new long[0];

    final Bundle bundle = new Bundle();
    bundle.putLongArray(
        FilterCustomerFragment.Result.FILTERED_CUSTOMER_IDS.key(), filteredCustomerIds);

    this._fragment
        .getParentFragmentManager()
        .setFragmentResult(FilterCustomerFragment.Request.FILTER_CUSTOMER.key(), bundle);
    this._fragment.finish();
  }

  private void _onCustomers(@Nullable List<CustomerModel> customers) {
    this._fragment.adapter().notifyDataSetChanged();
  }

  private void _onAddedFilteredCustomerIndexes(@Nullable List<Integer> indexes) {
    if (indexes == null) return;

    this._fragment.adapter().notifyItemChanged(0); // Update header holder.

    for (int index : indexes) {
      final RecyclerView.ViewHolder holder =
          // +1 offset because header holder.
          this._fragment.fragmentBinding().recyclerView.findViewHolderForLayoutPosition(index + 1);

      if (holder instanceof FilterCustomerListHolder listHolder) listHolder.setChecked(true);
    }
  }

  private void _onRemovedFilteredCustomerIndexes(@Nullable List<Integer> indexes) {
    if (indexes == null) return;

    this._fragment.adapter().notifyItemChanged(0); // Update header holder.

    for (int index : indexes) {
      final RecyclerView.ViewHolder holder =
          // +1 offset because header holder.
          this._fragment.fragmentBinding().recyclerView.findViewHolderForLayoutPosition(index + 1);

      if (holder instanceof FilterCustomerListHolder listHolder) listHolder.setChecked(false);
    }
  }
}
