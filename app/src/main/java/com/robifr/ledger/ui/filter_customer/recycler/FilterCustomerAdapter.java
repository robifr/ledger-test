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

package com.robifr.ledger.ui.filter_customer.recycler;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.databinding.CustomerCardBinding;
import com.robifr.ledger.databinding.ListableListSelectedItemBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.filter_customer.FilterCustomerFragment;
import com.robifr.ledger.util.Enums;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FilterCustomerAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
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

  @NonNull private final FilterCustomerFragment _fragment;

  public FilterCustomerAdapter(@NonNull FilterCustomerFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
  }

  @Override
  @NonNull
  public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    Objects.requireNonNull(parent);

    final ViewType type =
        Objects.requireNonNull(Enums.valueOf(viewType, ViewType.class, ViewType::value));
    final LayoutInflater inflater = this._fragment.getLayoutInflater();

    return switch (type) {
      case HEADER ->
          new FilterCustomerHeaderHolder(
              this._fragment, ListableListSelectedItemBinding.inflate(inflater, parent, false));

        // Defaults to `ViewType#LIST`.
      default ->
          new FilterCustomerListHolder(
              this._fragment, CustomerCardBinding.inflate(inflater, parent, false));
    };
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int index) {
    Objects.requireNonNull(holder);

    final List<CustomerModel> customers =
        this._fragment.filterCustomerViewModel().customers().getValue();
    if (customers == null) return;

    if (holder instanceof FilterCustomerHeaderHolder headerHolder) {
      headerHolder.bind(Optional.empty());

    } else if (holder instanceof FilterCustomerListHolder listHolder) {
      listHolder.bind(customers.get(index - 1)); // -1 offset because header holder.
    }
  }

  @Override
  public int getItemCount() {
    final List<CustomerModel> customers =
        this._fragment.filterCustomerViewModel().customers().getValue();
    return customers != null ? customers.size() + 1 : 0; // +1 offset because header holder.
  }

  @Override
  public int getItemViewType(int index) {
    return switch (index) {
      case 0 -> ViewType.HEADER.value();
      default -> ViewType.LIST.value();
    };
  }
}
