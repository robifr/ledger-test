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
import com.robifr.ledger.ui.customer.CustomerCardExpandedComponent;
import com.robifr.ledger.ui.customer.CustomerCardNormalComponent;
import com.robifr.ledger.ui.customer.CustomerFragment;
import java.util.List;
import java.util.Objects;

public class CustomerListHolder extends RecyclerViewHolder<CustomerModel>
    implements View.OnClickListener {
  @NonNull private final CustomerFragment _fragment;
  @NonNull private final CustomerCardWideBinding _cardBinding;
  @NonNull private final CustomerCardNormalComponent _normalCard;
  @NonNull private final CustomerCardExpandedComponent _expandedCard;
  @NonNull private final CustomerListMenu _menu;
  @Nullable private CustomerModel _boundCustomer;

  public CustomerListHolder(
      @NonNull CustomerFragment fragment, @NonNull CustomerCardWideBinding binding) {
    super(binding.getRoot());
    this._fragment = Objects.requireNonNull(fragment);
    this._cardBinding = Objects.requireNonNull(binding);
    this._normalCard =
        new CustomerCardNormalComponent(
            this._fragment.requireContext(), this._cardBinding.normalCard);
    this._expandedCard =
        new CustomerCardExpandedComponent(
            this._fragment.requireContext(), this._cardBinding.expandedCard);
    this._menu = new CustomerListMenu(this._fragment, this);

    this._cardBinding.cardView.setOnClickListener(this);
    this._cardBinding.normalCard.menuButton.setOnClickListener(v -> this._menu.openDialog());
    this._cardBinding.expandedCard.menuButton.setOnClickListener(v -> this._menu.openDialog());
  }

  @Override
  public void bind(@NonNull CustomerModel customer) {
    this._boundCustomer = Objects.requireNonNull(customer);

    this._normalCard.setCustomer(this._boundCustomer);
    this._expandedCard.reset();

    // Prevent reused view holder to expand the card
    // if current bound customer is different with selected expanded card.
    final List<CustomerModel> customers = this._fragment.customerViewModel().customers().getValue();
    final int expandedCustomerIndex =
        this._fragment.customerViewModel().expandedCustomerIndex().getValue();
    final boolean shouldCardExpanded =
        expandedCustomerIndex != -1
            && this._boundCustomer.equals(customers.get(expandedCustomerIndex));

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
                ? this._fragment
                    .customerViewModel()
                    .customers()
                    .getValue()
                    .indexOf(this._boundCustomer)
                : -1;
        this._fragment.customerViewModel().onExpandedCustomerIndexChanged(expandedCustomerIndex);
      }
    }
  }

  @NonNull
  public CustomerModel boundCustomer() {
    return Objects.requireNonNull(this._boundCustomer);
  }

  public void setCardExpanded(boolean isExpanded) {
    Objects.requireNonNull(this._boundCustomer);

    final int normalCardVisibility = isExpanded ? View.GONE : View.VISIBLE;
    final int expandedCardVisibility = isExpanded ? View.VISIBLE : View.GONE;

    this._cardBinding.normalCard.getRoot().setVisibility(normalCardVisibility);
    this._cardBinding.expandedCard.getRoot().setVisibility(expandedCardVisibility);

    // Only fill the view when it's shown on screen.
    if (isExpanded) this._expandedCard.setCustomer(this._boundCustomer);
  }
}
