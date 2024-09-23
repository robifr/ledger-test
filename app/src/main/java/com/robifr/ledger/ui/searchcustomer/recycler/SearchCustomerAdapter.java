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
import com.robifr.ledger.ui.customer.CustomerAction;
import com.robifr.ledger.ui.customer.CustomerListAction;
import com.robifr.ledger.ui.customer.recycler.CustomerListHolder;
import com.robifr.ledger.ui.searchcustomer.SearchCustomerAction;
import com.robifr.ledger.ui.searchcustomer.SearchCustomerFragment;
import com.robifr.ledger.ui.selectcustomer.SelectCustomerAction;
import com.robifr.ledger.ui.selectcustomer.recycler.SelectCustomerListHolder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SearchCustomerAdapter extends RecyclerView.Adapter<RecyclerViewHolder<?, ?>>
    implements CustomerListAction, CustomerAction, SelectCustomerAction, SearchCustomerAction {
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
  public RecyclerViewHolder<?, ?> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
      default -> {
        final CustomerCardWideBinding cardBinding =
            CustomerCardWideBinding.inflate(this._fragment.getLayoutInflater(), parent, false);

        if (this._fragment.searchCustomerViewModel().isSelectionEnabled()) {
          yield new SelectCustomerListHolder<>(cardBinding, this);
        } else {
          yield new CustomerListHolder<>(cardBinding, this);
        }
      }
    };
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int index) {
    Objects.requireNonNull(holder);

    if (holder instanceof SearchCustomerHeaderHolder<?> headerHolder) {
      headerHolder.bind(Optional.empty());

    } else if (holder instanceof CustomerListHolder<?> listHolder) {
      this._fragment
          .searchCustomerViewModel()
          .customers()
          .getValue()
          .map(customers -> customers.get(index - 1)) // -1 offset because header holder.
          .ifPresent(listHolder::bind);

    } else if (holder instanceof SelectCustomerListHolder<?> listHolder) {
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
    return this._fragment.searchCustomerViewModel().isSelectionEnabled();
  }

  @Override
  @NonNull
  public List<Long> initialSelectedCustomerIds() {
    return this._fragment.searchCustomerViewModel().initialSelectedCustomerIds();
  }

  @Override
  public void onCustomerSelected(@Nullable CustomerModel customer) {
    this._fragment.searchCustomerViewModel().onCustomerSelected(customer);
  }
}
