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

package com.robifr.ledger.ui.search_customer.recycler;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.databinding.CustomerCardBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.search_customer.SearchCustomerFragment;
import java.util.List;
import java.util.Objects;

public class SearchCustomerAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
  @NonNull private final SearchCustomerFragment _fragment;

  public SearchCustomerAdapter(@NonNull SearchCustomerFragment fragment) {
    this._fragment = Objects.requireNonNull(fragment);
  }

  @Override
  @NonNull
  public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    Objects.requireNonNull(parent);

    return new SearchCustomerListHolder(
        this._fragment,
        CustomerCardBinding.inflate(this._fragment.getLayoutInflater(), parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int index) {
    Objects.requireNonNull(holder);

    final List<CustomerModel> customers =
        this._fragment.searchCustomerViewModel().customers().getValue();
    if (customers == null) return;

    if (holder instanceof SearchCustomerListHolder listHolder) {
      listHolder.bind(customers.get(index));
    }
  }

  @Override
  public int getItemCount() {
    final List<CustomerModel> customers =
        this._fragment.searchCustomerViewModel().customers().getValue();
    return customers != null ? customers.size() : 0;
  }
}
