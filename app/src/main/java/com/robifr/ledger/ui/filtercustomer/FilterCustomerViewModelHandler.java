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
