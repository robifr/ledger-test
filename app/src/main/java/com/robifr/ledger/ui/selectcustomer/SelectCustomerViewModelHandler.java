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

package com.robifr.ledger.ui.selectcustomer;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.robifr.ledger.R;
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

    final RecyclerView.ViewHolder viewHolder =
        this._fragment.fragmentBinding().recyclerView.findViewHolderForLayoutPosition(0);

    if (viewHolder instanceof SelectCustomerHeaderHolder<?> headerHolder) {
      final CustomerModel selectedCustomer = this._viewModel.initialSelectedCustomer();
      final CustomerModel selectedCustomerOnDb =
          selectedCustomer != null
              ? customers.stream()
                  .filter(
                      customer ->
                          customer.id() != null && customer.id().equals(selectedCustomer.id()))
                  .findFirst()
                  .orElse(null)
              : null;

      // Actually all these "if" checks should never happen, which causes
      // `SelectCustomerHeaderHolder#setSelectedItemDescriptionVisibile()` to always be set to
      // false, because selecting a customer can only occur during queue creation or editing.
      // Unlike`ProductModel`, the referenced customer in the `QueueModel` is never stored,
      // only its ID. This means the customer will always be up to date.

      // The original customer on database was deleted.
      if (selectedCustomer != null && selectedCustomerOnDb == null) {
        headerHolder.setSelectedItemDescriptionText(
            this._fragment
                .requireContext()
                .getString(R.string.text_originally_selected_customer_was_deleted));
        headerHolder.setSelectedItemDescriptionVisible(true);

        // The original customer on database was edited.
      } else if (selectedCustomer != null && !selectedCustomer.equals(selectedCustomerOnDb)) {
        headerHolder.setSelectedItemDescriptionText(
            this._fragment
                .requireContext()
                .getString(R.string.text_originally_selected_customer_was_changed));
        headerHolder.setSelectedItemDescriptionVisible(true);

        // It's the same unchanged customer.
      } else {
        headerHolder.setSelectedItemDescriptionVisible(false);
      }
    }

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
