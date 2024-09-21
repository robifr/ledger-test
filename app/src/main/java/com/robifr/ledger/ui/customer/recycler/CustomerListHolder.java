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

package com.robifr.ledger.ui.customer.recycler;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.R;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.databinding.CustomerCardWideBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.customer.CustomerCardAction;
import com.robifr.ledger.ui.customer.CustomerCardWideComponent;
import com.robifr.ledger.ui.customer.CustomerListAction;
import java.util.Objects;

public class CustomerListHolder<T extends CustomerListAction & CustomerCardAction>
    extends RecyclerViewHolder<CustomerModel, T> implements View.OnClickListener {
  @NonNull protected final CustomerCardWideBinding _cardBinding;
  @NonNull protected final CustomerCardWideComponent _card;
  @NonNull protected final CustomerListMenu _menu;
  @Nullable protected CustomerModel _boundCustomer;

  public CustomerListHolder(@NonNull CustomerCardWideBinding binding, @NonNull T action) {
    super(binding.getRoot(), action);
    this._cardBinding = Objects.requireNonNull(binding);
    this._card = new CustomerCardWideComponent(this.itemView.getContext(), this._cardBinding);
    this._menu = new CustomerListMenu(this);

    this._cardBinding.cardView.setOnClickListener(this);
    this._cardBinding.normalCard.menuButton.setOnClickListener(v -> this._menu.openDialog());
    this._cardBinding.expandedCard.menuButton.setOnClickListener(v -> this._menu.openDialog());
  }

  @Override
  public void bind(@NonNull CustomerModel customer) {
    this._boundCustomer = Objects.requireNonNull(customer);
    // Prevent reused view holder to expand the card
    // if current bound customer is different with selected expanded card.
    final boolean shouldCardExpanded =
        this._action.expandedCustomerIndex() != -1
            && this._boundCustomer.equals(
                this._action.customers().get(this._action.expandedCustomerIndex()));

    this._card.reset();
    this._card.setNormalCardCustomer(this._boundCustomer);
    this.setCardExpanded(shouldCardExpanded);
  }

  @Override
  public void onClick(@NonNull View view) {
    Objects.requireNonNull(view);

    switch (view.getId()) {
      case R.id.cardView -> {
        // Only expand when it shrank.
        final int expandedCustomerIndex =
            this._cardBinding.expandedCard.getRoot().getVisibility() != View.VISIBLE
                ? this._action.customers().indexOf(this._boundCustomer)
                : -1;
        this._action.onExpandedCustomerIndexChanged(expandedCustomerIndex);
      }
    }
  }

  @NonNull
  public CustomerModel boundCustomer() {
    return Objects.requireNonNull(this._boundCustomer);
  }

  public void setCardExpanded(boolean isExpanded) {
    Objects.requireNonNull(this._boundCustomer);

    this._card.setCardExpanded(isExpanded);
    // Only fill the view when it's shown on screen.
    if (isExpanded) this._card.setExpandedCardCustomer(this._boundCustomer);
  }
}
