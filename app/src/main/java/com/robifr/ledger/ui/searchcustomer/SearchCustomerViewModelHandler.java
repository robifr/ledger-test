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

package com.robifr.ledger.ui.searchcustomer;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.ui.searchcustomer.viewmodel.SearchCustomerViewModel;
import com.robifr.ledger.util.Compats;
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
        .initializedInitialQuery()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            event -> event.handleIfNotHandled(this::_onInitializedInitialQuery));
    this._viewModel.customers().observe(this._fragment.getViewLifecycleOwner(), this::_onCustomers);
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

  private void _onInitializedInitialQuery(@NonNull String query) {
    Objects.requireNonNull(query);

    if (!query.isEmpty()) {
      this._fragment.fragmentBinding().searchView.setQuery(query, true);
    } else {
      Compats.showKeyboard(
          this._fragment.requireContext(), this._fragment.fragmentBinding().searchView);
    }
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
}
