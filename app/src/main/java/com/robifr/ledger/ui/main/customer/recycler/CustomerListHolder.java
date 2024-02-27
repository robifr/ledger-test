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

package com.robifr.ledger.ui.main.customer.recycler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.robifr.ledger.data.model.CustomerModel;
import com.robifr.ledger.databinding.CustomerCardBinding;
import com.robifr.ledger.ui.RecyclerViewHolder;
import com.robifr.ledger.ui.main.customer.CustomerCardNormalComponent;
import com.robifr.ledger.ui.main.customer.CustomerFragment;
import java.util.Objects;

public class CustomerListHolder extends RecyclerViewHolder<CustomerModel> {
  @NonNull private final CustomerFragment _fragment;
  @NonNull private final CustomerCardBinding _cardBinding;
  @NonNull private final CustomerCardNormalComponent _normalCard;
  @NonNull private final CustomerListMenu _menu;
  @Nullable private CustomerModel _boundCustomer;

  public CustomerListHolder(
      @NonNull CustomerFragment fragment, @NonNull CustomerCardBinding binding) {
    super(binding.getRoot());
    this._fragment = Objects.requireNonNull(fragment);
    this._cardBinding = Objects.requireNonNull(binding);
    this._normalCard =
        new CustomerCardNormalComponent(
            this._fragment.requireContext(), this._cardBinding.normalCard);
    this._menu = new CustomerListMenu(this._fragment, this);

    this._cardBinding.cardView.setClickable(false);
    this._cardBinding.normalCard.menuButton.setOnClickListener(v -> this._menu.openDialog());
  }

  @Override
  public void bind(@NonNull CustomerModel customer) {
    this._boundCustomer = Objects.requireNonNull(customer);
    this._normalCard.setCustomer(this._boundCustomer);
  }

  @NonNull
  public CustomerModel boundCustomer() {
    return Objects.requireNonNull(this._boundCustomer);
  }
}
