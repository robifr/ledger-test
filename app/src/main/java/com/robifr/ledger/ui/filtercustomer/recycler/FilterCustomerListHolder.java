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
import com.robifr.ledger.ui.customer.CustomerListAction;
import com.robifr.ledger.ui.filtercustomer.FilterCustomerAction;
import java.util.ArrayList;
import java.util.Objects;

public class FilterCustomerListHolder<T extends CustomerListAction & FilterCustomerAction>
    extends RecyclerViewHolder<CustomerModel, T> implements View.OnClickListener {
  @NonNull private final CustomerCardWideBinding _cardBinding;
  @NonNull private final CustomerCardWideComponent _card;
  @Nullable private CustomerModel _boundCustomer;

  public FilterCustomerListHolder(@NonNull CustomerCardWideBinding binding, @NonNull T action) {
    super(binding.getRoot(), action);
    this._cardBinding = Objects.requireNonNull(binding);
    this._card = new CustomerCardWideComponent(this.itemView.getContext(), this._cardBinding);

    this._cardBinding.cardView.setOnClickListener(this);
    // Don't set to `View.GONE` as the position will be occupied by expand button.
    this._cardBinding.normalCard.menuButton.setVisibility(View.INVISIBLE);
    this._cardBinding.normalCard.expandButton.setVisibility(View.VISIBLE);
    this._cardBinding.normalCard.expandButton.setOnClickListener(this);
    this._cardBinding.expandedCard.menuButton.setVisibility(View.INVISIBLE);
    this._cardBinding.expandedCard.expandButton.setVisibility(View.VISIBLE);
    this._cardBinding.expandedCard.expandButton.setOnClickListener(this);
  }

  @Override
  public void bind(@NonNull CustomerModel customer) {
    this._boundCustomer = Objects.requireNonNull(customer);

    final boolean shouldCardExpanded =
        this._action.expandedCustomerIndex() != -1
            && this._boundCustomer.equals(
                this._action.customers().get(this._action.expandedCustomerIndex()));

    this._card.reset();
    this._card.setNormalCardCustomer(this._boundCustomer);
    // Prevent reused view holder card to be expanded or checked
    // if current bound customer is different.
    this.setCardChecked(this._action.filteredCustomers().contains(this._boundCustomer));
    this.setCardExpanded(shouldCardExpanded);
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

      case R.id.expandButton -> {
        final boolean isExpanded =
            this._cardBinding.expandedCard.getRoot().getVisibility() == View.VISIBLE;
        final int expandedCustomerIndex =
            !isExpanded ? this._action.customers().indexOf(this._boundCustomer) : -1;

        this._action.onExpandedCustomerIndexChanged(expandedCustomerIndex);

        // Display ripple effect. The effect is gone due to the clicked view
        // set to `View.GONE` when the card expand/collapse.
        if (isExpanded) {
          this._cardBinding.normalCard.expandButton.setPressed(true);
          this._cardBinding.normalCard.expandButton.setPressed(false);
        } else {
          this._cardBinding.expandedCard.expandButton.setPressed(true);
          this._cardBinding.expandedCard.expandButton.setPressed(false);
        }
      }
    }
  }

  public void setCardChecked(boolean isChecked) {
    this._card.setCardChecked(isChecked);
  }

  public void setCardExpanded(boolean isExpanded) {
    Objects.requireNonNull(this._boundCustomer);

    this._card.setCardExpanded(isExpanded);
    // Only fill the view when it's shown on screen.
    if (isExpanded) this._card.setExpandedCardCustomer(this._boundCustomer);
  }
}
