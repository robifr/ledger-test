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
import androidx.annotation.Nullable;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.ui.LiveDataEvent.Observer;
import com.robifr.ledger.ui.searchcustomer.viewmodel.SearchCustomerViewModel;
import com.robifr.ledger.util.Compats;
import java.util.List;
import java.util.Objects;

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
            new Observer<>(this::_onResultSelectedCustomerId));
    this._viewModel
        .initializedInitialQuery()
        .observe(
            this._fragment.getViewLifecycleOwner(),
            new Observer<>(this::_onInitializedInitialQuery));
    this._viewModel.customers().observe(this._fragment.getViewLifecycleOwner(), this::_onCustomers);
  }

  private void _onResultSelectedCustomerId(@Nullable Long customerId) {
    final Bundle bundle = new Bundle();

    if (customerId != null) {
      bundle.putLong(SearchCustomerFragment.Result.SELECTED_CUSTOMER_ID.key(), customerId);
    }

    this._fragment
        .getParentFragmentManager()
        .setFragmentResult(SearchCustomerFragment.Request.SELECT_CUSTOMER.key(), bundle);
    this._fragment.finish();
  }

  private void _onInitializedInitialQuery(@Nullable String query) {
    if (query != null) {
      this._fragment.fragmentBinding().searchView.setQuery(query, true);
    } else {
      Compats.showKeyboard(
          this._fragment.requireContext(), this._fragment.fragmentBinding().searchView);
    }
  }

  private void _onCustomers(@Nullable List<CustomerModel> customers) {
    this._fragment.adapter().notifyDataSetChanged();

    final int noResultsVisibility =
        customers != null && customers.isEmpty() ? View.VISIBLE : View.GONE;
    final int recyclerVisibility =
        customers != null && !customers.isEmpty() ? View.VISIBLE : View.GONE;

    this._fragment.fragmentBinding().horizontalListContainer.setVisibility(noResultsVisibility);
    this._fragment.fragmentBinding().noResultsImage.getRoot().setVisibility(noResultsVisibility);
    this._fragment.fragmentBinding().recyclerView.setVisibility(recyclerVisibility);
  }
}
