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
import com.robifr.ledger.databinding.CustomerCardBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.filtercustomer.FilterCustomerFragment;
import com.robifr.ledger.ui.main.customer.CustomerCardNormalComponent;
import java.util.List;
import java.util.Objects;

public class FilterCustomerListHolder extends RecyclerViewHolder<CustomerModel>
    implements View.OnClickListener {
  @NonNull private final FilterCustomerFragment _fragment;
  @NonNull private final CustomerCardBinding _cardBinding;
  @NonNull private final CustomerCardNormalComponent _normalCard;
  @Nullable private CustomerModel _boundCustomer;

  public FilterCustomerListHolder(
      @NonNull FilterCustomerFragment fragment, @NonNull CustomerCardBinding binding) {
    super(binding.getRoot());
    this._fragment = Objects.requireNonNull(fragment);
    this._cardBinding = Objects.requireNonNull(binding);
    this._normalCard =
        new CustomerCardNormalComponent(
            this._fragment.requireContext(), this._cardBinding.normalCard);

    this._cardBinding.cardView.setOnClickListener(this);
    // Don't set to `View.GONE` as the position will be occupied by checkbox.
    this._cardBinding.normalCard.menuButton.setVisibility(View.INVISIBLE);
  }

  @Override
  public void bind(@NonNull CustomerModel customer) {
    this._boundCustomer = Objects.requireNonNull(customer);
    final List<CustomerModel> filteredCustomers =
        this._fragment.filterCustomerViewModel().filteredCustomers();

    this._normalCard.setCustomer(this._boundCustomer);
    this._cardBinding.cardView.setChecked(filteredCustomers.contains(this._boundCustomer));
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);
    Objects.requireNonNull(this._boundCustomer);

    switch (view.getId()) {
      case R.id.cardView -> {
        final List<CustomerModel> customers =
            this._fragment.filterCustomerViewModel().customers().getValue();
        if (customers == null) return;

        if (this._cardBinding.cardView.isChecked()) {
          this._fragment.filterCustomerViewModel().onRemoveFilteredCustomer(this._boundCustomer);
        } else {
          this._fragment.filterCustomerViewModel().onAddFilteredCustomer(this._boundCustomer);
        }
      }
    }
  }

  public void setChecked(boolean isChecked) {
    this._cardBinding.cardView.setChecked(isChecked);
  }
}
