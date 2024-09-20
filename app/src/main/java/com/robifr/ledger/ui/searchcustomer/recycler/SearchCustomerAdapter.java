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

package com.robifr.ledger.ui.searchcustomer.recycler;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.databinding.CustomerCardWideBinding;
import com.robifr.ledger.databinding.ListableListTextBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.customer.CustomerCardAction;
import com.robifr.ledger.ui.customer.CustomerListAction;
import com.robifr.ledger.ui.searchcustomer.SearchCustomerCardAction;
import com.robifr.ledger.ui.searchcustomer.SearchCustomerFragment;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SearchCustomerAdapter extends RecyclerView.Adapter<RecyclerViewHolder>
    implements CustomerListAction, CustomerCardAction, SearchCustomerCardAction {
  private enum ViewType {
    HEADER(0),
    LIST(1);

    private final int _value;

    private ViewType(int value) {
      this._value = value;
    }

    public int value() {
      return this._value;
    }
  }

  @NonNull private final SearchCustomerFragment _fragment;

  public SearchCustomerAdapter(@NonNull SearchCustomerFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
  }

  @Override
  @NonNull
  public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    Objects.requireNonNull(parent);

    final ViewType type =
        Arrays.stream(ViewType.values())
            .filter(e -> e.value() == viewType)
            .findFirst()
            .orElse(ViewType.LIST);
    final LayoutInflater inflater = this._fragment.getLayoutInflater();

    return switch (type) {
      case HEADER ->
          new SearchCustomerHeaderHolder<>(
              ListableListTextBinding.inflate(inflater, parent, false), this);

        // Defaults to `ViewType#LIST`.
      default ->
          new SearchCustomerListHolder<>(
              CustomerCardWideBinding.inflate(this._fragment.getLayoutInflater(), parent, false),
              this);
    };
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int index) {
    Objects.requireNonNull(holder);

    if (holder instanceof SearchCustomerHeaderHolder headerHolder) {
      headerHolder.bind(Optional.empty());

    } else if (holder instanceof SearchCustomerListHolder listHolder) {
      this._fragment
          .searchCustomerViewModel()
          .customers()
          .getValue()
          .map(customers -> customers.get(index - 1)) // -1 offset because header holder.
          .ifPresent(listHolder::bind);
    }
  }

  @Override
  public int getItemCount() {
    // +1 offset because header holder.
    return this._fragment.searchCustomerViewModel().customers().getValue().map(List::size).orElse(0)
        + 1;
  }

  @Override
  public int getItemViewType(int index) {
    return switch (index) {
      case 0 -> ViewType.HEADER.value();
      default -> ViewType.LIST.value();
    };
  }

  @Override
  @NonNull
  public List<CustomerModel> customers() {
    return this._fragment.searchCustomerViewModel().customers().getValue().orElse(List.of());
  }

  @Override
  public int expandedCustomerIndex() {
    return this._fragment.searchCustomerViewModel().expandedCustomerIndex().getValue();
  }

  @Override
  public void onExpandedCustomerIndexChanged(int index) {
    this._fragment.searchCustomerViewModel().onExpandedCustomerIndexChanged(index);
  }

  @Override
  public void onDeleteCustomer(@NonNull CustomerModel customer) {
    Objects.requireNonNull(customer);

    this._fragment.searchCustomerViewModel().onDeleteCustomer(customer);
  }

  @Override
  public boolean isSelectionEnabled() {
    return this._fragment.searchCustomerViewModel().isSelectionEnabled().getValue();
  }

  @Override
  public void onCustomerSelected(@Nullable CustomerModel customer) {
    this._fragment.searchCustomerViewModel().onCustomerSelected(customer);
  }
}
