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

package com.robifr.ledger.ui.searchcustomer;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.ui.StringResources;
import com.robifr.ledger.ui.customer.recycler.CustomerListHolder;
import com.robifr.ledger.ui.searchcustomer.viewmodel.SearchCustomerViewModel;
import com.robifr.ledger.ui.selectcustomer.recycler.SelectCustomerListHolder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SearchCustomerViewModelHandler {
  @NonNull private final SearchCustomerFragment _fragment;
  @NonNull private final SearchCustomerViewModel _viewModel;

  public SearchCustomerViewModelHandler(
      @NonNull SearchCustomerFragment fragment, @NonNull SearchCustomerViewModel viewModel) {
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
        id -> bundle.putLong(SearchCustomerFragment.Result.SELECTED_CUSTOMER_ID_LONG.key(), id));

    this._fragment
        .getParentFragmentManager()
        .setFragmentResult(SearchCustomerFragment.Request.SELECT_CUSTOMER.key(), bundle);
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

  /**
   * @noinspection OptionalUsedAsFieldOrParameterType
   */
  private void _onCustomers(@NonNull Optional<List<CustomerModel>> customers) {
    Objects.requireNonNull(customers);

    this._fragment.adapter().notifyDataSetChanged();

    final int noResultsVisibility =
        // Only show illustration when customers are empty list.
        customers.isPresent() && customers.get().isEmpty() ? View.VISIBLE : View.GONE;
    final int recyclerVisibility =
        customers.isPresent() && !customers.get().isEmpty() ? View.VISIBLE : View.GONE;

    this._fragment.fragmentBinding().horizontalListContainer.setVisibility(noResultsVisibility);
    this._fragment.fragmentBinding().noResultsImage.getRoot().setVisibility(noResultsVisibility);
    this._fragment.fragmentBinding().recyclerView.setVisibility(recyclerVisibility);
  }

  private void _onExpandedCustomerIndex(int index) {
    // Shrink all cards.
    for (int i = 0; i < this._fragment.fragmentBinding().recyclerView.getChildCount(); i++) {
      final RecyclerView.ViewHolder viewHolder =
          this._fragment
              .fragmentBinding()
              .recyclerView
              .getChildViewHolder(this._fragment.fragmentBinding().recyclerView.getChildAt(i));

      if (viewHolder instanceof CustomerListHolder<?> holder) {
        holder.setCardExpanded(false);
      } else if (viewHolder instanceof SelectCustomerListHolder<?> holder) {
        holder.setCardExpanded(false);
      }
    }

    // Expand the selected card.
    if (index != -1) {
      final RecyclerView.ViewHolder viewHolder =
          // +1 offset because header holder.
          this._fragment.fragmentBinding().recyclerView.findViewHolderForLayoutPosition(index + 1);

      if (viewHolder instanceof CustomerListHolder<?> holder) {
        holder.setCardExpanded(true);
      } else if (viewHolder instanceof SelectCustomerListHolder<?> holder) {
        holder.setCardExpanded(true);
      }
    }
  }
}
