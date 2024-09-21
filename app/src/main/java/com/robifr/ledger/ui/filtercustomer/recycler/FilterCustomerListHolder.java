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

package com.robifr.ledger.ui.filtercustomer.recycler;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.databinding.CustomerCardWideBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.customer.CustomerCardWideComponent;
import com.robifr.ledger.ui.filtercustomer.FilterCustomerAction;
import java.util.ArrayList;
import java.util.Objects;

public class FilterCustomerListHolder<T extends FilterCustomerAction>
    extends RecyclerViewHolder<CustomerModel, T> implements View.OnClickListener {
  @NonNull private final CustomerCardWideBinding _cardBinding;
  @NonNull private final CustomerCardWideComponent _card;
  @Nullable private CustomerModel _boundCustomer;

  public FilterCustomerListHolder(@NonNull CustomerCardWideBinding binding, @NonNull T action) {
    super(binding.getRoot(), action);
    this._cardBinding = Objects.requireNonNull(binding);
    this._card = new CustomerCardWideComponent(this.itemView.getContext(), this._cardBinding);

    this._cardBinding.cardView.setOnClickListener(this);
    // Don't set to `View.GONE` as the position will be occupied by checkbox.
    this._cardBinding.normalCard.menuButton.setVisibility(View.INVISIBLE);
  }

  @Override
  public void bind(@NonNull CustomerModel customer) {
    this._boundCustomer = Objects.requireNonNull(customer);

    this._card.reset();
    this._card.setNormalCardCustomer(this._boundCustomer);
    this._cardBinding.cardView.setChecked(
        this._action.filteredCustomers().contains(this._boundCustomer));
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);
    Objects.requireNonNull(this._boundCustomer);

    switch (view.getId()) {
      case R.id.cardView -> {
        final ArrayList<CustomerModel> filteredCustomers =
            new ArrayList<>(this._action.filteredCustomers());

        if (this._cardBinding.cardView.isChecked()) filteredCustomers.remove(this._boundCustomer);
        else filteredCustomers.add(this._boundCustomer);

        this._action.onFilteredCustomersChanged(filteredCustomers);
      }
    }
  }

  public void setChecked(boolean isChecked) {
    this._cardBinding.cardView.setChecked(isChecked);
  }
}
