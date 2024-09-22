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

package com.robifr.ledger.ui.selectcustomer;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.selectcustomer.recycler.SelectCustomerHeaderHolder;
import com.robifr.ledger.ui.selectcustomer.recycler.SelectCustomerListHolder;
import com.robifr.ledger.ui.selectcustomer.viewmodel.SelectCustomerViewModel;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SelectCustomerViewModelHandler {
  @NonNull private final SelectCustomerFragment _fragment;
  @NonNull private final SelectCustomerViewModel _viewModel;

  public SelectCustomerViewModelHandler(
      @NonNull SelectCustomerFragment fragment, @NonNull SelectCustomerViewModel viewModel) {
    this._fragment = Objects.requireNonNull(fragment);
    this._viewModel = Objects.requireNonNull(viewModel);

    this._viewModel
        .resultSelectedCustomerId()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onResultSelectedCustomerId));
    this._viewModel
        .snackbarMessage()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onSnackbarMessage));
    this._viewModel.customers().observe(this._fragment.getViewLifecycleOwner(), this::_onCustomers);
    this._viewModel
        .isSelectedCustomerExpanded()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onSelectedCustomerExpanded);
    this._viewModel
        .expandedCustomerIndex()
        .observe(this._fragment.getViewLifecycleOwner(), this::_onExpandedCustomerIndex);
  }

  /**
   * @noinspection OptionalUsedAsFieldOrParameterType
   */
  private void _onResultSelectedCustomerId(@NonNull Optional<Long> customerId) {
    Objects.requireNonNull(customerId);

    final Bundle bundle = new Bundle();

    customerId.ifPresent(
        id -> bundle.putLong(SelectCustomerFragment.Result.SELECTED_CUSTOMER_ID_LONG.key(), id));

    this._fragment
        .getParentFragmentManager()
        .setFragmentResult(SelectCustomerFragment.Request.SELECT_CUSTOMER.key(), bundle);
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

  private void _onSelectedCustomerExpanded(boolean isExpanded) {
    final RecyclerView.ViewHolder viewHolder =
        this._fragment.fragmentBinding().recyclerView.findViewHolderForLayoutPosition(0);

    if (viewHolder instanceof SelectCustomerHeaderHolder<?> headerHolder) {
      headerHolder.setCardExpanded(isExpanded);
    }
  }

  private void _onExpandedCustomerIndex(int index) {
    // Shrink all cards.
    for (int i = 0; i < this._fragment.fragmentBinding().recyclerView.getChildCount(); i++) {
      final RecyclerView.ViewHolder viewHolder =
          this._fragment
              .fragmentBinding()
              .recyclerView
              .getChildViewHolder(this._fragment.fragmentBinding().recyclerView.getChildAt(i));

      if (viewHolder instanceof SelectCustomerListHolder<?> holder) holder.setCardExpanded(false);
    }

    // Expand the selected card.
    if (index != -1) {
      final RecyclerView.ViewHolder viewHolder =
          // +1 offset because header holder.
          this._fragment.fragmentBinding().recyclerView.findViewHolderForLayoutPosition(index + 1);

      if (viewHolder instanceof SelectCustomerListHolder<?> holder) holder.setCardExpanded(true);
    }
  }
}
