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

package com.robifr.ledger.ui.selectcustomer.recycler;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.databinding.CustomerCardWideBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.customer.CustomerCardWideComponent;
import com.robifr.ledger.ui.selectcustomer.SelectCustomerCardAction;
import java.util.Objects;

public class SelectCustomerListHolder<T extends SelectCustomerCardAction>
    extends RecyclerViewHolder<CustomerModel, T> implements View.OnClickListener {
  @NonNull private final CustomerCardWideBinding _cardBinding;
  @NonNull private final CustomerCardWideComponent _card;
  @Nullable private CustomerModel _boundCustomer;

  public SelectCustomerListHolder(@NonNull CustomerCardWideBinding binding, @NonNull T action) {
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
    final boolean shouldChecked =
        this._action.initialSelectedCustomer() != null
            && this._action.initialSelectedCustomer().id() != null
            && this._action.initialSelectedCustomer().id().equals(this._boundCustomer.id());

    this._card.setNormalCardCustomer(this._boundCustomer);
    this._cardBinding.cardView.setChecked(shouldChecked);
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);
    Objects.requireNonNull(this._boundCustomer);

    switch (view.getId()) {
      case R.id.cardView -> this._action.onCustomerSelected(this._boundCustomer);
    }
  }
}
